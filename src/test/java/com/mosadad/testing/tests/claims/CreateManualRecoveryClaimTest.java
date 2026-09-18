package com.mosadad.testing.tests.claims;

import com.microsoft.playwright.Page;
import com.mosadad.testing.base.BaseUiTest;
import com.mosadad.testing.browser.ActorPages;
import com.mosadad.testing.browser.TwoActorPlaywrightManager;
import com.mosadad.testing.config.ConfigManager;
import com.mosadad.testing.pages.DashboardPage;
import com.mosadad.testing.pages.LoginPage;
import com.mosadad.testing.pages.RecoveryClaimsHubPage;
import com.mosadad.testing.pages.claims.CreateManualRecoveryClaimPage;
import com.mosadad.testing.pages.claims.PotentialRecoveryClaimsListPage;
import com.mosadad.testing.pages.claims.WalletPage;
import io.qameta.allure.Description;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.mosadad.testing.browser.TwoActorPlaywrightManager.closeTwoBrowsers;
import static org.assertj.core.api.Assertions.assertThat;

public class CreateManualRecoveryClaimTest extends BaseUiTest {

    private Page claimantPage;
    private Page atFaultPage;

    @BeforeMethod(alwaysRun = true)
    public void launchTwoBrowsers() {
        ActorPages pages = TwoActorPlaywrightManager.openTwoBrowsers("chrome", "safari");
        claimantPage = pages.getClaimantPage();
        atFaultPage = pages.getAtFaultPage();
        log.info("Launched claimant (Chrome) + at-fault (WebKit) sessions");
    }

    @Test(enabled = false, groups = {"stage", "manual claim"})
    @Description("Creating Manual Claim for Recovery inside claimant user")
    public void createManualClaim() {
        claimantPage.navigate(ConfigManager.getLoginUrl("stage"));
        DashboardPage claimantDashboard = new LoginPage(claimantPage).login(
                ConfigManager.getEmail("stage", "dubai"),
                ConfigManager.getPassword("stage", "dubai")
        );
        assertThat(claimantDashboard.isLoaded())
                .as("Claimant (Chrome, Dubai account) should land on the dashboard")
                .isTrue();

        WalletPage walletPageOfClaimant = claimantDashboard.openWallet();
        Double walletBalanceOfClaimant = walletPageOfClaimant.getBalanceAmount();

        // At-fault — WebKit/Safari engine, DNL STAGE account.
        atFaultPage.navigate(ConfigManager.getLoginUrl("stage"));
        LoginPage atFaultLogin = new LoginPage(atFaultPage);
        DashboardPage atFaultDashboard = atFaultLogin.login(
                ConfigManager.getEmail("stage", "dnl"),
                ConfigManager.getPassword("stage", "dnl")
        );
        String atFaultFailureReason = atFaultLogin.isErrorToastVisible()
                ? " — app said: \"" + atFaultLogin.getErrorToastMessage() + "\""
                : "";
        assertThat(atFaultDashboard.isLoaded())
                .as("At-fault (WebKit, DNL account) should land on the dashboard%s", atFaultFailureReason)
                .isTrue();

        assertThat(claimantDashboard.isEntityModulesSidebarVisible())
                .as("Claimant's session must remain unaffected by the at-fault actor logging in")
                .isTrue();

        WalletPage walletPageOfAtFault = claimantDashboard.openWallet();
        Double walletBalanceOfAtFault = walletPageOfAtFault.getBalanceAmount();

        closeTwoBrowsers();
        launchTwoBrowsers();

        claimantPage.navigate(ConfigManager.getLoginUrl("stage"));
        claimantDashboard = new LoginPage(claimantPage).login(
                ConfigManager.getEmail("stage", "dubai"),
                ConfigManager.getPassword("stage", "dubai")
        );
        assertThat(claimantDashboard.isLoaded())
                .as("Claimant (Chrome, Dubai account) should land on the dashboard")
                .isTrue();

        atFaultPage.navigate(ConfigManager.getLoginUrl("stage"));
        atFaultLogin = new LoginPage(atFaultPage);
        atFaultDashboard = atFaultLogin.login(
                ConfigManager.getEmail("stage", "dnl"),
                ConfigManager.getPassword("stage", "dnl")
        );
        atFaultFailureReason = atFaultLogin.isErrorToastVisible()
                ? " — app said: \"" + atFaultLogin.getErrorToastMessage() + "\""
                : "";
        assertThat(atFaultDashboard.isLoaded())
                .as("At-fault (WebKit, DNL account) should land on the dashboard%s", atFaultFailureReason)
                .isTrue();

        RecoveryClaimsHubPage recoveryClaimsHubPage = claimantDashboard.openRecoveryClaims();
        PotentialRecoveryClaimsListPage potentialRecoveryClaimsListPage = recoveryClaimsHubPage.openPotentialRecoveryClaimsListPage();
        assertThat(potentialRecoveryClaimsListPage.isLoaded())
                .as("Potential Recovery Claims List screen should load")
                .isTrue();
        CreateManualRecoveryClaimPage createManualRecoveryClaimPage = potentialRecoveryClaimsListPage.clickCreateNewRecoveryClaimBtn();
        createManualRecoveryClaimPage.selectReportProvider("Rafid");
        createManualRecoveryClaimPage.uploadPoliceReport("Fast Track333.xlsx");
        createManualRecoveryClaimPage.submitPoliceReportStep();
    }

    public void closeTwoBrowsers() {
        String tracePathPrefix = "target/traces/" + getClass().getSimpleName() + "-"
                + DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS").format(LocalDateTime.now());
        TwoActorPlaywrightManager.closeTwoBrowsers(tracePathPrefix);
    }

}
