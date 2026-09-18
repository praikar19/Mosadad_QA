package com.mosadad.testing.tests.api.claims;

import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** claims service — Insights tag. A diagnostic endpoint that fires a test event into Azure Application Insights — not a data read, so the assertion is on the acknowledgement, not business data. */
@Epic("Mosadad Recovery Claim")
@Feature("Claims API — Insights")
public class InsightsApiTest extends BaseApiTest {

    @Test(groups = {"api", "claims", "insights"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /Insights/test-app-insights returns 200 with 'Event Sent' — confirms the App Insights telemetry pipeline is wired up and reachable, not a business data endpoint.")
    public void testAppInsightsEventReturns200() {
        Response res = claims().get("/Insights/test-app-insights");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.asString()).containsIgnoringCase("Event Sent");
    }
}
