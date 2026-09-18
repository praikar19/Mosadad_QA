package com.mosadad.testing.tests.api.claims;

import com.mosadad.testing.api.ApiClient.Service;
import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** claims service — Health tag. Same public liveness-probe contract as the other 5 services. */
@Epic("Mosadad Recovery Claim")
@Feature("Claims API — Health")
public class HealthApiTest extends BaseApiTest {

    @Test(groups = {"api", "claims", "health", "smoke"})
    @Severity(SeverityLevel.BLOCKER)
    @Description("GET /Health is public (no auth required) and returns 200 with a 'Service is healthy' message.")
    public void healthCheckWithoutAuthReturns200() {
        Response res = noAuth(Service.CLAIMS).get("/Health");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.asString()).containsIgnoringCase("healthy");
    }
}
