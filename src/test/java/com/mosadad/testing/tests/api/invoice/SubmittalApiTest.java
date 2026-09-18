package com.mosadad.testing.tests.api.invoice;

import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * invoice service — Submittal tag. Same submission concept as
 * {@code quotation}'s Submittal tag but for invoice-stage rejected/
 * resubmitted flows — a separate implementation confirmed live, not a proxy
 * to quotation's service.
 */
@Epic("Mosadad Recovery Claim")
@Feature("Invoice API — Submittal")
public class SubmittalApiTest extends BaseApiTest {

    @Test(groups = {"api", "invoice", "submittal"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /Submittal/Claim/{id} for a real claim with no submittal yet returns 200 with an empty list, not a 404.")
    public void getSubmittalsByClaimIdForClaimWithoutSubmittalsReturns200WithEmptyList() {
        Response res = invoice().get("/Submittal/Claim/6aab489ecb32564549128fa6");
        ApiAssertions.assertEnvelopeSuccess(res);
        assertThat(res.jsonPath().getList("response")).isEmpty();
    }

    @Test(groups = {"api", "invoice", "submittal"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /Submittal/{id} for a non-existent submittal id returns an envelope failure (statusCode 500), not a raw crash.")
    public void getSubmittalByNonExistentIdReturnsEnvelopeFailure() {
        Response res = invoice().get("/Submittal/000000000000000000000000");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "invoice", "submittal"})
    @Severity(SeverityLevel.MINOR)
    @Description("POST /Submittal/Claim/{id}/Rejected for a non-existent claim returns a bare HTTP 404 'Resource not found' — confirmed live this route behaves differently from the rest of the envelope-wrapped API on this QA deployment, documenting the observed behavior rather than an assumed contract.")
    public void rejectSubmittalForNonExistentClaimReturns404() {
        Response res = invoice().body(Map.of()).post("/Submittal/Claim/000000000000000000000000/Rejected");
        ApiAssertions.assertStatusCode(res, 404, "HTTP status");
    }

    @Test(groups = {"api", "invoice", "submittal"})
    @Severity(SeverityLevel.MINOR)
    @Description("POST /Submittal/Claim/{id}/Resubmitted for a non-existent claim returns the same bare HTTP 404 as the Rejected variant.")
    public void resubmitSubmittalForNonExistentClaimReturns404() {
        Response res = invoice().body(Map.of()).post("/Submittal/Claim/000000000000000000000000/Resubmitted");
        ApiAssertions.assertStatusCode(res, 404, "HTTP status");
    }
}
