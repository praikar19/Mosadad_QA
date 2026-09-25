package com.mosadad.testing.tests.claims;

import com.microsoft.playwright.Page;
import com.mosadad.testing.base.BaseTwoActorUiTest;
import com.mosadad.testing.config.ConfigManager;
import com.mosadad.testing.constants.Routes;
import com.mosadad.testing.pages.AtbCheckoutPage;
import com.mosadad.testing.pages.BulkSettlementPage;
import com.mosadad.testing.pages.BulkSettlementPaymentPage;
import com.mosadad.testing.pages.CreditNoteBulkPage;
import com.mosadad.testing.pages.DashboardPage;
import com.mosadad.testing.pages.FastTrackPage;
import com.mosadad.testing.pages.LoginPage;
import com.mosadad.testing.pages.PaymentSuccessPage;
import com.mosadad.testing.pages.RecoveryClaimsHubPage;
import com.mosadad.testing.pages.claims.FastTrackClaimDetailsPage;
import com.mosadad.testing.pages.claims.FastTrackClaimsListPage;
import com.mosadad.testing.pages.claims.FastTrackCreateBatchPage;
import com.mosadad.testing.pages.claims.FastTrackReuploadBatchPage;
import com.mosadad.testing.utils.FastTrackRunClaims;
import com.mosadad.testing.utils.FastTrackTestDataGenerator;
import com.mosadad.testing.utils.FastTrackTestDataGenerator.AtFaultSheet;
import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.model.Status;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mosadad.testing.utils.FastTrackTestDataGenerator.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Fast Track end to end on stage, claimant (Union) → at-fault (Orient): batch
 * upload through "Credit Note Created", then the at-fault pays the credit notes
 * through the ATB Pay checkout. WARNING: the last step is a REAL payment from
 * Orient's wallet, and every run leaves a paid batch of 2 claims on stage. It is
 * in no regression suite; run it on purpose with {@code mvn test -P fast-track}.
 */
@Epic("Mosadad Recovery Claim")
@Feature("Fast Track")
public class FastTrackE2ETest extends BaseTwoActorUiTest {

    private static final String PLATFORM = "stage";
    private static final String CLAIMANT_USER = "union";
    private static final String AT_FAULT_USER = "orient";
    /** How each entity's name appears to the other side (verified live on stage). */
    private static final String AT_FAULT_ENTITY = "ORIENT INSURANCE PJSC";
    private static final String CLAIMANT_ENTITY = "Union Insurance Company";

    /** Both under the AED 5,000 auto-accept threshold; the credit note totals AED 7,200. */
    private static final int[] CLAIM_AMOUNTS_AED = {4100, 3100};

    private static final String XLSX_MIME = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String PDF_MIME = "application/pdf";
    private static final byte[] RECOVERY_DOCUMENT_PDF = readClasspathResource("/fixtures/accident-report-sample.pdf");

    private static final String UPLOAD_EXCEL_ACTION = "Upload Excel";

    private static final String STATUS_PENDING_DOCUMENT = "Pending 1 Document(s)";
    private static final String STATUS_SUBMITTED = "Submitted";
    private static final String STATUS_INVOICE_ACCEPTED = "Invoice Accepted";
    private static final String STATUS_CREDIT_NOTE_CREATED = "Credit Note Created";
    private static final String STATUS_PAYMENT_SUCCESSFUL = "Payment Successful";
    /** Toasts, from the app's English texts. */
    private static final String TOAST_CREDIT_NOTE_ISSUED = "Credit Note issued successfully";
    private static final String TOAST_SETTLEMENT_REQUEST_SENT = "Settlement request has been sent successfully";

    @Test(groups = {"ui", "fast-track", "stage"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("Claimant uploads 2 Fast Track claims under AED 5,000, adds the recovery documents and submits the batch; "
            + "the at-fault adds its claim numbers and the claims reach 'Invoice Accepted', the credit note is "
            + "requested and both sides see 'Credit Note Created'; the at-fault pays the credit notes via ATB Pay and "
            + "the claimant sees every claim as 'Payment Successful'.")
    public void fastTrackBatchIsSettledAndPaid() {
        // Step 1 — both actors log in.
        DashboardPage claimantDashboard = login(claimantPage, CLAIMANT_USER);
        DashboardPage atFaultDashboard = login(atFaultPage, AT_FAULT_USER);

        // Step 3 — a sheet with fresh claim data.
        List<Map<String, String>> rows = generateRowsWithAmounts(CLAIM_AMOUNTS_AED);
        List<String> claimNumbers = rows.stream().map(row -> row.get(COL_CLAIM_NUMBER)).toList();
        log.info("Generated Fast Track claims: {}", claimNumbers);
        // Kept for the rest of this run only (see FastTrackRunClaims), with each claim's batch code.
        List<FastTrackRunClaims.Entry> runClaims = rows.stream()
                .map(row -> FastTrackRunClaims.add(row.get(COL_CLAIM_NUMBER), digitsOnly(row.get(COL_RECOVERY_CLAIM_AMOUNT))))
                .toList();

        // Step 4 — claimant creates the batch.
        FastTrackCreateBatchPage createBatch = claimantDashboard.openRecoveryClaims().openFastTrack().clickCreateNewBatch();
        assertThat(createBatch.isLoaded()).as("Create New Batch screen should load").isTrue();
        createBatch.selectFaultyEntity(AT_FAULT_ENTITY);
        createBatch.uploadClaimsFile("fast-track-claims.xlsx", XLSX_MIME, FastTrackTestDataGenerator.writeToXlsxBytes(rows));
        FastTrackClaimsListPage claimantList = createBatch.validateAndCreateBatch();

        assertThat(claimantList.getClaimNumbers())
                .as("The batch should hold exactly the uploaded claims")
                .containsExactlyInAnyOrderElementsOf(claimNumbers);
        assertThat(claimantList.getStatuses())
                .as("Each new claim still needs its recovery document")
                .containsOnly(STATUS_PENDING_DOCUMENT);
        String claimantBatchUrl = claimantPage.url();

        // Step 5 — check each claim's summary against the sheet and upload its recovery document.
        String batchCode = null;
        for (Map<String, String> row : rows) {
            String claimNumber = row.get(COL_CLAIM_NUMBER);
            claimantPage.navigate(claimantBatchUrl);
            FastTrackClaimDetailsPage details = new FastTrackClaimsListPage(claimantPage).openClaim(claimNumber);

            assertThat(details.getSummaryValue("Claimant Claim Number")).as("Claim number").isEqualTo(claimNumber);
            assertThat(details.getSummaryValue("Report Number")).as("Accident no. of %s", claimNumber).isEqualTo(row.get(COL_ACCIDENT_NO));
            assertThat(details.getSummaryValue("Report Date")).as("Accident date of %s", claimNumber).isEqualTo(row.get(COL_ACCIDENT_DATE));
            assertThat(details.getSummaryValue("Plate Number")).as("Plate of %s", claimNumber).isEqualTo(row.get(COL_PLATE));
            assertThat(details.getSummaryValue("Atfault Plate Number")).as("At-fault plate of %s", claimNumber).isEqualTo(row.get(COL_ATFAULT_PLATE_NUMBER));
            assertThat(details.getSummaryValue("Claim Amount")).as("Claim amount of %s", claimNumber)
                    .isEqualTo(digitsOnly(row.get(COL_RECOVERY_CLAIM_AMOUNT)));
            String claimBatchCode = details.getBatchCode();
            assertThat(claimBatchCode).as("Batch code of %s", claimNumber).startsWith("RC-");
            if (batchCode != null) {
                assertThat(claimBatchCode).as("Every claim should be in the same batch").isEqualTo(batchCode);
            }
            batchCode = claimBatchCode;
            FastTrackRunClaims.get(claimNumber).batchCode = claimBatchCode;
            FastTrackRunClaims.get(claimNumber).lastStatus = STATUS_PENDING_DOCUMENT;

            String fileName = FastTrackTestDataGenerator.claimNumberForFileName(claimNumber) + "_recovery-document.pdf";
            details.uploadMandatoryDocument(fileName, PDF_MIME, RECOVERY_DOCUMENT_PDF);
            details.clickSave();
        }
        assertThat(batchCode).as("Claim Summary should show the batch code").startsWith("RC-");
        log.info("Fast Track batch {} created with claims {}", batchCode, claimNumbers);

        // Step 6 — submit the batch to the at-fault insurer.
        claimantPage.navigate(claimantBatchUrl);
        claimantList = new FastTrackClaimsListPage(claimantPage);
        String batchSummary = claimantList.openSubmitToAtFaultDialog();
        assertThat(countNextTo(batchSummary, "complete")).as("Complete claims in Batch Summary:%n%s", batchSummary).isEqualTo(rows.size());
        assertThat(countNextTo(batchSummary, "incomplete")).as("Incomplete claims in Batch Summary:%n%s", batchSummary).isZero();
        assertThat(batchSummary).as("Batch Summary should warn the batch will be locked").containsIgnoringCase("locked");
        String confirmation = claimantList.confirmSubmitToAtFault();
        assertThat(confirmation)
                .as("Submit confirmation should name the batch and the at-fault insurer")
                .contains(batchCode)
                .containsIgnoringCase(AT_FAULT_ENTITY)
                .containsIgnoringCase("locked");

        runClaims.forEach(claim -> claim.lastStatus = STATUS_SUBMITTED);

        FastTrackPage claimantFastTrack = openFastTrack(claimantPage);
        assertThat(claimantFastTrack.getBatchStatus(batchCode)).as("Claimant's batch status after submit").isEqualTo(STATUS_SUBMITTED);

        // Step 7 — at-fault finds the batch under Payable Claims.
        FastTrackPage atFaultFastTrack = atFaultDashboard.openRecoveryClaims().openFastTrack().openPayableClaimsTab();
        assertThat(atFaultFastTrack.getBatchStatus(batchCode)).as("At-fault's batch status").isEqualTo(STATUS_SUBMITTED);
        FastTrackClaimsListPage atFaultList = atFaultFastTrack.openBatch(batchCode);
        assertThat(atFaultList.isProceedToSettlementVisible())
                .as("At-fault should see 'Proceed to Settlement' on a submitted batch")
                .isTrue();
        String atFaultBatchUrl = atFaultPage.url();

        // Step 8 — at-fault downloads the sheet and fills in its own claim numbers.
        atFaultFastTrack = openFastTrack(atFaultPage).openPayableClaimsTab();
        AtFaultSheet atFaultSheet = FastTrackTestDataGenerator.fillAtFaultClaimNumbers(atFaultFastTrack.downloadSheet(batchCode));
        assertThat(atFaultSheet.atFaultClaimNumbers()).as("One at-fault claim number per claim in the sheet").hasSize(rows.size());

        // Step 9 — at-fault re-uploads the filled sheet, or, when stage doesn't offer
        // "Upload Excel", flags it and enters each claim's number on its details page.
        List<String> atFaultClaimNumbers;
        List<String> rowActions = atFaultFastTrack.getRowActions(batchCode);
        if (rowActions.contains(UPLOAD_EXCEL_ACTION)) {
            FastTrackReuploadBatchPage reupload = atFaultFastTrack.openUploadExcel(batchCode);
            assertThat(reupload.isLoaded()).as("'Update And Re-upload Batch' screen should load").isTrue();
            reupload.uploadSheet("fast-track-at-fault.xlsx", XLSX_MIME, atFaultSheet.xlsx());
            atFaultList = reupload.validateAndReupload();
            atFaultClaimNumbers = atFaultSheet.atFaultClaimNumbers();
            // The sheet's row order isn't tied to claimant claim numbers, so these stay unmapped in the ledger.
        } else {
            flagUploadExcelMissing(batchCode, rowActions);
            atFaultClaimNumbers = addAtFaultClaimNumbersOneByOne(atFaultBatchUrl);
            atFaultPage.navigate(atFaultBatchUrl);
            atFaultList = new FastTrackClaimsListPage(atFaultPage);
        }
        assertThat(atFaultList.getStatusByClaimNumber().keySet())
                .as("The batch should now list the at-fault claim numbers")
                .containsExactlyInAnyOrderElementsOf(atFaultClaimNumbers);

        // Step 10 — every claim reads "Invoice Accepted" in the batch, on both sides
        // (auto-accepted under AED 5,000 after a re-upload; accepted one by one otherwise).
        waitForAllStatuses(atFaultList, STATUS_INVOICE_ACCEPTED, "at-fault");
        claimantPage.navigate(claimantBatchUrl);
        FastTrackClaimsListPage claimantAcceptedList = new FastTrackClaimsListPage(claimantPage);
        waitForAllStatuses(claimantAcceptedList, STATUS_INVOICE_ACCEPTED, "claimant");
        Map<String, String> serialByClaimNumber = claimantAcceptedList.getSerialByClaimNumber();
        serialByClaimNumber.forEach((claimNumber, serial) -> {
            FastTrackRunClaims.Entry claim = FastTrackRunClaims.get(claimNumber);
            if (claim != null) {
                claim.claimSerialNumber = serial;
                claim.lastStatus = STATUS_INVOICE_ACCEPTED;
            }
        });
        List<String> claimSerialNumbers = List.copyOf(serialByClaimNumber.values());
        assertThat(claimSerialNumbers).as("Claim serial numbers").hasSize(rows.size()).allMatch(s -> s.startsWith("RC-"));

        int expectedTotalAed = Arrays.stream(CLAIM_AMOUNTS_AED).sum();
        String expectedTotal = String.format("%,d.00", expectedTotalAed);
        String expectedSummary = rows.size() + " / " + expectedTotal;

        // Step 11 — at-fault gets the credit note issued (Proceed to Settlement → Bulk Settlement).
        BulkSettlementPage bulkSettlement = atFaultList.clickProceedToSettlement();
        assertThat(bulkSettlement.isLoaded()).as("Proceed to Settlement should open Bulk Settlement").isTrue();
        if (bulkSettlement.isRequestSettlementEnabled(CLAIMANT_ENTITY)) {
            assertThat(bulkSettlement.requestSettlement(CLAIMANT_ENTITY))
                    .as("Toast after Request Settlement")
                    .containsIgnoringCase(TOAST_SETTLEMENT_REQUEST_SENT);
        } else {
            issueCreditNoteViaSettle(bulkSettlement, claimSerialNumbers, expectedTotal, expectedSummary);
        }

        // Step 12 — both sides see "Credit Note Created" on every claim of the batch.
        claimantPage.navigate(claimantBatchUrl);
        waitForAllStatuses(new FastTrackClaimsListPage(claimantPage), STATUS_CREDIT_NOTE_CREATED, "claimant");
        atFaultPage.navigate(atFaultBatchUrl);
        waitForAllStatuses(new FastTrackClaimsListPage(atFaultPage), STATUS_CREDIT_NOTE_CREATED, "at-fault");
        runClaims.forEach(claim -> claim.lastStatus = STATUS_CREDIT_NOTE_CREATED);

        // Step 13 — at-fault pays this run's credit notes: Recovery Claims → Due Amount →
        // Settle (Union) → tick only this run's claims → Proceed To Payment → ATB checkout →
        // I agree → Confirm → Payment Success → Return To Home Screen. REAL payment from
        // Orient's wallet; starting the checkout locks the claims for 24h.
        atFaultPage.navigate(ConfigManager.getBaseUrl(PLATFORM) + Routes.RECOVERY_CLAIMS_HUB);
        CreditNoteBulkPage creditNotes = new RecoveryClaimsHubPage(atFaultPage).openDueAmount().settle(CLAIMANT_ENTITY);
        creditNotes.selectOnly(claimSerialNumbers);
        assertThat(creditNotes.waitForSummary(rows.size(), expectedTotal))
                .as("Selected claims / Total Amount on Credit Note Bulk Details")
                .isEqualTo(expectedSummary);

        AtbCheckoutPage checkout = creditNotes.proceedToPayment();
        String invoice = checkout.getInvoiceText();
        Allure.addAttachment("ATB checkout — invoice details", "text/plain", invoice);
        log.info("ATB checkout total (claims + Mosadad fees): {}", checkout.getTotalAmount());
        assertThat(invoice)
                .as("Checkout should bill %s for this run's AED %d", CLAIMANT_ENTITY, expectedTotalAed)
                .contains(String.valueOf(expectedTotalAed));

        PaymentSuccessPage paymentSuccess = checkout.agreeToTerms().confirmPayment();
        Allure.addAttachment("Payment successful — page", "image/png",
                new ByteArrayInputStream(paymentSuccess.screenshot()), "png");
        assertThat(paymentSuccess.getHeading()).as("Payment result heading").isEqualTo(PaymentSuccessPage.SUCCESS_HEADING);
        String transactionId = paymentSuccess.getTransactionId();
        assertThat(transactionId).as("Transaction ID on the payment success page").isNotBlank();
        log.info("Payment successful for batch {} — transaction ID {}", batchCode, transactionId);
        Allure.step("Payment successful — transaction ID " + transactionId);
        // This click is what reports the payment to Mosadad for each claim.
        paymentSuccess.returnToHomeScreen();

        // Step 14 — the claimant (non-fault) sees every claim of the batch as "Payment Successful".
        claimantPage.navigate(claimantBatchUrl);
        waitForAllStatuses(new FastTrackClaimsListPage(claimantPage), STATUS_PAYMENT_SUCCESSFUL, "claimant");
        runClaims.forEach(claim -> claim.lastStatus = STATUS_PAYMENT_SUCCESSFUL);

        Allure.addAttachment("Fast Track claims (batch code per claim)", "text/plain",
                String.join("\n", runClaims.stream().map(Object::toString).toList()));
    }

    /**
     * The claims and batch codes are only for this run: logged for the record
     * (pass or fail), then dropped once every test in the run has finished.
     */
    @AfterSuite(alwaysRun = true)
    public void clearRunClaims() {
        FastTrackRunClaims.all().forEach(claim -> log.info("Fast Track claim this run: {}", claim));
        FastTrackRunClaims.clear();
    }

    /**
     * Stage's front end only offers "Upload Excel" for batch status 2, but a
     * submitted batch comes back as 5 (see FRAMEWORK.md). Shown as a broken
     * step in Allure so the report makes the workaround visible.
     */
    private void flagUploadExcelMissing(String batchCode, List<String> rowActions) {
        String message = "App issue: 'Upload Excel' is not offered for submitted batch " + batchCode
                + " (⋮ menu has " + rowActions + ") — at-fault claim numbers entered per claim instead";
        log.warn(message);
        Allure.step(message, Status.BROKEN);
    }

    /**
     * Bulk Settlement only enables "Request Settlement" when the row has a
     * receivable amount; the at-fault side owes, so it gets "Settle": tick only
     * this run's claims → Issue Credit Note → back on Bulk Settlement.
     */
    private void issueCreditNoteViaSettle(BulkSettlementPage bulkSettlement, List<String> claimSerialNumbers,
                                          String expectedTotal, String expectedSummary) {
        String message = "'Request Settlement' is disabled for " + CLAIMANT_ENTITY
                + " (nothing receivable from the at-fault side) — issuing the credit note via Settle instead";
        log.info(message);
        Allure.step(message);

        BulkSettlementPaymentPage payment = bulkSettlement.settle(CLAIMANT_ENTITY);
        payment.selectOnly(claimSerialNumbers);
        assertThat(payment.waitForSummary(claimSerialNumbers.size(), expectedTotal))
                .as("Selected claims / Total Amount on Bulk Settlement Payment")
                .isEqualTo(expectedSummary);
        assertThat(payment.issueCreditNote())
                .as("Toast after Issue Credit Note")
                .containsIgnoringCase(TOAST_CREDIT_NOTE_ISSUED);
    }

    /**
     * On each claim's details page: enters a fresh at-fault claim number, saves,
     * then accepts the claim and checks it reads "Invoice Accepted". Returns the
     * numbers in row order.
     */
    private List<String> addAtFaultClaimNumbersOneByOne(String atFaultBatchUrl) {
        atFaultPage.navigate(atFaultBatchUrl);
        int claimCount = new FastTrackClaimsListPage(atFaultPage).getClaimCount();
        List<String> numbers = new ArrayList<>();
        for (int i = 0; i < claimCount; i++) {
            atFaultPage.navigate(atFaultBatchUrl);
            FastTrackClaimDetailsPage details = new FastTrackClaimsListPage(atFaultPage).openClaimAt(i);
            String atFaultClaimNumber = FastTrackTestDataGenerator.newAtFaultClaimNumber();
            FastTrackRunClaims.Entry claim = FastTrackRunClaims.get(details.getSummaryValue("Claimant Claim Number"));
            assertThat(details.addAtFaultClaimNumber(atFaultClaimNumber))
                    .as("At Fault Claim Number shown after saving it on claim %d", i + 1)
                    .startsWith(atFaultClaimNumber);
            assertThat(details.acceptClaim(STATUS_INVOICE_ACCEPTED))
                    .as("Claim Status on claim %d after Accept", i + 1)
                    .isEqualTo(STATUS_INVOICE_ACCEPTED);
            if (claim != null) {
                claim.atFaultClaimNumber = atFaultClaimNumber;
                claim.lastStatus = STATUS_INVOICE_ACCEPTED;
            }
            numbers.add(atFaultClaimNumber);
        }
        log.info("Entered at-fault claim numbers one by one: {}", numbers);
        return numbers;
    }

    private DashboardPage login(Page page, String userKey) {
        page.navigate(ConfigManager.getLoginUrl(PLATFORM));
        LoginPage loginPage = new LoginPage(page);
        DashboardPage dashboard = loginPage.login(
                ConfigManager.getEmail(PLATFORM, userKey), ConfigManager.getPassword(PLATFORM, userKey));
        boolean loaded = dashboard.isLoaded();
        String reason = !loaded && loginPage.isErrorToastVisible() ? " — app said: \"" + loginPage.getErrorToastMessage() + "\"" : "";
        assertThat(loaded).as("%s should land on the dashboard%s", userKey, reason).isTrue();
        return dashboard;
    }

    private static FastTrackPage openFastTrack(Page page) {
        page.navigate(ConfigManager.getBaseUrl(PLATFORM) + Routes.FAST_TRACK);
        return new FastTrackPage(page);
    }

    /** Status changes are made server-side and not pushed, so reload until they show. */
    private void waitForAllStatuses(FastTrackClaimsListPage list, String expected, String actor) {
        List<String> statuses = list.getStatuses();
        for (int attempt = 1; attempt <= 6 && !statuses.stream().allMatch(expected::equals); attempt++) {
            log.info("{} statuses {} — waiting for '{}' (attempt {})", actor, statuses, expected, attempt);
            list.refresh();
            statuses = list.getStatuses();
        }
        assertThat(statuses).as("Every claim should read '%s' for the %s", expected, actor).containsOnly(expected);
    }

    /**
     * The number printed next to a word in the Batch Summary, either order
     * ("2 complete" or "Complete: 2"). "complete" does not match inside "incomplete".
     */
    private static int countNextTo(String text, String word) {
        Matcher m = Pattern.compile("(?is)(\\d+)\\W{0,5}\\b" + word + "\\b|\\b" + word + "\\b\\D{0,20}?(\\d+)").matcher(text);
        if (!m.find()) {
            throw new AssertionError("No count next to '" + word + "' in Batch Summary:\n" + text);
        }
        return Integer.parseInt(m.group(1) != null ? m.group(1) : m.group(2));
    }

    private static String digitsOnly(String amount) {
        return amount.replaceAll("\\D", "");
    }

    private static byte[] readClasspathResource(String path) {
        try (InputStream in = FastTrackE2ETest.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Test fixture not found on classpath: " + path);
            }
            return in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read test fixture: " + path, e);
        }
    }
}
