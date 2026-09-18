package com.mosadad.testing.tests.api.claims;

import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** claims service — ClaimTask tag. Single-endpoint tag: per-claim task list (assignment/SLA tracking). */
@Epic("Mosadad Recovery Claim")
@Feature("Claims API — ClaimTask")
public class ClaimTaskApiTest extends BaseApiTest {

    @Test(groups = {"api", "claims", "claim-task"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /ClaimTask/Claim/{id} for a real claim returns an envelope failure (statusCode 200, isSuccess:false) rather than a raw unhandled exception — confirmed live on this QA data; documents the observed behavior rather than an assumed happy path.")
    public void getClaimTasksForRealClaimReturnsEnvelopeFailure() {
        Response res = claims().get("/ClaimTask/Claim/6aab489ecb32564549128fa6");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }
}
