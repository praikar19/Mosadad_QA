package com.mosadad.testing.tests.claims;

import com.microsoft.playwright.Page;
import com.mosadad.testing.config.ConfigManager;
import com.mosadad.testing.pages.DashboardPage;
import com.mosadad.testing.pages.LoginPage;
import com.mosadad.testing.pages.RecoveryClaimsHubPage;
import com.mosadad.testing.pages.claims.CreateManualClaimPage;
import com.mosadad.testing.pages.claims.ManualClaimDetails;
import com.mosadad.testing.pages.claims.OpenRecoveryClaimsPage;
import com.mosadad.testing.pages.claims.PotentialRecoveryClaimsListPage;
import com.mosadad.testing.utils.RandomUniqueGenerator;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Shared two-actor setup for QuotationTest, InvoiceTest, and SettlementTest:
 * each of those exercises one claim stage, but every stage after the first
 * needs the claim to already exist and have progressed through every prior
 * stage — Stage 2 needs an accepted claim, Stage 3 needs an accepted
 * quotation, Stage 4 needs an accepted invoice (see MOSADAD_DOMAIN.md's
 * Claim Lifecycle). Rather than duplicate that setup in three classes, each
 * stage's fixture method here takes the previous stage's result and returns
 * its own, so a Stage 4 test simply chains
 * {@code createAndAcceptClaim() -> submitAndAcceptNormalRepairQuotation() ->
 * submitAndAcceptInvoice() -> requestSettleAndCloseClaim()}.
 *
 * Every method here is real, live-verified logic (2026-09-03 through
 * 2026-09-07 against the stage QA environment) — not a TODO placeholder.
 * It was originally written as one long two-actor E2E method (claimant +
 * at-fault, Chrome + WebKit) that walked the entire lifecycle in a single
 * test; it's split up here so each stage's own test class can exercise up
 * to (and asserting on) just its own stage, matching how the rest of this
 * suite is organized. The original is still visible in git history if the
 * split ever needs rechecking against it.
 *
 * Still opt-in, not automatic: every @Test method that calls into this
 * class stays {@code enabled=false} — reaching Stage 4 ends in a real ATB
 * Pay checkout payment against shared QA data (see
 * {@code requestSettleAndCloseClaim}'s Javadoc), and CLAUDE.md's
 * risk-level guidance says that's not something to run by default. Review
 * the impact before flipping any of these on.
 */
final class ClaimLifecycleFixtures {

    private ClaimLifecycleFixtures() {}

    /** Everything Stage 2/3/4 need to keep going: both actors' claim-detail pages, the claim's serial number, and the data that was actually submitted (for assertions). */
    record Stage1Result(OpenRecoveryClaimsPage claimantClaim, OpenRecoveryClaimsPage atFaultClaim,
                         String serialNumber, ManualClaimDetails details) {}

    /** What Stage 3 (invoice) needs from Stage 2: the repair cost, since the invoice amount must match it exactly or the server rejects it. */
    record QuotationResult(String quotationNumber, String repairCost) {}

    /**
     * Stage 1 (Manual Entry — MOSADAD_DOMAIN.md §Stage 1): logs both actors
     * in for real (stage/dubai claimant, stage/dnl at-fault — both real,
     * configured credentials, not the "mehtaq" account an earlier draft of
     * this flow warned was broken), creates a manual claim as the
     * claimant, and has the at-fault actor accept it.
     */
    static Stage1Result createAndAcceptClaim(Page claimantPage, Page atFaultPage) {
        claimantPage.navigate(ConfigManager.getLoginUrl("stage"));
        DashboardPage claimantDashboard = new LoginPage(claimantPage).login(
                ConfigManager.getEmail("stage", "dubai"),
                ConfigManager.getPassword("stage", "dubai"));
        assertThat(claimantDashboard.isLoaded())
                .as("Claimant (Chrome, Dubai account) should land on the dashboard")
                .isTrue();

        atFaultPage.navigate(ConfigManager.getLoginUrl("stage"));
        LoginPage atFaultLogin = new LoginPage(atFaultPage);
        DashboardPage atFaultDashboard = atFaultLogin.login(
                ConfigManager.getEmail("stage", "dnl"),
                ConfigManager.getPassword("stage", "dnl"));
        String atFaultFailureReason = atFaultLogin.isErrorToastVisible()
                ? " — app said: \"" + atFaultLogin.getErrorToastMessage() + "\""
                : "";
        assertThat(atFaultDashboard.isLoaded())
                .as("At-fault (WebKit, DNL account) should land on the dashboard%s", atFaultFailureReason)
                .isTrue();

        // The default Playwright viewport (1280x720) is too short for this
        // form — confirmed live 2026-09-03: a mat-datepicker overlay opened
        // near the bottom of the page renders partly off-screen and
        // Playwright refuses to click into it ("element is outside of the
        // viewport").
        claimantPage.setViewportSize(1600, 2200);

        RecoveryClaimsHubPage hub = claimantDashboard.openRecoveryClaims();
        PotentialRecoveryClaimsListPage potentialList = hub.openPotentialRecoveryClaimsListPage();
        assertThat(potentialList.isLoaded()).as("Potential Recovery Claims List screen should load").isTrue();

        // Confirmed live 2026-09-07: "Create New Recovery Claim" now lands
        // directly on the Create Manual Claim form — the separate
        // police-report-provider gate screen (select provider, upload
        // report, submit) that used to precede it is gone entirely, not
        // just occasionally slow to render. Wrapping the same claimantPage
        // directly in CreateManualClaimPage works without needing
        // PotentialRecoveryClaimsListPage's or CreateManualRecoveryClaimPage's
        // return type to change. NOTE: CreateManualRecoveryClaimTest still
        // exercises the old gate screen directly and was last verified
        // 2026-09-03 — three days before this was confirmed gone — so check
        // it separately before assuming it still reflects the real flow.
        potentialList.clickCreateNewRecoveryClaimBtn();
        CreateManualClaimPage createForm = new CreateManualClaimPage(claimantPage);
        assertThat(createForm.isLoaded()).as("Claimant should land on the Create Manual Claim page").isTrue();

        ManualClaimDetails details = buildSampleManualClaimDetails();
        createForm.enterAllRecoveryClaimsDetails(details);
        OpenRecoveryClaimsPage claimantClaim = createForm.createBtnClick();
        assertThat(claimantClaim.isLoaded())
                .as("Claimant should land on the Open Recovery Claim summary page after Create")
                .isTrue();
        // Mandatory check: every value that was actually SET on
        // manualClaimDetails must come back unchanged on the claim summary
        // page — see OpenRecoveryClaimsPage.validateAllDetailsOfOpenRecoveryClaimsPage() Javadoc.
        claimantClaim.validateAllDetailsOfOpenRecoveryClaimsPage(details);

        String serialNumber = claimantClaim.getClaimSerialNumber();
        OpenRecoveryClaimsPage atFaultClaim = atFaultDashboard.searchClaim(serialNumber);
        atFaultClaim.refresh();
        // Confirmed live 2026-09-04 (manual repro): "Respond" doesn't exist
        // yet at this stage — it only appears after the claim has been
        // accepted once via "Claim Details". fillAtFaultClaimNumber() blurs
        // and waits for its async validation call to settle before Accept
        // is clicked — clicking Accept while validation is still in flight
        // is silently blocked by a "Please wait for claim number validation
        // to complete." toast with no server mutation firing at all.
        atFaultClaim.clickClaimDetails();
        atFaultClaim.fillAtFaultClaimNumber(String.valueOf(RandomUniqueGenerator.generateRandom7Digit()));
        atFaultClaim.clickAcceptBtn();

        return new Stage1Result(claimantClaim, atFaultClaim, serialNumber, details);
    }

    /**
     * Stage 2, Normal Repair path only (MOSADAD_DOMAIN.md §Stage 2A) —
     * claimant submits a workshop repair quotation, at-fault accepts it.
     * Total Loss/Salvage and Negotiate were never built out in the original
     * draft this was recovered from; they're still genuinely untested, not
     * just disabled — see the other methods in QuotationTest.
     */
    static QuotationResult submitAndAcceptNormalRepairQuotation(Stage1Result stage1) {
        OpenRecoveryClaimsPage claimantClaim = stage1.claimantClaim();
        OpenRecoveryClaimsPage atFaultClaim = stage1.atFaultClaim();

        claimantClaim.refresh();
        claimantClaim.clickCreateQuotation();
        claimantClaim.selectQuotationType(OpenRecoveryClaimsPage.REPAIR_QUOTATION);
        claimantClaim.fillWorkShopName("Al Tharaa Auto Mech Rep W Shop");
        claimantClaim.fillWorkShopLocation("Abu Dhabi Workshop Location");
        String quotationNumber = claimantClaim.getQuotationNumber();
        String repairCost = "5000";
        claimantClaim.fillRepairDurationDays("5");
        claimantClaim.fillRepairCost(repairCost);
        claimantClaim.clickUploadClaimDocuments("target/classes/RecoveryClaimScreenshot.png");
        claimantClaim.clickSubmitBtn();

        // clickRespondBtn() retries refresh() internally until Respond
        // actually appears — confirmed live 2026-09-04, see its Javadoc.
        atFaultClaim.refresh();
        atFaultClaim.clickRespondBtn();
        atFaultClaim.clickAcceptBtn();

        return new QuotationResult(quotationNumber, repairCost);
    }

    /**
     * Stage 3 (MOSADAD_DOMAIN.md §Stage 3) — claimant submits the final
     * invoice, at-fault accepts it. Negotiate was never built out in the
     * original draft; still genuinely untested — see InvoiceTest's other
     * methods.
     */
    static String submitAndAcceptInvoice(Stage1Result stage1, String repairCost) {
        OpenRecoveryClaimsPage claimantClaim = stage1.claimantClaim();
        OpenRecoveryClaimsPage atFaultClaim = stage1.atFaultClaim();

        claimantClaim.refresh();
        claimantClaim.scrollToCreateInvoiceBtn();
        claimantClaim.clickCreateInvoice();
        String invoiceNumber = String.valueOf(RandomUniqueGenerator.generateRandom7Digit());
        // fillInvoiceForm() fills Invoice Amount, the document, and Invoice
        // Number together and retries the whole set if the form's
        // re-render wipes one of them mid-fill — confirmed live 2026-09-04,
        // filling them as independent steps is unreliable regardless of
        // order. Invoice Amount must equal the accepted quotation's repair
        // cost exactly or the server rejects it.
        claimantClaim.fillInvoiceForm(invoiceNumber, repairCost, "target/classes/RecoveryClaimScreenshot.png");
        claimantClaim.clickSubmitBtn();

        atFaultClaim.refresh();
        atFaultClaim.clickRespondBtn();
        atFaultClaim.clickAcceptBtn();

        return invoiceNumber;
    }

    /**
     * Stage 4 (MOSADAD_DOMAIN.md §Stage 4) — at-fault requests a credit
     * note, bulk-settles the claim, and pays it through a real ATB Pay
     * checkout (RAK Bank's gateway — not a sandbox: this executes an
     * actual settlement payment against the entity's wallet). Claimant
     * then finalizes the claim as Closed.
     *
     * The diagnostic logging the original draft had wrapped around the
     * checkout click (popup/console/response capture) was stripped here —
     * it was mid-debug instrumentation, not meant to stay — but that
     * concentration of diagnostics is itself a signal: the checkout step
     * specifically was still being actively debugged when work on this
     * stopped, more than the stages before it. Expect this one to need
     * rechecking before it's trusted.
     */
    static void requestSettleAndCloseClaim(Stage1Result stage1) {
        OpenRecoveryClaimsPage claimantClaim = stage1.claimantClaim();
        OpenRecoveryClaimsPage atFaultClaim = stage1.atFaultClaim();
        String serialNumber = stage1.serialNumber();

        atFaultClaim.clickRequestCreditNote();
        atFaultClaim.selectClaimForSettlement(serialNumber);
        assertThat(atFaultClaim.getNumberOfClaimsSelected())
                .as("Number Of Claims Selected should reflect the one claim just checked")
                .isEqualTo("1");
        atFaultClaim.clickRequestCreditNoteToCreateBulk();
        atFaultClaim.selectClaimForSettlement(serialNumber);

        atFaultClaim.clickProceedToPayment();
        atFaultClaim.agreeToCheckoutTerms();
        atFaultClaim.clickCheckoutConfirm();
        assertThat(atFaultClaim.isPaymentSuccessful())
                .as("Settlement payment should redirect to payment-success")
                .isTrue();

        claimantClaim.refresh();
        claimantClaim.clickProceedToClaimClosure();
        claimantClaim.clickFinalizeClaim();
        claimantClaim.confirmClaimClosure();
        assertThat(claimantClaim.isClaimFinalized())
                .as("Claim should show Claim Finalized after closure is confirmed")
                .isTrue();
    }

    /**
     * Sample claim data. Lives here, not on ManualClaimDetails itself —
     * ManualClaimDetails is a plain, reusable data holder (see its own
     * Javadoc), and baking one fixture's specific values into it would
     * make every other caller (OpenRecoveryClaimsPage included) carry
     * test-only knowledge it has no need for.
     */
    private static ManualClaimDetails buildSampleManualClaimDetails() {
        // Every vehicle identifier below (plate/chassis/policy, both sides)
        // must be unique per run, not just the claim number — confirmed
        // live 2026-09-04: reusing the same claimant vehicle across many
        // successive test runs made the app route Create to an EXISTING
        // claim tied to that vehicle instead of the freshly submitted one
        // (identical accident date/time came back across runs with
        // different submitted values). Same root cause already confirmed
        // for the Non-Faulty Details card duplication — see
        // OpenRecoveryClaimsPage's class Javadoc.
        long unique = System.currentTimeMillis();

        ManualClaimDetails manualClaimDetails = new ManualClaimDetails();
        manualClaimDetails.setReportNumber(String.valueOf(unique));
        manualClaimDetails.setAccidentDateToday(true);
        // input[type=time].fill() needs 24h "HH:mm" — "1 pm" doesn't fill.
        // The accident date is always today (setAccidentDateToday above), so
        // any hardcoded time-of-day is in the future during part of the day
        // and in the past during the rest — confirmed live: a hardcoded
        // "01:00" passed when this was first tested in the afternoon and
        // failed with "Accident Date & Time cannot be in the future" when
        // run again just after midnight. Using the actual current time
        // (truncated to the minute) is always <= the real time by the
        // moment this reaches the server, so it's never in the future
        // regardless of when the test runs.
        manualClaimDetails.setAccidentTime(
                java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
        manualClaimDetails.setAccidentType("Vehicle Collision");
        manualClaimDetails.setEmirate("Abu Dhabi");
        manualClaimDetails.setCity("Abu Dhabi");
        manualClaimDetails.setArea("Sayh Al Shuaib");
        manualClaimDetails.setIntersection("Collision");
        manualClaimDetails.setStreet("Abu Dhabi 123");
        manualClaimDetails.setReportDateToday(true);
        manualClaimDetails.setAccidentDescription("Due to Collision");
        // Must be unique per accident — the API rejects a reused claim number
        // (confirmed live), so a hardcoded value only works on the first run.
        manualClaimDetails.setClaimantClaimNumber("CLM" + unique);
        manualClaimDetails.setClaimantPlateSource("ABU DHABI");
        manualClaimDetails.setClaimantPlateColor("WHITE");
        manualClaimDetails.setClaimantPlateNumber(String.valueOf(unique).substring(6));
        manualClaimDetails.setClaimantPolicyNumber("1" + unique);
        manualClaimDetails.setClaimantPolicyExpiryDateToday(true);
        manualClaimDetails.setClaimantPolicyType("Comprehensive");
        manualClaimDetails.setClaimantChassisNumber("11" + unique + "11");
        manualClaimDetails.setClaimantVehicleMake("OTOYOL");
        manualClaimDetails.setClaimantVehicleModel("ACADIA");
        manualClaimDetails.setClaimantVehicleYear("2010");
        manualClaimDetails.setClaimantDamagedParts("The Right Front Corner");
        manualClaimDetails.setAtFaultInsuranceCompany("DUBAI NATIONAL INSURANCE and REINSURANCE");
        manualClaimDetails.setAtFaultPlateSource("ABU DHABI");
        manualClaimDetails.setAtFaultPlateColor("WHITE");
        manualClaimDetails.setAtFaultPlateNumber(String.valueOf(unique + 1).substring(6));
        manualClaimDetails.setAtFaultChassisNumber("22" + unique + "22");
        manualClaimDetails.setAtFaultPolicyNumber("2" + unique);
        manualClaimDetails.setAtFaultPolicyExpiryDateToday(true);
        manualClaimDetails.setAtFaultPolicyType("Comprehensive");
        manualClaimDetails.setAtFaultVehicleMake("Stewart");
        manualClaimDetails.setAtFaultVehicleModel("ACADIA");
        manualClaimDetails.setAtFaultVehicleYear("2018");
        manualClaimDetails.setAtFaultDamagedParts("The Front Right Door");
        return manualClaimDetails;
    }
}
