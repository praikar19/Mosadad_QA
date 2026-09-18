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
 * claims service — Communication tag. Per-claim audit-logged comment thread
 * ("Messages here are specific to this claim and are retained for audit and
 * regulatory review" — real system welcome message, confirmed live) — this
 * is the structured-communication mechanism MOSADAD_DOMAIN.md §Disputes
 * describes replacing informal email back-and-forth.
 *
 * <p>Note the real route carries an extra {@code /api} segment
 * ({@code /api/Communication}, not {@code /Communication}) — confirmed live
 * against the Swagger-documented path, unlike claims/Action (see
 * {@code ActionApiTest}), which 404s regardless of the extra segment.
 */
@Epic("Mosadad Recovery Claim")
@Feature("Claims API — Communication")
public class CommunicationApiTest extends BaseApiTest {

    @Test(groups = {"api", "claims", "communication"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /api/Communication/{claimId} for a real claim returns 200 with the real comment thread, including the system's own welcome message.")
    public void getCommentsForRealClaimReturns200() {
        Response res = claims().get("/api/Communication/6aab489ecb32564549128fa6");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", true, "envelope isSuccess");
        List<Map<String, Object>> comments = res.jsonPath().getList("response");
        assertThat(comments).isNotEmpty();
        assertThat(comments.get(0)).containsKeys("commentId", "claimId", "content");
    }

    @Test(groups = {"api", "claims", "communication"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /api/Communication/{claimId}/unread-count for a real claim returns 200 with a numeric unread count.")
    public void getUnreadCommentCountForRealClaimReturns200() {
        Response res = claims().get("/api/Communication/6aab489ecb32564549128fa6/unread-count");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.jsonPath().getInt("response")).isGreaterThanOrEqualTo(0);
    }

    @Test(groups = {"api", "claims", "communication"})
    @Severity(SeverityLevel.NORMAL)
    @Description("DELETE /api/Communication/{commentId} for a non-existent comment returns a 'not found or already deleted' envelope failure, not a 500.")
    public void deleteCommentWithNonExistentIdReturnsEnvelopeFailure() {
        Response res = claims().delete("/api/Communication/000000000000000000000000");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
        assertThat(res.jsonPath().getString("message")).as("Expected message to contain 'not found' but actual was '%s'. Response body: %s", res.jsonPath().getString("message"), res.asString()).containsIgnoringCase("not found");
    }

    @Test(groups = {"api", "claims", "communication"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /api/Communication with an empty body fails model validation (HTTP 400): ClaimId and Content are required — no comment is posted.")
    public void postCommentWithEmptyBodyReturnsValidationError() {
        Response res = claims().body(Map.of()).post("/api/Communication");
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.jsonPath().getMap("errors")).containsKeys("ClaimId", "Content");
    }

    @Test(enabled = false, groups = {"api", "claims", "communication", "mutating"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — posts a real, permanently audit-logged comment onto a real claim's thread. Enable only against a disposable claim, since the system explicitly retains these for regulatory review.")
    public void postCommentWithValidPayloadReturns200() {
        Response res = claims().body(Map.of("claimId", "6aab489ecb32564549128fa6", "content", "QA automation test comment"))
                .post("/api/Communication");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", true, "envelope isSuccess");
    }
}
