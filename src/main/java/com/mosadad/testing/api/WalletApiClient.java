package com.mosadad.testing.api;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static io.restassured.RestAssured.given;

/**
 * RestAssured client for the ATB Pay Wallet Portal API — a THIRD-PARTY
 * system, not part of Mosadad's own backend. Mosadad's "My Wallets" button
 * (DashboardPage.openWallet()) redirects the browser cross-domain to
 * rak.atbpay.me; this client talks to that system's own API directly.
 * Verified live 2026-09-02 as the Claimant Insurer (Dubai stage account).
 *
 * Auth is a separate Bearer JWT the wallet portal issues to itself after
 * the SSO redirect, stored in that page's own localStorage under
 * "access-token" — not the Mosadad session ApiClient uses. There is no
 * login call here; extract the token from the browser
 * (see WalletPage.getAccessToken()) and pass it in.
 */
public class WalletApiClient {

    private static final Logger log = LogManager.getLogger(WalletApiClient.class);
    private static final String BASE_URI = "https://rak.atbpay.me/API/PayPro.RAKEGA/WalletAPI/api";

    /**
     * GET /WalletParticipantAdmin — the logged-in participant's wallet:
     * walletNumber, balance, englishDescription (owner name), etc.
     * Response shape: {"result":{"items":[{...}], "totalCount":1, ...}}
     */
    public Response getWalletParticipant(String accessToken) {
        Response res = given()
                .baseUri(BASE_URI)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .queryParam("page", 0)
                .queryParam("id", 0)
                .queryParam("pageSize", 10)
                .queryParam("orderBy", "")
                .queryParam("descendingDirection", false)
                .queryParam("userId", 0)
                .get("/WalletParticipantAdmin");
        log.info("GET /WalletParticipantAdmin -> {}", res.statusCode());
        return res;
    }
}
