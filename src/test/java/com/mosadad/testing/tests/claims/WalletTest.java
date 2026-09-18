package com.mosadad.testing.tests.claims;

import com.mosadad.testing.api.WalletApiClient;
import com.mosadad.testing.base.BaseTwoActorUiTest;
import com.mosadad.testing.config.ConfigManager;
import com.mosadad.testing.pages.DashboardPage;
import com.mosadad.testing.pages.LoginPage;
import com.mosadad.testing.pages.claims.WalletPage;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

public class WalletTest extends BaseTwoActorUiTest {

    @Test(groups = {"sanity", "wallet"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("Claimant's My Wallets screen (balance, wallet number, owner name) must match the wallet portal's own API — catches the UI silently showing stale/wrong figures. My Wallets is a third-party redirect (ATB Pay, rak.atbpay.me), not a Mosadad screen — see WalletPage/WalletApiClient.")
    public void claimantWalletUiMatchesWalletApi() {
        claimantPage.navigate(ConfigManager.getLoginUrl("stage"));
        DashboardPage dashboard = new LoginPage(claimantPage).login(
                ConfigManager.getEmail("stage", "dubai"),
                ConfigManager.getPassword("stage", "dubai")
        );
        assertThat(dashboard.isLoaded()).as("Claimant should land on the dashboard").isTrue();

        WalletPage walletPage = dashboard.openWallet();
        assertThat(walletPage.isLoaded()).as("My Wallets should open the ATB Pay wallet portal").isTrue();

        long uiWalletNumber = walletPage.getWalletNumber();
        double uiBalance = walletPage.getBalanceAmount();
        String uiOwnerName = walletPage.getOwnerName();

        String accessToken = walletPage.getAccessToken();
        Response apiResponse = new WalletApiClient().getWalletParticipant(accessToken);
        assertThat(apiResponse.statusCode()).as("Wallet API should return 200").isEqualTo(200);

        long apiWalletNumber = apiResponse.jsonPath().getLong("result.items[0].walletNumber");
        double apiBalance = apiResponse.jsonPath().getDouble("result.items[0].balance");
        String apiOwnerName = apiResponse.jsonPath().getString("result.items[0].englishDescription");

        assertThat(uiWalletNumber)
                .as("UI wallet number should match the wallet API")
                .isEqualTo(apiWalletNumber);
        assertThat(uiBalance)
                .as("UI balance should match the wallet API (within AED 2 — this ledger ticks with live background transactions)")
                .isCloseTo(apiBalance, offset(2.0));
        assertThat(uiOwnerName)
                .as("UI owner name should match the wallet API")
                .isEqualTo(apiOwnerName);
    }
}
