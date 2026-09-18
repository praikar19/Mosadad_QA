package com.mosadad.testing.tests.api.claims;

import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** claims service — WebNotification tag. Broader web-notification feed per entity (title/content/creationDate) — distinct from InAppNotification (per-user bell inbox) and tenant's Notification (config/templates). */
@Epic("Mosadad Recovery Claim")
@Feature("Claims API — WebNotification")
public class WebNotificationApiTest extends BaseApiTest {

    @Test(groups = {"api", "claims", "web-notification"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /WebNotification/{entityId} for the logged-in entity with an empty body returns 200 with real notification history for that entity.")
    public void getWebNotificationsForOwnEntityReturns200() {
        Response res = claims().body(Map.of()).post("/WebNotification/" + entityId());
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        List<Map<String, Object>> items = res.jsonPath().getList("response.items");
        assertThat(items).isNotEmpty();
        assertThat(items.get(0)).containsKeys("entityId", "title", "content", "creationDate");
    }

    @Test(groups = {"api", "claims", "web-notification"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /WebNotification/GetById/{id} for a non-existent id returns an envelope failure.")
    public void getWebNotificationByNonExistentIdReturnsEnvelopeFailure() {
        Response res = claims().get("/WebNotification/GetById/000000000000000000000000");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "claims", "web-notification"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /WebNotification/Create with an empty body fails model validation (HTTP 400): Title, Content and EntityId are required.")
    public void createWebNotificationWithEmptyBodyReturnsValidationError() {
        Response res = claims().body(Map.of()).post("/WebNotification/Create");
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.jsonPath().getMap("errors")).containsKeys("Title", "Content", "EntityId");
    }

    @Test(groups = {"api", "claims", "web-notification"})
    @Severity(SeverityLevel.NORMAL)
    @Description("DELETE /WebNotification/Delete/{id} for a non-existent id returns an envelope failure, not a raw crash.")
    public void deleteWebNotificationWithNonExistentIdReturnsEnvelopeFailure() {
        Response res = claims().delete("/WebNotification/Delete/000000000000000000000000");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(enabled = false, groups = {"api", "claims", "web-notification", "mutating"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — creates a real web notification visible to a real entity. Enable with explicit sign-off on title/content/entityId to seed.")
    public void createWebNotificationWithValidPayloadReturns200() {
        Response res = claims().body(Map.of("title", "QA test", "content", "QA test", "entityId", entityId())).post("/WebNotification/Create");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
    }

    @Test(enabled = false, groups = {"api", "claims", "web-notification", "mutating"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — edits a real web notification. Enable only against a disposable notification.")
    public void updateWebNotificationReturns200() {
        Response res = claims().body(Map.of()).put("/WebNotification/Update/placeholder-id");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
    }
}
