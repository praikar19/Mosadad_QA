package com.mosadad.testing.tests.api.inthub;

import com.mosadad.testing.api.ApiClient.Service;
import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** inthub service — Health tag. Public liveness probe, same contract as the other 5 services' Health endpoints. */
@Epic("Mosadad Recovery Claim")
@Feature("IntHub API — Health")
public class HealthApiTest extends BaseApiTest {

    @Test(groups = {"api", "inthub", "health", "smoke"})
    @Severity(SeverityLevel.BLOCKER)
    @Description("GET /Health is public (no auth required) and returns 200 with a 'Service is healthy' message.")
    public void healthCheckWithoutAuthReturns200() {
        Response res = noAuth(Service.INTHUB).get("/Health");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.asString()).containsIgnoringCase("healthy");
    }
}
