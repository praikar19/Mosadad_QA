package com.mosadad.testing.tests.api.quotation;

import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** quotation service — Submittal tag. Workshop-estimate / repair-amount submissions attached to a claim's quotation (see MOSADAD_DOMAIN.md §Stage 2A step 1). */
@Epic("Mosadad Recovery Claim")
@Feature("Quotation API — Submittal")
public class SubmittalApiTest extends BaseApiTest {

    @Test(groups = {"api", "quotation", "submittal"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /Submittal/Claim/{id} for a real claim with no submittal yet returns 200 with an empty list, not a 404 — confirmed live.")
    public void getSubmittalsByClaimIdForClaimWithoutSubmittalsReturns200WithEmptyList() {
        Response res = quotation().get("/Submittal/Claim/6aab489ecb32564549128fa6");
        ApiAssertions.assertEnvelopeSuccess(res);
        assertThat(res.jsonPath().getList("response")).isEmpty();
    }

    @Test(groups = {"api", "quotation", "submittal"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /Submittal/{id} for a well-formed but non-existent submittal id returns an envelope failure, not a 500.")
    public void getSubmittalByNonExistentIdReturnsEnvelopeFailure() {
        Response res = quotation().get("/Submittal/000000000000000000000000");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }
}
