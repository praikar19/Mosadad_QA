package com.mosadad.testing.tests.api.claims;

import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** claims service — NotificationContent tag. Templated notification message bodies keyed by actionId/eventType (e.g. "`ActionType` has been `ActionState` on the claim number {ClaimNumber}.") — feeds the templates tenant/Notification renders. */
@Epic("Mosadad Recovery Claim")
@Feature("Claims API — NotificationContent")
public class NotificationContentApiTest extends BaseApiTest {

    @Test(groups = {"api", "claims", "notification-content"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /NotificationContent/GetAll returns 200 with a real, non-empty list of templated notification content strings.")
    public void getAllNotificationContentReturns200() {
        Response res = claims().get("/NotificationContent/GetAll");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        List<Map<String, Object>> items = res.jsonPath().getList("response");
        assertThat(items).isNotEmpty();
        assertThat(items.get(0)).containsKeys("content", "actionId", "eventType");
    }

    @Test(groups = {"api", "claims", "notification-content"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /NotificationContent/GetById/{id} for a non-existent id returns an envelope failure, not a 500.")
    public void getNotificationContentByNonExistentIdReturnsEnvelopeFailure() {
        Response res = claims().get("/NotificationContent/GetById/000000000000000000000000");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "claims", "notification-content"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /NotificationContent/Create with an empty body fails model validation (HTTP 400): Content and ActionId are required — no content template is created.")
    public void createNotificationContentWithEmptyBodyReturnsValidationError() {
        Response res = claims().body(Map.of()).post("/NotificationContent/Create");
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.jsonPath().getMap("errors")).containsKeys("Content", "ActionId");
    }

    @Test(enabled = false, groups = {"api", "claims", "notification-content", "mutating"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — creates a real notification content template used platform-wide. Enable with explicit sign-off on the actionId/content to seed.")
    public void createNotificationContentWithValidPayloadReturns200() {
        Response res = claims().body(Map.of("content", "QA test", "actionId", "placeholder")).post("/NotificationContent/Create");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
    }

    @Test(enabled = false, groups = {"api", "claims", "notification-content", "mutating"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — edits a real notification content template. Enable only against a disposable template.")
    public void updateNotificationContentReturns200() {
        Response res = claims().body(Map.of("content", "Renamed")).put("/NotificationContent/Update/placeholder-id");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
    }

    @Test(enabled = false, groups = {"api", "claims", "notification-content", "mutating", "destructive"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — permanently deletes a real notification content template. Enable only against a disposable template.")
    public void deleteNotificationContentReturns200() {
        Response res = claims().delete("/NotificationContent/Delete/placeholder-id");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
    }
}
