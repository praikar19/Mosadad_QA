package com.mosadad.testing.tests.api.claims;

import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * claims service — ClaimantNotification tag. Outbound notifications to the
 * claimant side of a claim.
 *
 * <p><b>Caution — {@code POST /ClaimantNotification/TriggerAll} is another
 * unscoped, platform-wide bulk action</b> in the same family as
 * {@code QuotationApiTest}'s {@code AcceptAll}: confirmed live it returns
 * {@code "All notification jobs triggered successfully."} and, unlike the
 * Accept-family endpoints, this one has no id-scoped safe sibling at all —
 * every call triggers every pending notification job platform-wide. It is
 * never called by this suite, not even for a validation probe.
 */
@Epic("Mosadad Recovery Claim")
@Feature("Claims API — ClaimantNotification")
public class ClaimantNotificationApiTest extends BaseApiTest {

    @Test(groups = {"api", "claims", "claimant-notification"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /ClaimantNotification/Send with an empty body fails model validation (HTTP 400): ClaimId is required — no notification is sent.")
    public void sendClaimantNotificationWithEmptyBodyReturnsValidationError() {
        Response res = claims().body(Map.of()).post("/ClaimantNotification/Send");
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.jsonPath().getMap("errors")).containsKey("ClaimId");
    }

    @Test(groups = {"api", "claims", "claimant-notification"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /ClaimantNotification/SendBulk/{fastTrackId} for a non-existent Fast Track batch id returns a 'Batch not found' envelope failure, not a raw crash — safely scoped since the id doesn't match any real batch.")
    public void sendBulkClaimantNotificationForNonExistentBatchReturnsEnvelopeFailure() {
        Response res = claims().post("/ClaimantNotification/SendBulk/000000000000000000000000");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
        assertThat(res.jsonPath().getString("message")).as("Expected message to contain 'not found' but actual was '%s'. Response body: %s", res.jsonPath().getString("message"), res.asString()).containsIgnoringCase("not found");
    }

    @Test(enabled = false, groups = {"api", "claims", "claimant-notification", "mutating"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — sends a real notification to a real claim's claimant. Enable only against a disposable claim.")
    public void sendClaimantNotificationWithValidPayloadReturns200() {
        Response res = claims().body(Map.of("claimId", "6aab489ecb32564549128fa6")).post("/ClaimantNotification/Send");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
    }

    @Test(enabled = false, groups = {"api", "claims", "claimant-notification", "mutating", "destructive", "bulk", "unscoped"})
    @Severity(SeverityLevel.BLOCKER)
    @Description("STUB — POST /ClaimantNotification/TriggerAll is unscoped and platform-wide with no safe id-scoped variant — confirmed live it triggers every pending notification job. Never enable against the shared QA environment without explicit sign-off.")
    public void triggerAllClaimantNotificationsIsPlatformWide_NeverRunWithoutSignOff() {
        Response res = claims().post("/ClaimantNotification/TriggerAll");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
    }
}
