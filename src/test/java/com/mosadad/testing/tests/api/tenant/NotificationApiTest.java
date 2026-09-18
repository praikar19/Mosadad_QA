package com.mosadad.testing.tests.api.tenant;

import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** tenant service — Notification tag. Notification templates/config, not to be confused with claims/InAppNotification or claims/WebNotification (per-user inbox), which live in the claims service. */
@Epic("Mosadad Recovery Claim")
@Feature("Tenant API — Notification")
public class NotificationApiTest extends BaseApiTest {

    @Test(groups = {"api", "tenant", "notification"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Notification/List with an empty filter body returns 200 with a paged (possibly empty) items collection.")
    public void listNotificationsWithEmptyFilterReturns200() {
        Response res = tenant().body(Map.of()).post("/Notification/List");
        ApiAssertions.assertEnvelopeSuccess(res);
        assertThat(res.jsonPath().getList("response.items")).isNotNull();
    }

    @Test(groups = {"api", "tenant", "notification"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /Notification/ListEvents/{category} for a valid category (\"claims\") returns 200 with an event-types list (possibly empty for this tenant).")
    public void listEventsForClaimsCategoryReturns200() {
        Response res = tenant().get("/Notification/ListEvents/claims");
        ApiAssertions.assertEnvelopeSuccess(res);
        assertThat(res.jsonPath().getList("response")).isNotNull();
    }

    @Test(groups = {"api", "tenant", "notification"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /Notification/ListEvents/{category} for an unknown category returns a safe envelope result rather than a crash — read-only validation coverage.")
    public void listEventsForUnknownCategoryReturnsSafeEnvelope() {
        Response res = tenant().get("/Notification/ListEvents/not-a-real-category");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.jsonPath().getList("response")).isNotNull();
    }

    @Test(groups = {"api", "tenant", "notification"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Notification/ListTemplates with an empty body fails model validation (HTTP 400): eventId is required.")
    public void listTemplatesWithoutEventIdReturnsValidationError() {
        Response res = tenant().body(Map.of()).post("/Notification/ListTemplates");
        ApiAssertions.assertValidationProblem(res, "eventId");
    }

    @Test(groups = {"api", "tenant", "notification"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /Notification/LoadNotification/{id} for a well-formed but non-existent id returns an envelope failure, not a 500.")
    public void loadNotificationWithNonExistentIdReturnsEnvelopeFailure() {
        Response res = tenant().get("/Notification/LoadNotification/000000000000000000000000");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "tenant", "notification"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Notification/Create with an empty body fails model validation (HTTP 400), flagging Name, EventTypeId, EntityTypeId and ContactPersons as required — no notification is persisted.")
    public void createNotificationWithEmptyBodyReturnsValidationError() {
        Response res = tenant().body(Map.of()).post("/Notification/Create");
        ApiAssertions.assertValidationProblem(res, "Name", "EventTypeId", "EntityTypeId");
    }

    @Test(enabled = false, groups = {"api", "tenant", "notification", "mutating"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — creating a real Notification config persists it for the whole shared tenant. Enable once a disposable eventTypeId/contactPerson fixture is agreed with QA.")
    public void createNotificationWithValidPayloadReturns200() {
        Response res = tenant().body(Map.of("name", "QA Automation Notification")).post("/Notification/Create");
        ApiAssertions.assertEnvelopeSuccess(res);
    }

    @Test(enabled = false, groups = {"api", "tenant", "notification", "mutating"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — updates a real Notification config. Enable once run against a disposable notification created by this suite.")
    public void updateNotificationReturns200() {
        Response res = tenant().body(Map.of("id", "placeholder")).put("/Notification/Update/placeholder");
        ApiAssertions.assertEnvelopeSuccess(res);
    }

    @Test(enabled = false, groups = {"api", "tenant", "notification", "mutating"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — toggles a Notification's active flag. Enable only against a disposable fixture.")
    public void setNotificationActivationReturns200() {
        Response res = tenant().put("/Notification/SetActivation/placeholder/true");
        ApiAssertions.assertEnvelopeSuccess(res);
    }

    @Test(enabled = false, groups = {"api", "tenant", "notification", "mutating", "destructive"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — permanently deletes a Notification config. Only ever run against a disposable fixture created by this suite.")
    public void deleteNotificationReturns200() {
        Response res = tenant().delete("/Notification/placeholder");
        ApiAssertions.assertEnvelopeSuccess(res);
    }
}
