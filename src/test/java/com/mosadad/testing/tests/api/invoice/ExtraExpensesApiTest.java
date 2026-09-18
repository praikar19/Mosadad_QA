package com.mosadad.testing.tests.api.invoice;

import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * invoice service — ExtraExpenses tag. Ad-hoc expense line items attached
 * to a claim's invoice (towing, storage, etc.), each independently
 * negotiable between insurers.
 *
 * <p><b>Caution — {@code PUT /ExtraExpenses/HandleStatus} and
 * {@code PUT /ExtraExpenses/AcceptNegotiation} (both WITHOUT an id) are the
 * same dangerous unscoped-bulk pattern as
 * {@code QuotationApiTest}'s {@code AcceptAll}</b> — confirmed live an empty
 * body on either returns {@code "Accepted Successfully":true} rather than a
 * validation error. Their id-scoped siblings
 * ({@code PUT /ExtraExpenses/HandleStatus/{id}},
 * {@code POST /ExtraExpenses/{id}/AcceptNegotiation}) are safe and used
 * below instead. The two unscoped variants are never called by this suite,
 * not even for validation.
 */
@Epic("Mosadad Recovery Claim")
@Feature("Invoice API — ExtraExpenses")
public class ExtraExpensesApiTest extends BaseApiTest {

    @Test(groups = {"api", "invoice", "extra-expenses"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /ExtraExpenses/Claim/{claimId} for a real claim with no extra expenses returns a 'not found' envelope failure, not a 500.")
    public void getExtraExpensesByClaimIdForClaimWithNoneReturnsEnvelopeFailure() {
        Response res = invoice().get("/ExtraExpenses/Claim/6aab489ecb32564549128fa6");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "invoice", "extra-expenses"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /ExtraExpenses/{claimId}/ClaimId (the same lookup as Claim/{claimId}, reversed path shape) also returns a 'not found' envelope failure for a claim with no extra expenses.")
    public void getExtraExpensesClaimIdVariantReturnsEnvelopeFailure() {
        Response res = invoice().get("/ExtraExpenses/6aab489ecb32564549128fa6/ClaimId");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "invoice", "extra-expenses"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /ExtraExpenses/{id} for a non-existent id returns a 'not found' envelope failure.")
    public void getExtraExpenseByNonExistentIdReturnsEnvelopeFailure() {
        Response res = invoice().get("/ExtraExpenses/000000000000000000000000");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "invoice", "extra-expenses"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /ExtraExpenses/{id}/NegotiationHistory for a non-existent id returns a 'not found' envelope failure.")
    public void getNegotiationHistoryForNonExistentIdReturnsEnvelopeFailure() {
        Response res = invoice().get("/ExtraExpenses/000000000000000000000000/NegotiationHistory");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "invoice", "extra-expenses"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /ExtraExpenses/{id}/GetLatestNegotiation for a non-existent id returns a 'not found' envelope failure.")
    public void getLatestNegotiationForNonExistentIdReturnsEnvelopeFailure() {
        Response res = invoice().get("/ExtraExpenses/000000000000000000000000/GetLatestNegotiation");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "invoice", "extra-expenses"})
    @Severity(SeverityLevel.NORMAL)
    @Description("PUT /ExtraExpenses/HandleStatus/{id} — the safe, id-scoped sibling of the unscoped HandleStatus endpoint — for a non-existent id returns a 'not found' envelope failure rather than actually accepting anything.")
    public void handleStatusForNonExistentIdReturnsEnvelopeFailure() {
        Response res = invoice().body(Map.of()).put("/ExtraExpenses/HandleStatus/000000000000000000000000");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "invoice", "extra-expenses"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /ExtraExpenses/{id}/AcceptNegotiation — the safe, id-scoped sibling of the unscoped AcceptNegotiation endpoint — for a non-existent id returns a 'not found' envelope failure.")
    public void acceptNegotiationForNonExistentIdReturnsEnvelopeFailure() {
        Response res = invoice().body(Map.of()).post("/ExtraExpenses/000000000000000000000000/AcceptNegotiation");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "invoice", "extra-expenses"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /ExtraExpenses/{id}/CreateNegotiation with an empty body fails model validation (HTTP 400): Attachments and Description are required — checked before the fake id is looked up.")
    public void createNegotiationWithEmptyBodyReturnsValidationError() {
        Response res = invoice().body(Map.of()).post("/ExtraExpenses/000000000000000000000000/CreateNegotiation");
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.jsonPath().getMap("errors")).containsKey("Attachments");
    }

    @Test(groups = {"api", "invoice", "extra-expenses"})
    @Severity(SeverityLevel.MINOR)
    @Description("POST /ExtraExpenses with an empty array returns an envelope failure (statusCode 500) rather than silently creating nothing — confirmed live, unlike the tenant User/Create endpoint an empty array here is NOT treated as a safe no-op.")
    public void createExtraExpensesWithEmptyArrayReturnsEnvelopeFailure() {
        Response res = invoice().body(Collections.emptyList()).post("/ExtraExpenses");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "invoice", "extra-expenses"})
    @Severity(SeverityLevel.MINOR)
    @Description("DELETE /ExtraExpenses/{id} for a non-existent id returns an envelope failure (statusCode 500) rather than a silent success or a raw exception.")
    public void deleteExtraExpenseWithNonExistentIdReturnsEnvelopeFailure() {
        Response res = invoice().delete("/ExtraExpenses/000000000000000000000000");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(enabled = false, groups = {"api", "invoice", "extra-expenses", "mutating"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — creates real extra expense line items against a real claim. Enable only against a disposable claim.")
    public void createExtraExpensesWithValidPayloadReturns200() {
        Response res = invoice().body(java.util.List.of(Map.of())).post("/ExtraExpenses");
        ApiAssertions.assertEnvelopeSuccess(res);
    }

    @Test(enabled = false, groups = {"api", "invoice", "extra-expenses", "mutating", "destructive", "bulk", "unscoped"})
    @Severity(SeverityLevel.BLOCKER)
    @Description("STUB — PUT /ExtraExpenses/HandleStatus (no id) is unscoped and confirmed live to 'Accept' with an empty body. Never enable against the shared QA environment without explicit sign-off.")
    public void handleStatusUnscopedIsBulk_NeverRunWithoutSignOff() {
        Response res = invoice().body(Map.of()).put("/ExtraExpenses/HandleStatus");
        ApiAssertions.assertEnvelopeSuccess(res);
    }

    @Test(enabled = false, groups = {"api", "invoice", "extra-expenses", "mutating", "destructive", "bulk", "unscoped"})
    @Severity(SeverityLevel.BLOCKER)
    @Description("STUB — PUT /ExtraExpenses/AcceptNegotiation (no id) is unscoped and confirmed live to 'Accept' with an empty body. Never enable against the shared QA environment without explicit sign-off.")
    public void acceptNegotiationUnscopedIsBulk_NeverRunWithoutSignOff() {
        Response res = invoice().body(Map.of()).put("/ExtraExpenses/AcceptNegotiation");
        ApiAssertions.assertEnvelopeSuccess(res);
    }
}
