package com.mosadad.testing.tests.api.claims;

import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * claims service — InAppNotification tag. The bell-icon notification inbox
 * (see FRAMEWORK.md's captured network traffic: {@code InAppNotification/Search}
 * is the real call the dashboard's bell button makes).
 *
 * <p>Note {@code PUT /InAppNotification/MarkAsRead} is body-only (no id) —
 * structurally similar to the risky AcceptAll family, but confirmed live to
 * be a genuinely benign, self-scoped action (marks the logged-in user's own
 * notifications read, the same as clicking "mark all read" in the UI) — so
 * unlike AcceptAll/Config-Update it IS exercised live here.
 */
@Epic("Mosadad Recovery Claim")
@Feature("Claims API — InAppNotification")
public class InAppNotificationApiTest extends BaseApiTest {

    @Test(groups = {"api", "claims", "in-app-notification"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("POST /InAppNotification/Search with an empty body returns 200 with the logged-in user's real notification inbox — the exact call the live dashboard's bell icon makes (confirmed via captured network traffic).")
    public void searchInAppNotificationsReturns200() {
        Response res = claims().body(Map.of()).post("/InAppNotification/Search");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        List<Map<String, Object>> items = res.jsonPath().getList("response.items");
        assertThat(items).isNotEmpty();
        assertThat(items.get(0)).containsKeys("id", "notificationType", "content", "title");
    }

    @Test(groups = {"api", "claims", "in-app-notification"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /InAppNotification/GetById/{id} for a non-existent id returns a 'not found' envelope failure.")
    public void getInAppNotificationByNonExistentIdReturnsEnvelopeFailure() {
        Response res = claims().get("/InAppNotification/GetById/000000000000000000000000");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
        assertThat(res.jsonPath().getString("message")).as("Expected message to contain 'not found' but actual was '%s'. Response body: %s", res.jsonPath().getString("message"), res.asString()).containsIgnoringCase("not found");
    }

    @Test(groups = {"api", "claims", "in-app-notification"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /InAppNotification/Create with an empty body fails model validation (HTTP 400): Title and EntityId are required.")
    public void createInAppNotificationWithEmptyBodyReturnsValidationError() {
        Response res = claims().body(Map.of()).post("/InAppNotification/Create");
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.jsonPath().getMap("errors")).containsKeys("Title", "EntityId");
    }

    @Test(groups = {"api", "claims", "in-app-notification"})
    @Severity(SeverityLevel.NORMAL)
    @Description("PUT /InAppNotification/MarkAsRead with an empty body is a real, self-scoped, benign action — confirmed live it returns 200 'Notifications marked as read successfully' (equivalent to the UI's own 'mark all read').")
    public void markAllInAppNotificationsAsReadReturns200() {
        Response res = claims().body(Map.of()).put("/InAppNotification/MarkAsRead");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", true, "envelope isSuccess");
    }

    @Test(groups = {"api", "claims", "in-app-notification"})
    @Severity(SeverityLevel.NORMAL)
    @Description("DELETE /InAppNotification/Delete/{id} for a non-existent id returns an envelope failure, not a raw crash.")
    public void deleteInAppNotificationWithNonExistentIdReturnsEnvelopeFailure() {
        Response res = claims().delete("/InAppNotification/Delete/000000000000000000000000");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(enabled = false, groups = {"api", "claims", "in-app-notification", "mutating"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — creates a real in-app notification for a real entity's users. Enable with explicit sign-off on title/entityId to seed.")
    public void createInAppNotificationWithValidPayloadReturns200() {
        Response res = claims().body(Map.of("title", "QA test", "entityId", entityId())).post("/InAppNotification/Create");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
    }

    @Test(enabled = false, groups = {"api", "claims", "in-app-notification", "mutating"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — edits a real in-app notification. Enable only against a disposable notification.")
    public void updateInAppNotificationReturns200() {
        Response res = claims().body(Map.of()).put("/InAppNotification/Update");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
    }
}
