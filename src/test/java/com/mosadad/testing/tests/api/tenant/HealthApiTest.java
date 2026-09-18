package com.mosadad.testing.tests.api.tenant;

import com.mosadad.testing.api.ApiClient.Service;
import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * tenant service — Health tag. A public, unauthenticated liveness probe —
 * confirmed live 2026-09-17 that it returns 200 with no bearer token at all
 * (unlike every other endpoint in this framework, which requires one).
 */
@Epic("Mosadad Recovery Claim")
@Feature("Tenant API — Health")
public class HealthApiTest extends BaseApiTest {

    @Test(groups = {"api", "tenant", "health", "smoke"})
    @Severity(SeverityLevel.BLOCKER)
    @Description("GET /Health is public (no auth required) and returns 200 with a human-readable 'Service is healthy' message.")
    public void healthCheckWithoutAuthReturns200() {
        Response res = noAuth(Service.TENANT).get("/Health");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.asString()).containsIgnoringCase("healthy");
    }
}
