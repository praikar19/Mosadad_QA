package com.mosadad.testing.tests.api.invoice;

import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * invoice service — Invoice tag. Stage 3 of the claim lifecycle (see
 * MOSADAD_DOMAIN.md §Stage 3: final repair invoice, credit note, recovery
 * letter, At-Fault approve/negotiate).
 *
 * <p><b>Caution — {@code PUT /Invoice/Accept} (no id) is the same
 * dangerous unscoped-bulk pattern</b> documented in
 * {@code QuotationApiTest#acceptAllQuotationsIsBulkAndUnscoped_NeverRunWithoutSignOff}
 * and {@code ExtraExpensesApiTest} — confirmed live an empty body returns
 * {@code "Accepted Successfully":true}. Its id-scoped sibling
 * {@code PUT /Invoice/{id}/Accept} is safe and used below instead; the
 * unscoped variant is never called by this suite.
 */
@Epic("Mosadad Recovery Claim")
@Feature("Invoice API — Invoice")
public class InvoiceApiTest extends BaseApiTest {

    @Test(groups = {"api", "invoice"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /Invoice/{claimId}/Initial for a claim not yet accepted through quotation returns an envelope failure ('Please wait for the claim to be accepted') rather than a 500 — confirms the invoice stage correctly gates on quotation acceptance per MOSADAD_DOMAIN.md §Stage 2→3.")
    public void getInitialInvoiceForClaimNotYetAcceptedReturnsEnvelopeFailure() {
        Response res = invoice().get("/Invoice/6aab489ecb32564549128fa6/Initial");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "invoice"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /Invoice/{claimId}/ClaimId for a claim with no invoice yet returns an envelope failure, not a 500.")
    public void getInvoiceByClaimIdForClaimWithoutInvoiceReturnsEnvelopeFailure() {
        Response res = invoice().get("/Invoice/6aab489ecb32564549128fa6/ClaimId");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "invoice"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /Invoice/{id} for a non-existent invoice id returns an envelope failure.")
    public void getInvoiceByNonExistentIdReturnsEnvelopeFailure() {
        Response res = invoice().get("/Invoice/000000000000000000000000");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "invoice"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /Invoice/{claimId}/GetLatestNegotiation for a claim with no invoice negotiation returns an envelope failure.")
    public void getLatestInvoiceNegotiationForClaimWithoutOneReturnsEnvelopeFailure() {
        Response res = invoice().get("/Invoice/6aab489ecb32564549128fa6/GetLatestNegotiation");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "invoice"})
    @Severity(SeverityLevel.NORMAL)
    @Description("PUT /Invoice/{id}/Accept — the safe, id-scoped sibling of the unscoped Accept endpoint — for a non-existent id returns an envelope failure rather than actually accepting anything.")
    public void acceptInvoiceForNonExistentIdReturnsEnvelopeFailure() {
        Response res = invoice().body(Map.of()).put("/Invoice/000000000000000000000000/Accept");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "invoice"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Invoice with an empty body fails model validation (HTTP 400), flagging ClaimId, Attachments, QuotationId and InvoiceNumber as required — no invoice is created.")
    public void createInvoiceWithEmptyBodyReturnsValidationError() {
        Response res = invoice().body(Map.of()).post("/Invoice");
        ApiAssertions.assertValidationProblem(res, "ClaimId", "Attachments", "QuotationId", "InvoiceNumber");
    }

    @Test(groups = {"api", "invoice"})
    @Severity(SeverityLevel.NORMAL)
    @Description("PUT /Invoice/{id} with an empty body fails model validation (HTTP 400): InvoiceNumber is required — checked before the fake id is looked up.")
    public void editInvoiceWithEmptyBodyReturnsValidationError() {
        Response res = invoice().body(Map.of()).put("/Invoice/000000000000000000000000");
        ApiAssertions.assertValidationProblem(res, "InvoiceNumber");
    }

    @Test(groups = {"api", "invoice"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Invoice/{id}/CreateNegotiation with an empty body fails model validation (HTTP 400): Attachments and Description are required.")
    public void createInvoiceNegotiationWithEmptyBodyReturnsValidationError() {
        Response res = invoice().body(Map.of()).post("/Invoice/000000000000000000000000/CreateNegotiation");
        ApiAssertions.assertValidationProblem(res, "Attachments", "Description");
    }

    @Test(enabled = false, groups = {"api", "invoice", "mutating"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("STUB — creates a real invoice against a real, quotation-accepted claim, notifying the At-Fault insurer per MOSADAD_DOMAIN.md §Stage 3. Enable only against a disposable claim already through Stage 2.")
    public void createInvoiceWithValidPayloadReturns200() {
        Response res = invoice().body(Map.of("claimId", "placeholder")).post("/Invoice");
        ApiAssertions.assertEnvelopeSuccess(res);
    }

    @Test(enabled = false, groups = {"api", "invoice", "mutating", "destructive", "bulk", "unscoped"})
    @Severity(SeverityLevel.BLOCKER)
    @Description("STUB — PUT /Invoice/Accept (no id) is unscoped and confirmed live to 'Accept' with an empty body. Never enable against the shared QA environment without explicit sign-off.")
    public void acceptInvoiceUnscopedIsBulk_NeverRunWithoutSignOff() {
        Response res = invoice().body(Map.of()).put("/Invoice/Accept");
        ApiAssertions.assertEnvelopeSuccess(res);
    }
}
