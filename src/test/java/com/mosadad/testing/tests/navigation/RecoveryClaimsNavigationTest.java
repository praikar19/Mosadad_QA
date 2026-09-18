package com.mosadad.testing.tests.navigation;

import com.mosadad.testing.base.BaseUiTest;
import com.mosadad.testing.constants.Routes;
import com.mosadad.testing.pages.DashboardPage;
import com.mosadad.testing.pages.RecoveryClaimsHubPage;
import io.qameta.allure.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verified live (2026-08-30): every route asserted here was actually
 * navigated to and its URL confirmed — none of these are guesses.
 */
@Epic("Mosadad Recovery Claim")
@Feature("Entity Navigation")
public class RecoveryClaimsNavigationTest extends BaseUiTest {

    private RecoveryClaimsHubPage hub;

    // Runs after BaseUiTest.launchBrowser() — TestNG always runs superclass
    // @BeforeMethod methods before subclass ones, so no explicit ordering needed.
    //
    // Uses the cached-session login (see BaseUiTest.loginWithCachedSession)
    // instead of driving the real login form: this method runs once per
    // @Test below (8x per suite run), and only navigation is under test
    // here, not login itself — LoginUiTest already covers that. The real
    // login now happens at most once for the whole run instead of 8 times.
    @BeforeMethod(alwaysRun = true)
    public void loginAndOpenHubForQAPlatform() {
        DashboardPage dashboard = loginWithCachedSession("qa", "dubai");
        hub = dashboard.openRecoveryClaims();
        assertThat(hub.isLoaded()).as("Recovery Claims hub should load").isTrue();
    }

    @Test(groups = {"ui", "navigation"})
    @Severity(SeverityLevel.NORMAL)
    @Description("Dashboard module — Claims Report link navigates to the financial statement report screen.")
    public void claimsReportLinkNavigatesCorrectly() {
        hub.openClaimsReport();
        assertThat(page.url()).contains(Routes.CLAIMS_REPORT);
    }

    @Test(groups = {"ui", "navigation"})
    @Severity(SeverityLevel.NORMAL)
    @Description("Dashboard module — SLA Violation link navigates correctly.")
    public void slaViolationLinkNavigatesCorrectly() {
        hub.openSlaViolation();
        assertThat(page.url()).contains(Routes.SLA_VIOLATION);
    }

    @Test(groups = {"ui", "navigation"})
    @Severity(SeverityLevel.NORMAL)
    @Description("Recovery Claim Records module — Potential Recovery Claims link navigates correctly.")
    public void potentialRecoveryClaimsLinkNavigatesCorrectly() {
        hub.openPotentialRecoveryClaimsListPage();
        assertThat(page.url()).contains(Routes.POTENTIAL_RECOVERY_CLAIMS);
    }

    @Test(groups = {"ui", "navigation"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("Recovery Claim Records module — Recovery Claims List link navigates correctly. Entry point for future Stage 1 claim-registration tests.")
    public void recoveryClaimsListLinkNavigatesCorrectly() {
        hub.openRecoveryClaimsList();
        assertThat(page.url()).contains(Routes.RECOVERY_CLAIMS_LIST);
    }

    @Test(groups = {"ui", "navigation"})
    @Severity(SeverityLevel.NORMAL)
    @Description("Recovery Claim Records module — Fast Track link navigates correctly.")
    public void fastTrackLinkNavigatesCorrectly() {
        hub.openFastTrack();
        assertThat(page.url()).contains(Routes.FAST_TRACK);
    }

    @Test(groups = {"ui", "navigation"})
    @Severity(SeverityLevel.NORMAL)
    @Description("Financial module — Bulk Settlement link navigates correctly.")
    public void bulkSettlementLinkNavigatesCorrectly() {
        hub.openBulkSettlement();
        assertThat(page.url()).contains(Routes.BULK_SETTLEMENT);
    }

    @Test(groups = {"ui", "navigation"})
    @Severity(SeverityLevel.NORMAL)
    @Description("Financial module — Due Amount link navigates correctly.")
    public void dueAmountLinkNavigatesCorrectly() {
        hub.openDueAmount();
        assertThat(page.url()).contains(Routes.DUE_AMOUNT);
    }

    @Test(groups = {"ui", "navigation"})
    @Severity(SeverityLevel.NORMAL)
    @Description("Financial module — Payment History link navigates correctly.")
    public void paymentHistoryLinkNavigatesCorrectly() {
        hub.openPaymentHistory();
        assertThat(page.url()).contains(Routes.PAYMENT_HISTORY);
    }
}
