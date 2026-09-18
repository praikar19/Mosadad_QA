package com.mosadad.testing.tests.api.claims;

import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** claims service — Events tag. Notification event-type catalog feeding tenant service's Notification templates (see tenant {@code NotificationApiTest#listEventsForClaimsCategoryReturns200}). */
@Epic("Mosadad Recovery Claim")
@Feature("Claims API — Events")
public class EventsApiTest extends BaseApiTest {

    @Test(groups = {"api", "claims", "events"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /Events/GetAll returns 200 with a real, non-empty list of event types.")
    public void getAllEventsReturns200() {
        Response res = claims().get("/Events/GetAll");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        List<Map<String, Object>> events = res.jsonPath().getList("response");
        assertThat(events).isNotEmpty();
        assertThat(events.get(0)).containsKeys("name", "type");
    }

    @Test(groups = {"api", "claims", "events"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /Events/GetById/{id} for a non-existent id returns an envelope failure, not a 500.")
    public void getEventByNonExistentIdReturnsEnvelopeFailure() {
        Response res = claims().get("/Events/GetById/000000000000000000000000");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "claims", "events"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Events/Create with an empty body fails model validation (HTTP 400): Name is required.")
    public void createEventWithEmptyBodyReturnsValidationError() {
        Response res = claims().body(Map.of()).post("/Events/Create");
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.jsonPath().getMap("errors")).containsKey("Name");
    }

    @Test(enabled = false, groups = {"api", "claims", "events", "mutating"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — creates a real event type in the shared catalog every tenant's Notification config draws from. Enable with explicit sign-off on the event name.")
    public void createEventWithValidPayloadReturns200() {
        Response res = claims().body(Map.of("name", "QA Automation Event")).post("/Events/Create");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
    }

    @Test(enabled = false, groups = {"api", "claims", "events", "mutating"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — edits a real event type. Enable only against a disposable event.")
    public void updateEventReturns200() {
        Response res = claims().body(Map.of("name", "Renamed")).put("/Events/Update/placeholder-event-id");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
    }

    @Test(enabled = false, groups = {"api", "claims", "events", "mutating", "destructive"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — permanently deletes a real event type, potentially breaking any tenant Notification config still referencing it. Enable only against a disposable event.")
    public void deleteEventReturns200() {
        Response res = claims().delete("/Events/Delete/placeholder-event-id");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
    }
}
