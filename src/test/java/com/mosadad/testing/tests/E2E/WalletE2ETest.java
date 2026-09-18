package com.mosadad.testing.tests.E2E;

import com.microsoft.playwright.options.AriaRole;
import com.mosadad.testing.api.WalletApiClient;
import com.mosadad.testing.base.BaseTest;
import com.mosadad.testing.browser.ActorPages;
import com.mosadad.testing.browser.TwoActorPlaywrightManager;
import com.mosadad.testing.config.ConfigManager;
import com.mosadad.testing.pages.DashboardPage;
import com.mosadad.testing.pages.LoginPage;
import com.mosadad.testing.pages.RecoveryClaimsHubPage;
import com.mosadad.testing.pages.claims.*;
import com.mosadad.testing.utils.RandomUniqueGenerator;
import com.microsoft.playwright.Page;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

/**
 * Two-actor, two-browser-engine E2E: one insurer submits an action, a
 * second insurer (a separate account, separate browser) receives and acts
 * on it. See MOSADAD_DOMAIN.md §Wallet / §9.2 Digital Settlement Flow.
 *
 * Browser lifecycle is TwoActorPlaywrightManager, not BaseUiTest/
 * PlaywrightManager: that lifecycle holds exactly one Playwright session
 * per thread, right for every single-actor test in this suite but wrong
 * here — this needs two independent, concurrently-logged-in sessions in
 * one test method. TwoActorPlaywrightManager is the reusable version of
 * that: any future claimant/at-fault test can call it the same way instead
 * of duplicating the launch/teardown code.
 *
 * Login credentials: Dubai QA account is confirmed live. The Mehtaq QA
 * account (qa.claimant.email.mehtaq / qa.claimant.password.mehtaq in
 * credentials.properties) was checked live on 2026-09-01 and the app
 * rejects it with a real "Wrong Username or Password." toast — that's a
 * genuinely wrong credential, not a framework bug. This test will keep
 * failing on the at-fault actor until credentials.properties has a real
 * second account; swap in a different one if you have it.
 *
 * The actual cross-actor hand-off (claimant submits -> at-fault sees +
 * acts, wallet updates) is TODO: it depends on the Create Claim / Wallet
 * screens, which are still unverified placeholders — see WalletPage and
 * FRAMEWORK.md.
 */
@Epic("Mosadad Recovery Claim")
@Feature("Wallet / Settlement — Cross-Actor E2E")
public class WalletE2ETest extends BaseTest {

    private Page claimantPage;
    private Page atFaultPage;

    @BeforeMethod(alwaysRun = true)
    public void launchTwoBrowsers() {
        ActorPages pages = TwoActorPlaywrightManager.openTwoBrowsers("chrome", "chrome");
        claimantPage = pages.getClaimantPage();
        atFaultPage = pages.getAtFaultPage();
        log.info("Launched claimant (Chrome) + at-fault (WebKit) sessions");
    }

    /**
     * Sample claim data for twoActorsCanBeLoggedInSimultaneouslyOnDifferentBrowsers().
     * Lives here, not on ManualClaimDetails itself — ManualClaimDetails is a
     * plain, reusable data holder (see its own Javadoc), and baking one
     * test's specific fixture values into it would make every other caller
     * (OpenRecoveryClaimsPage included) carry test-only knowledge it has no
     * need for.
     */
    private ManualClaimDetails buildSampleManualClaimDetails() {
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

//     @Test(groups = {"e2e", "wallet"})
//     @Severity(SeverityLevel.CRITICAL)
//     @Description("Two different insurer accounts can be logged in at the same time, on two different browser engines (Chrome + WebKit), without either session interfering with the other.")
//     public void twoActorsCanBeLoggedInSimultaneouslyOnDifferentBrowsers() {
//         // Claimant — real Chrome, Dubai STAGE account.
//         /*claimantPage.navigate(ConfigManager.getLoginUrl("stage"));
//         DashboardPage claimantDashboardPage = new LoginPage(claimantPage).login(
//                 ConfigManager.getEmail("stage", "dubai"),
//                 ConfigManager.getPassword("stage", "dubai")
//         );
//         assertThat(claimantDashboardPage.isLoaded())
//                 .as("Claimant (Chrome, Dubai account) should land on the dashboard")
//                 .isTrue();

//         // At-fault — WebKit/Safari engine, DNL STAGE account.
//         atFaultPage.navigate(ConfigManager.getLoginUrl("stage"));
//         LoginPage atFaultLogin = new LoginPage(atFaultPage);
//         DashboardPage atFaultDashboardPage = atFaultLogin.login(
//                 ConfigManager.getEmail("stage", "dnl"),
//                 ConfigManager.getPassword("stage", "dnl")
//         );
//         String atFaultFailureReason = atFaultLogin.isErrorToastVisible()
//                 ? " — app said: \"" + atFaultLogin.getErrorToastMessage() + "\""
//                 : "";
//         assertThat(atFaultDashboardPage.isLoaded())
//                 .as("At-fault (WebKit, DNL account) should land on the dashboard%s", atFaultFailureReason)
//                 .isTrue();

//         // Both sessions are independent BrowserContexts (separate cookies/
//         // storage) on separate browser engines — prove neither leaked into
//         // the other by re-checking the claimant's session is still intact
//         // after the at-fault actor logged in. Must run before either actor's
//         // openWallet() below — that click redirects the same tab cross-domain
//         // to a third-party portal (rak.atbpay.me, confirmed live — see
//         // DashboardPage.openWallet() Javadoc), which has no Mosadad sidebar
//         // at all, so checking after would fail regardless of the at-fault
//         // actor and wasn't actually testing session isolation.
//         assertThat(claimantDashboardPage.isEntityModulesSidebarVisible())
//                 .as("Claimant's session must remain unaffected by the at-fault actor logging in")
//                 .isTrue();

//         WalletPage walletPageOfClaimant = claimantDashboardPage.openWallet();
//         Double walletBalanceOfClaimant = walletPageOfClaimant.getBalanceAmount();

//         WalletPage walletPageOfAtFault = atFaultDashboardPage.openWallet();
//         Double walletBalanceOfAtFault = walletPageOfAtFault.getBalanceAmount();

//         closeTwoBrowsers();
//         launchTwoBrowsers();*/

//         // Opening both of the browser again-
//         claimantPage.navigate(ConfigManager.getLoginUrl("stage"));
//         DashboardPage claimantDashboardPage = new LoginPage(claimantPage).login(
//                 ConfigManager.getEmail("stage", "dubai"),
//                 ConfigManager.getPassword("stage", "dubai")
//         );
//         assertThat(claimantDashboardPage.isLoaded())
//                 .as("Claimant (Chrome, Dubai account) should land on the dashboard")
//                 .isTrue();

//         atFaultPage.navigate(ConfigManager.getLoginUrl("stage"));
//         LoginPage atFaultLogin = new LoginPage(atFaultPage);
//         DashboardPage atFaultDashboardPage = atFaultLogin.login(
//                 ConfigManager.getEmail("stage", "dnl"),
//                 ConfigManager.getPassword("stage", "dnl")
//         );
//         String atFaultFailureReason = atFaultLogin.isErrorToastVisible()
//                 ? " — app said: \"" + atFaultLogin.getErrorToastMessage() + "\""
//                 : "";
//         assertThat(atFaultDashboardPage.isLoaded())
//                 .as("At-fault (WebKit, DNL account) should land on the dashboard%s", atFaultFailureReason)
//                 .isTrue();

//         // The default Playwright viewport (1280x720) is too short for this
//         // form — confirmed live 2026-09-03: a mat-datepicker overlay opened
//         // near the bottom of the page renders partly off-screen and Playwright
//         // refuses to click into it ("element is outside of the viewport").
//         claimantPage.setViewportSize(1600, 2200);

//         RecoveryClaimsHubPage recoveryClaimsHubPage = claimantDashboardPage.openRecoveryClaims();
//         PotentialRecoveryClaimsListPage potentialRecoveryClaimsListPage = recoveryClaimsHubPage.openPotentialRecoveryClaimsListPage();
//         assertThat(potentialRecoveryClaimsListPage.isLoaded())
//                 .as("Potential Recovery Claims List screen should load")
//                 .isTrue();
//         // Confirmed live 2026-09-07: "Create New Recovery Claim" now lands
//         // directly on the Create Manual Claim form — the separate
//         // police-report-provider gate screen (select provider, upload
//         // report, submit, "continue processing" popup) that used to
//         // precede it is gone entirely, not just occasionally slow to
//         // render. Both pages shared the same route
//         // (Routes.CREATE_MANUAL_RECOVERY_CLAIMS / CREATE_MANUAL_CLAIMS are
//         // both "/entity-portal/manual-claim") even before this change, so
//         // wrapping the same claimantPage in CreateManualClaimPage directly
//         // works without touching PotentialRecoveryClaimsListPage or
//         // CreateManualRecoveryClaimPage themselves — CreateManualRecoveryClaimTest
//         // still exercises that gate directly and shouldn't be assumed
//         // broken without checking it separately.
//         potentialRecoveryClaimsListPage.clickCreateNewRecoveryClaimBtn();
//         CreateManualClaimPage createManualClaimPage = new CreateManualClaimPage(claimantPage);
//         assertThat(createManualClaimPage.isLoaded()).as("Claimant should land on the Create Manual Claim Page").isTrue();

//         //Setting all the details from
//         ManualClaimDetails manualClaimDetails = buildSampleManualClaimDetails();

//         createManualClaimPage.enterAllRecoveryClaimsDetails(manualClaimDetails);
//         OpenRecoveryClaimsPage openRecoveryClaimsPage = createManualClaimPage.createBtnClick();
//         assertThat(openRecoveryClaimsPage.isLoaded())
//                 .as("Claimant should land on the Open Recovery Claim summary page after Create")
//                 .isTrue();

//         // Mandatory check: every value that was actually SET on
//         // manualClaimDetails must come back unchanged on the claim summary
//         // page — see OpenRecoveryClaimsPage.validateAllDetailsOfOpenRecoveryClaimsPage() Javadoc.
//         openRecoveryClaimsPage.validateAllDetailsOfOpenRecoveryClaimsPage(manualClaimDetails);
//         String serialNumber = openRecoveryClaimsPage.getClaimSerialNumber();
//         OpenRecoveryClaimsPage atFaultopenRecoveryClaimsPage= atFaultDashboardPage.searchClaim(serialNumber);
//         atFaultopenRecoveryClaimsPage.refresh();
//         // Confirmed live 2026-09-04 (manual browser repro): "Respond" does
//         // not exist yet at this stage — it only appears after the claim has
//         // been accepted once via "Claim Details". fillAtFaultClaimNumber()
//         // now blurs and waits for its async validation call to settle
//         // before Accept is clicked — clicking Accept while that validation
//         // is still in flight is silently blocked by a "Please wait for
//         // claim number validation to complete." toast with no server
//         // mutation firing at all, which was the real root cause of Accept
//         // never actually accepting the claim.
//         atFaultopenRecoveryClaimsPage.clickClaimDetails();
//         atFaultopenRecoveryClaimsPage.fillAtFaultClaimNumber(String.valueOf(RandomUniqueGenerator.generateRandom7Digit()));
//         atFaultopenRecoveryClaimsPage.clickAcceptBtn();

//         openRecoveryClaimsPage.refresh();
//         openRecoveryClaimsPage.clickCreateQuotation();
//         openRecoveryClaimsPage.selectQuotationType(OpenRecoveryClaimsPage.REPAIR_QUOTATION);
//         openRecoveryClaimsPage.fillWorkShopName("Al Tharaa Auto Mech Rep W Shop");
//         openRecoveryClaimsPage.fillWorkShopLocation("Abu Dhabi Workshop Location");
//         String quotationNumber = openRecoveryClaimsPage.getQuotationNumber();
//         String repairCost = "5000";
//         openRecoveryClaimsPage.fillRepairDurationDays("5");
//         openRecoveryClaimsPage.fillRepairCost(repairCost);
//         openRecoveryClaimsPage.clickUploadClaimDocuments("target/classes/RecoveryClaimScreenshot.png");
//         openRecoveryClaimsPage.clickSubmitBtn();

//         atFaultopenRecoveryClaimsPage.refresh();
//         atFaultopenRecoveryClaimsPage.clickRespondBtn();
//         atFaultopenRecoveryClaimsPage.clickAcceptBtn();

//         openRecoveryClaimsPage.refresh();
//         openRecoveryClaimsPage.scrollToCreateInvoiceBtn();
//         openRecoveryClaimsPage.clickCreateInvoice();
//         // Confirmed live 2026-09-04: filling Invoice Amount, uploading the
//         // document, and filling Invoice Number as independent steps is
//         // unreliable regardless of order — the form re-renders while
//         // settling and silently wipes whichever field was filled earliest.
//         // fillInvoiceForm() fills all three, verifies all three stuck, and
//         // retries the whole set if not. Invoice Amount must match the
//         // accepted quotation's repair cost exactly or the server rejects
//         // it — confirmed via the invoice API's /Initial response returning
//         // repairCost as the required totalCost.
//         openRecoveryClaimsPage.fillInvoiceForm(
//                 String.valueOf(RandomUniqueGenerator.generateRandom7Digit()),
//                 repairCost,
//                 "target/classes/RecoveryClaimScreenshot.png");
//         openRecoveryClaimsPage.clickSubmitBtn();

//         // Now the at-fault side accepts the invoice itself. clickRespondBtn()
//         // retries refresh() internally until Respond actually appears — see
//         // its Javadoc for why a single refresh isn't reliable here.
//         atFaultopenRecoveryClaimsPage.refresh();
//         atFaultopenRecoveryClaimsPage.clickRespondBtn();
//         atFaultopenRecoveryClaimsPage.clickAcceptBtn();

//         // Settlement: at-fault side requests a credit note for the accepted
//         // invoice, selects this claim in the Bulk Settlement Payment
//         // picker, creates the credit note bulk record, then proceeds to
//         // pay it via the external ATB wallet checkout. Confirmed live
//         // 2026-09-04 — see OpenRecoveryClaimsPage's Javadoc on each method
//         // for the exact page/route each step lands on.
//         atFaultopenRecoveryClaimsPage.clickRequestCreditNote();
//         atFaultopenRecoveryClaimsPage.selectClaimForSettlement(serialNumber);
//         assertThat(atFaultopenRecoveryClaimsPage.getNumberOfClaimsSelected())
//                 .as("Number Of Claims Selected should reflect the one claim just checked")
//                 .isEqualTo("1");
//         atFaultopenRecoveryClaimsPage.clickRequestCreditNoteToCreateBulk();
//         log.info("=== DIAGNOSTIC: URL on credit-note-bulk page = {}", atFaultPage.url());
//         atFaultopenRecoveryClaimsPage.selectClaimForSettlement(serialNumber);
//         log.info("=== DIAGNOSTIC: Number Of Claims Selected on credit-note-bulk page = {}",
//                 atFaultopenRecoveryClaimsPage.getNumberOfClaimsSelected());
//         log.info("=== DIAGNOSTIC: Proceed To Payment button outerHTML = {}", atFaultPage.evaluate(
//                 "Array.from(document.querySelectorAll('button')).find(b => b.innerText.trim() === 'Proceed To Payment') ? Array.from(document.querySelectorAll('button')).find(b => b.innerText.trim() === 'Proceed To Payment').outerHTML : 'NOT FOUND'"
//         ));
//         java.util.List<Page> popups = new java.util.concurrent.CopyOnWriteArrayList<>();
//         atFaultPage.onPopup(popups::add);
//         java.util.List<String> checkoutDiagnostics = new java.util.concurrent.CopyOnWriteArrayList<>();
//         atFaultPage.onConsoleMessage(msg -> checkoutDiagnostics.add("CONSOLE " + msg.type() + ": " + msg.text()));
//         atFaultPage.onPageError(err -> checkoutDiagnostics.add("PAGE ERROR: " + err));
//         atFaultPage.onResponse(resp -> {
//             if (resp.url().contains("atbpay.me") && resp.status() >= 400) {
//                 checkoutDiagnostics.add("FAILED RESPONSE " + resp.status() + " " + resp.url());
//             }
//         });
//         try {
//             atFaultopenRecoveryClaimsPage.clickProceedToPayment();
//         } catch (com.microsoft.playwright.PlaywrightException e) {
//             log.warn("clickProceedToPayment threw: {}", e.getMessage());
//         }
//         log.info("=== DIAGNOSTIC: checkout console/page-error/failed-response log = {}", checkoutDiagnostics);
//         log.info("=== DIAGNOSTIC: popups opened = {}", popups.size());
//         for (Page p : popups) {
//             log.info("=== DIAGNOSTIC: popup URL = {}", p.url());
//         }
//         log.info("=== DIAGNOSTIC: all context pages = {}", atFaultPage.context().pages().size());
//         for (Page p : atFaultPage.context().pages()) {
//             log.info("=== DIAGNOSTIC: context page URL = {}", p.url());
//         }
//         log.info("=== DIAGNOSTIC: URL right after clickProceedToPayment = {}", atFaultPage.url());
//         log.info("=== DIAGNOSTIC: body text snippet after clickProceedToPayment = {}",
//                 ((String) atFaultPage.evaluate("document.body.innerText")).substring(0, Math.min(500,
//                         ((String) atFaultPage.evaluate("document.body.innerText")).length())));

//         // Checkout page — a real external payment gateway (RAK Bank's ATB
//         // Pay), not a sandbox. Confirming here executes an actual
//         // settlement payment against the entity's wallet. Only done here
//         // because the user explicitly authorized it after being told this
//         // isn't something automated by default — see this test's class
//         // Javadoc.
//         log.info("=== DIAGNOSTIC: checkout body text = {}", atFaultPage.locator("body").innerText());
//         String checkoutAccountNumber = atFaultopenRecoveryClaimsPage.getCheckoutAccountNumber();
//         String checkoutBalance = atFaultopenRecoveryClaimsPage.getCheckoutBalance();
//         log.info("Checkout account {} balance {} before settlement payment", checkoutAccountNumber, checkoutBalance);
//         atFaultopenRecoveryClaimsPage.agreeToCheckoutTerms();
//         atFaultopenRecoveryClaimsPage.clickCheckoutConfirm();
//         assertThat(atFaultopenRecoveryClaimsPage.isPaymentSuccessful())
//                 .as("Settlement payment should redirect to payment-success")
//                 .isTrue();

//         // Claim closure: claimant refreshes and finalizes the claim once
//         // settlement is complete.
//         openRecoveryClaimsPage.refresh();
//         openRecoveryClaimsPage.clickProceedToClaimClosure();
//         openRecoveryClaimsPage.clickFinalizeClaim();
//         openRecoveryClaimsPage.confirmClaimClosure();
//         assertThat(openRecoveryClaimsPage.isClaimFinalized())
//                 .as("Claim should show Claim Finalized after closure is confirmed")
//                 .isTrue();
//     }

//     @Test(groups = {"e2e", "wallet"})
//     @Severity(SeverityLevel.CRITICAL)
//     @Description("Claimant's My Wallets screen (balance, wallet number, owner name) must match the wallet portal's own API — catches the UI silently showing stale/wrong figures. My Wallets is a third-party redirect (ATB Pay, rak.atbpay.me), not a Mosadad screen — see WalletPage/WalletApiClient.")
//     public void claimantWalletUiMatchesWalletApi() {
//         claimantPage.navigate(ConfigManager.getLoginUrl("stage"));
//         DashboardPage dashboard = new LoginPage(claimantPage).login(
//                 ConfigManager.getEmail("stage", "dubai"),
//                 ConfigManager.getPassword("stage", "dubai")
//         );
//         assertThat(dashboard.isLoaded()).as("Claimant should land on the dashboard").isTrue();

//         WalletPage walletPage = dashboard.openWallet();
//         assertThat(walletPage.isLoaded()).as("My Wallets should open the ATB Pay wallet portal").isTrue();
//         // Snapshot the UI values first, immediately after load, then call the
//         // API — minimizes (but on a live ledger can't fully eliminate) the
//         // timing gap between the two reads. Verified live 2026-09-02: this
//         // wallet's balance ticks from background transactions even a few
//         // seconds apart, so balance uses a tolerance below; wallet number and
//         // owner name are stable identity fields and are asserted exactly.
//         long uiWalletNumber = walletPage.getWalletNumber();
//         double uiBalance = walletPage.getBalanceAmount();
//         String uiOwnerName = walletPage.getOwnerName();

//         String accessToken = walletPage.getAccessToken();
//         Response apiResponse = new WalletApiClient().getWalletParticipant(accessToken);
//         assertThat(apiResponse.statusCode()).as("Wallet API should return 200").isEqualTo(200);

//         long apiWalletNumber = apiResponse.jsonPath().getLong("result.items[0].walletNumber");
//         double apiBalance = apiResponse.jsonPath().getDouble("result.items[0].balance");
//         String apiOwnerName = apiResponse.jsonPath().getString("result.items[0].englishDescription");

//         assertThat(uiWalletNumber)
//                 .as("UI wallet number should match the wallet API")
//                 .isEqualTo(apiWalletNumber);
//         assertThat(uiBalance)
//                 .as("UI balance should match the wallet API (within AED 2 — this ledger ticks with live background transactions)")
//                 .isCloseTo(apiBalance, offset(2.0));
//         assertThat(uiOwnerName)
//                 .as("UI owner name should match the wallet API")
//                 .isEqualTo(apiOwnerName);

//     }

    //@AfterMethod(alwaysRun = true)
    public void closeTwoBrowsers() {
        String tracePathPrefix = "target/traces/" + getClass().getSimpleName() + "-"
                + DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS").format(LocalDateTime.now());
        TwoActorPlaywrightManager.closeTwoBrowsers(tracePathPrefix);
    }
}
