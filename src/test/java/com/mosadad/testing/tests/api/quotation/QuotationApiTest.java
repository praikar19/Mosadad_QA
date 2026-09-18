package com.mosadad.testing.tests.api.quotation;

import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * quotation service — Quotation tag. Stage 2 of the claim lifecycle (see
 * MOSADAD_DOMAIN.md §Stage 2: Normal Repair 72h SLA / Total Loss 168h SLA).
 *
 * <p>The envelope here carries extra fields beyond the standard
 * {statusCode, message, response, isSuccess, errors} shape:
 * {@code thresholdFailed, amount, permissionName, shopType} — tying into
 * the tenant service's recovery-amount approval thresholds (see
 * {@code RoleApiTest#getUserThresholdsForLoggedInUserReturns200} /
 * {@code #checkThresholdsForSuperAdminBypassesCheck}).
 *
 * <p><b>Caution — {@code PUT /Quotation/AcceptAll} is NOT id-scoped.</b>
 * Unlike every other write endpoint in this tag it takes no id and no
 * required body, and confirmed live it performs a real bulk accept when
 * called with an empty body (returned {@code "Accepted Successfully":true}).
 * There is no safe way to probe it without risking a real mutation, so it
 * has no live test at all here — only a permanently-disabled stub with this
 * warning. Do not call it ad hoc against a shared environment.
 */
@Epic("Mosadad Recovery Claim")
@Feature("Quotation API — Quotation")
public class QuotationApiTest extends BaseApiTest {

    @Test(groups = {"api", "quotation"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /Quotation/GenerateQuotationNumber/{claimSerialNumber} for a real claim serial number returns 200 with a generated number in the '<serial>-RQ-000N' format.")
    public void generateQuotationNumberForRealClaimSerialReturns200() {
        Response res = quotation().get("/Quotation/GenerateQuotationNumber/RC-2026-031512");
        ApiAssertions.assertEnvelopeSuccess(res);
        assertThat(res.jsonPath().getString("response")).startsWith("RC-2026-031512").contains("RQ");
    }

    @Test(groups = {"api", "quotation"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /Quotation/{id}/ByClaimId for a real claim with no quotation yet returns a 'Quotation not found' envelope failure, not a 500.")
    public void getQuotationByClaimIdForClaimWithoutQuotationReturnsEnvelopeFailure() {
        Response res = quotation().get("/Quotation/6aab489ecb32564549128fa6/ByClaimId");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
        assertThat(res.jsonPath().getString("message")).as("Expected message to contain 'not found' but actual was '%s'. Response body: %s", res.jsonPath().getString("message"), res.asString()).containsIgnoringCase("not found");
    }

    @Test(groups = {"api", "quotation"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /Quotation/{id} for a well-formed but non-existent quotation id returns a 'Quotation not found' envelope failure.")
    public void getQuotationByIdWithNonExistentIdReturnsEnvelopeFailure() {
        Response res = quotation().get("/Quotation/000000000000000000000000");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
        assertThat(res.jsonPath().getString("message")).as("Expected message to contain 'not found' but actual was '%s'. Response body: %s", res.jsonPath().getString("message"), res.asString()).containsIgnoringCase("not found");
    }

    @Test(groups = {"api", "quotation"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /Quotation/{claimId}/BeforeChangeRequest for a claim with no change request returns a 'Change request not found' envelope failure.")
    public void getBeforeChangeRequestForClaimWithoutOneReturnsEnvelopeFailure() {
        Response res = quotation().get("/Quotation/6aab489ecb32564549128fa6/BeforeChangeRequest");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "quotation"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /Quotation/{id}/LatestNegotiation?type= for a non-existent quotation returns a 'Negotiation not found' envelope failure.")
    public void getLatestNegotiationForNonExistentQuotationReturnsEnvelopeFailure() {
        Response res = quotation().queryParam("type", 1).get("/Quotation/000000000000000000000000/LatestNegotiation");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
        assertThat(res.jsonPath().getString("message")).as("Expected message to contain 'not found' but actual was '%s'. Response body: %s", res.jsonPath().getString("message"), res.asString()).containsIgnoringCase("not found");
    }

    @Test(groups = {"api", "quotation"})
    @Severity(SeverityLevel.MINOR)
    @Description("PUT /Quotation/{id}/Accept for a non-existent quotation returns a 'Quotation Not Found' envelope failure — scoped to a fake id, so nothing real is ever accepted.")
    public void acceptQuotationForNonExistentIdReturnsEnvelopeFailure() {
        Response res = quotation().body(Map.of()).put("/Quotation/000000000000000000000000/Accept");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "quotation"})
    @Severity(SeverityLevel.MINOR)
    @Description("PUT /Quotation/{id}/RejectChangeRequest for a non-existent quotation returns a 'Change request not found' envelope failure.")
    public void rejectChangeRequestForNonExistentIdReturnsEnvelopeFailure() {
        Response res = quotation().put("/Quotation/000000000000000000000000/RejectChangeRequest");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "quotation"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Quotation with an empty body fails model validation (HTTP 400): ClaimId is required — no quotation is created.")
    public void createQuotationWithEmptyBodyReturnsValidationError() {
        Response res = quotation().body(Map.of()).post("/Quotation");
        ApiAssertions.assertValidationProblem(res, "ClaimId");
    }

    @Test(groups = {"api", "quotation"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Quotation/ChangeRequest with an empty body fails model validation (HTTP 400): ClaimId is required.")
    public void createChangeRequestWithEmptyBodyReturnsValidationError() {
        Response res = quotation().body(Map.of()).post("/Quotation/ChangeRequest");
        ApiAssertions.assertValidationProblem(res, "ClaimId");
    }

    @Test(groups = {"api", "quotation"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Quotation/{id}/Negotiation with an empty body fails model validation (HTTP 400): RequiredDocuments is required — this check runs before the fake id is even looked up.")
    public void createNegotiationWithEmptyBodyReturnsValidationError() {
        Response res = quotation().body(Map.of()).post("/Quotation/000000000000000000000000/Negotiation");
        ApiAssertions.assertValidationProblem(res, "RequiredDocuments");
    }

    @Test(groups = {"api", "quotation"})
    @Severity(SeverityLevel.MINOR)
    @Description("POST /Quotation/BulkLog with an empty array returns a 'No quotations found' envelope failure rather than a 500 — confirmed live, no side effects.")
    public void bulkLogWithEmptyArrayReturnsEnvelopeFailure() {
        Response res = quotation().body(java.util.Collections.emptyList()).post("/Quotation/BulkLog");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(enabled = false, groups = {"api", "quotation", "mutating"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("STUB — creates a real Normal Repair or Total Loss quotation against a real claim, starting its SLA timer (72h/168h, see MOSADAD_DOMAIN.md). Enable only against a disposable claim created by the (also disabled) claims Claim create stub.")
    public void createQuotationWithValidPayloadReturns200() {
        Response res = quotation().body(Map.of("claimId", "placeholder-claim-id")).post("/Quotation");
        ApiAssertions.assertEnvelopeSuccess(res);
    }

    @Test(enabled = false, groups = {"api", "quotation", "mutating"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("STUB — accepts a real quotation, generating an Approval Letter per MOSADAD_DOMAIN.md §Stage 2A. Enable only against a disposable quotation in a testable state.")
    public void acceptQuotationWithValidPayloadReturns200() {
        Response res = quotation().body(Map.of()).put("/Quotation/placeholder-quotation-id/Accept");
        ApiAssertions.assertEnvelopeSuccess(res);
    }

    @Test(enabled = false, groups = {"api", "quotation", "mutating", "destructive", "bulk"})
    @Severity(SeverityLevel.BLOCKER)
    @Description("STUB — PUT /Quotation/AcceptAll is NOT id-scoped and confirmed live to perform a real bulk accept even with an empty body. Never enable this against the shared QA environment without an explicit, isolated fixture and QA sign-off.")
    public void acceptAllQuotationsIsBulkAndUnscoped_NeverRunWithoutSignOff() {
        Response res = quotation().body(Map.of()).put("/Quotation/AcceptAll");
        ApiAssertions.assertEnvelopeSuccess(res);
    }

    @Test(enabled = false, groups = {"api", "quotation", "mutating"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — creates a real Final LPO against a quotation. Enable only against a disposable quotation.")
    public void createFinalLpoWithValidPayloadReturns200() {
        Response res = quotation().body(Map.of()).post("/Quotation/placeholder-quotation-id/FinalLpo");
        ApiAssertions.assertEnvelopeSuccess(res);
    }

    @Test(enabled = false, groups = {"api", "quotation", "mutating"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — edits a real quotation's fields. Enable only against a disposable quotation.")
    public void editQuotationWithValidPayloadReturns200() {
        Response res = quotation().body(Map.of()).put("/Quotation/placeholder-quotation-id");
        ApiAssertions.assertEnvelopeSuccess(res);
    }

    @Test(enabled = false, groups = {"api", "quotation", "mutating"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — starts a real negotiation round on a quotation. Enable only against a disposable quotation, with real RequiredDocuments.")
    public void createNegotiationWithValidPayloadReturns200() {
        Response res = quotation().body(Map.of("requiredDocuments", java.util.List.of())).post("/Quotation/placeholder-quotation-id/Negotiation");
        ApiAssertions.assertEnvelopeSuccess(res);
    }
}
