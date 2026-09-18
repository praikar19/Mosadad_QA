package com.mosadad.testing.tests.api.settlement;

import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * settlement service — Purchase tag. The UAE PASS-backed wallet checkout
 * flow behind the real Settlement Rail (see MOSADAD_DOMAIN.md §Wallet,
 * Settlement Rail & UAE PASS) — "Purchase" here means checking out/settling
 * a batch of claims through the Mosadad Wallet, not buying goods. All of it
 * is real-money-adjacent, so only request validation is exercised live.
 */
@Epic("Mosadad Recovery Claim")
@Feature("Settlement API — Purchase")
public class PurchaseApiTest extends BaseApiTest {

    @Test(groups = {"api", "settlement", "purchase"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Purchase with an empty body fails model validation (HTTP 400), flagging ClaimIds, EntityId, Language, CallbackUrl, OtherEntityId, MerchantSiteUrl and UaePassAuthCode as required — no checkout is initiated.")
    public void createPurchaseWithEmptyBodyReturnsValidationError() {
        Response res = settlement().body(Map.of()).post("/Purchase");
        ApiAssertions.assertValidationProblem(res, "ClaimIds", "EntityId", "Language", "CallbackUrl", "OtherEntityId", "MerchantSiteUrl", "UaePassAuthCode");
    }

    @Test(groups = {"api", "settlement", "purchase"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Purchase/Status with an empty body fails model validation (HTTP 400): TransactionId is required.")
    public void getPurchaseStatusWithEmptyBodyReturnsValidationError() {
        Response res = settlement().body(Map.of()).post("/Purchase/Status");
        ApiAssertions.assertValidationProblem(res, "TransactionId");
    }

    @Test(groups = {"api", "settlement", "purchase"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Purchase/Settle/Mock with an empty body fails model validation (HTTP 400): ClaimIds is required — even the QA-only mock settlement path validates its input.")
    public void mockSettleWithEmptyBodyReturnsValidationError() {
        Response res = settlement().body(Map.of()).post("/Purchase/Settle/Mock");
        ApiAssertions.assertValidationProblem(res, "ClaimIds");
    }

    @Test(groups = {"api", "settlement", "purchase"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Purchase/Wallet/Redirection with an empty body fails model validation (HTTP 400), flagging EntityId, CallbackUrl, MerchantSiteUrl and UaePassAuthCode as required.")
    public void walletRedirectionWithEmptyBodyReturnsValidationError() {
        Response res = settlement().body(Map.of()).post("/Purchase/Wallet/Redirection");
        ApiAssertions.assertValidationProblem(res, "EntityId", "CallbackUrl", "MerchantSiteUrl", "UaePassAuthCode");
    }

    @Test(groups = {"api", "settlement", "purchase"})
    @Severity(SeverityLevel.MINOR)
    @Description("POST /Purchase/Wallet/Balance/{entityid} — confirmed live this route returns a bare HTTP 404 'Resource not found' for both a real counterpart entity and the logged-in entity's own id, on this QA environment (a different, unrouted-looking shape vs. every other 404 in this suite, which are HTTP-200 envelopes). Documents the current live behavior rather than an assumed contract.")
    public void getWalletBalanceReturns404OnThisEnvironment() {
        Response res = settlement().post("/Purchase/Wallet/Balance/" + entityId());
        ApiAssertions.assertStatusCode(res, 404, "HTTP status");
    }

    @Test(enabled = false, groups = {"api", "settlement", "purchase", "mutating", "financial"})
    @Severity(SeverityLevel.BLOCKER)
    @Description("STUB — initiates a real UAE PASS wallet checkout for real claims. Requires a real UaePassAuthCode from a live UAE PASS auth flow; never run with fabricated data.")
    public void createPurchaseWithValidPayloadReturns200() {
        Response res = settlement().body(Map.of("entityId", entityId())).post("/Purchase");
        ApiAssertions.assertEnvelopeSuccess(res);
    }

    @Test(enabled = false, groups = {"api", "settlement", "purchase", "mutating"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — the QA-only mock settlement path, but still writes real settlement state for the given ClaimIds. Enable only against disposable claims once QA confirms this path is safe to automate.")
    public void mockSettleWithValidPayloadReturns200() {
        Response res = settlement().body(Map.of("claimIds", java.util.List.of("placeholder-claim-id"))).post("/Purchase/Settle/Mock");
        ApiAssertions.assertEnvelopeSuccess(res);
    }
}
