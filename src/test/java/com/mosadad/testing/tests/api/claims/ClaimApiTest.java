package com.mosadad.testing.tests.api.claims;

import com.mosadad.testing.api.ApiAssertions;`r`nimport com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * claims service — Claim tag. The core recovery-claim entity driving the
 * whole lifecycle in MOSADAD_DOMAIN.md (Stage 1 registration through
 * Stage 4 settlement, plus disputes, reassignment, and cross-insurer
 * reallocation). The largest single tag in the API (35 operations).
 */
@Epic("Mosadad Recovery Claim")
@Feature("Claims API — Claim")
public class ClaimApiTest extends BaseApiTest {

    private static final String REAL_CLAIM_ID = "6aab489ecb32564549128fa6";
    private static final String FAKE_ID = "000000000000000000000000";

    /* ── Reads ─────────────────────────────────────────────────────────── */

    @Test(groups = {"api", "claims", "claim"})
    @Severity(SeverityLevel.BLOCKER)
    @Description("GET /Claim/{id} for a real claim returns 200 with the full claim record, including its lifecycle flags (isActive, isFastTrack, isManual, hasExtraExpenses).")
    public void getClaimByIdForRealClaimReturns200() {
        Response res = claims().get("/Claim/" + REAL_CLAIM_ID);
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", true, "envelope isSuccess");
        ApiAssertions.assertJsonBoolean(res, "response.isActive", true, "envelope response.isActive");
    }

    @Test(groups = {"api", "claims", "claim"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /Claim/History/{id} for a real claim returns 200 with its real stage-transition history.")
    public void getClaimHistoryForRealClaimReturns200() {
        Response res = claims().get("/Claim/History/" + REAL_CLAIM_ID);
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.jsonPath().getList("response")).isNotEmpty();
    }

    @Test(groups = {"api", "claims", "claim"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /Claim/{claimId}/FullHistory for a real claim returns 200 with a richer, company-annotated history (claimantCompanyName, currentAtFaultCompanyName).")
    public void getClaimFullHistoryForRealClaimReturns200() {
        Response res = claims().get("/Claim/" + REAL_CLAIM_ID + "/FullHistory");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.jsonPath().getString("response.claimId")).isEqualTo(REAL_CLAIM_ID);
    }

    @Test(groups = {"api", "claims", "claim"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /Claim/Attachments/{id} for a real claim returns 200 with its full attachment bundle (claimAttachment, quotationAttachment, intimationLetter, rejectionAttachment).")
    public void getClaimAttachmentsForRealClaimReturns200() {
        Response res = claims().get("/Claim/Attachments/" + REAL_CLAIM_ID);
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.jsonPath().getMap("response")).containsKeys("claimAttachment", "quotationAttachment");
    }

    @Test(groups = {"api", "claims", "claim"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /Claim/CreditNote/{claimId} for a claim with no credit note yet returns a 'Credit note not found' envelope failure, not a 500.")
    public void getClaimCreditNoteForClaimWithoutOneReturnsEnvelopeFailure() {
        Response res = claims().get("/Claim/CreditNote/" + REAL_CLAIM_ID);
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "claims", "claim"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /Claim/DebitNote/{claimId} for a claim with no debit note yet returns a 'Debit note not found' envelope failure, not a 500.")
    public void getClaimDebitNoteForClaimWithoutOneReturnsEnvelopeFailure() {
        Response res = claims().get("/Claim/DebitNote/" + REAL_CLAIM_ID);
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "claims", "claim"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /Claim/CurrentUserAuthority for a Total Loss repair type (not 'workshop'/'agency') returns an envelope validation failure with a real business-rule message — confirms the repair-type contract without needing a real handler assignment.")
    public void getCurrentUserAuthorityWithInvalidRepairTypeReturnsEnvelopeFailure() {
        Response res = claims().queryParam("claimAmount", 100).queryParam("permissionName", "test").queryParam("repairType", 1)
                .get("/Claim/CurrentUserAuthority");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
        assertThat(res.jsonPath().getString("message")).containsIgnoringCase("workshop");
    }

    @Test(groups = {"api", "claims", "claim"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("POST /Claim/List with an empty filter returns 200 with real, non-empty claim records.")
    public void listClaimsReturns200() {
        Response res = claims().body(Map.of()).post("/Claim/List");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.jsonPath().getList("response.items")).isNotEmpty();
    }

    @Test(groups = {"api", "claims", "claim"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Claim/GSearch (global search) with an empty filter returns 200 with a valid, well-formed paged result (empty on an unfiltered global search).")
    public void globalSearchClaimsReturns200() {
        Response res = claims().body(Map.of()).post("/Claim/GSearch");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", true, "envelope isSuccess");
    }

    @Test(groups = {"api", "claims", "claim"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /Claim/ListStatistics/{isFastTrack}/{type} returns 200 with real aggregate stats (totalNumberOfClaims, myActiveClaims, nearBreachClaims).")
    public void getClaimListStatisticsReturns200() {
        Response res = claims().get("/Claim/ListStatistics/false/1");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.jsonPath().getInt("response.totalNumberOfClaims")).isGreaterThan(0);
    }

    @Test(groups = {"api", "claims", "claim"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Claim/AdvancedSearchList with an empty filter returns 200 with real recoverable/payable claim buckets.")
    public void advancedSearchClaimsReturns200() {
        Response res = claims().body(Map.of()).post("/Claim/AdvancedSearchList");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.jsonPath().getList("response.recoverableClaims.items")).isNotNull();
    }

    @Test(groups = {"api", "claims", "claim"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /Claim/Latest returns 200 with real payableClaims/recoverableClaims lists — the exact call the live dashboard makes (see FRAMEWORK.md's captured network traffic).")
    public void getLatestClaimsReturns200() {
        Response res = claims().get("/Claim/Latest");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.jsonPath().getList("response.payableClaims")).isNotEmpty();
    }

    @Test(groups = {"api", "claims", "claim"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /Claim/Latest/{entityId} for the logged-in entity returns 200 with the same shape as the entity-less variant.")
    public void getLatestClaimsForOwnEntityReturns200() {
        Response res = claims().get("/Claim/Latest/" + entityId());
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.jsonPath().getList("response.payableClaims")).isNotNull();
    }

    @Test(groups = {"api", "claims", "claim"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /Claim/RecentActivities returns 200 with real recent activity entries (activityStage, statusLabel, lastActivityAt).")
    public void getRecentActivitiesReturns200() {
        Response res = claims().get("/Claim/RecentActivities");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.jsonPath().getList("response.payableClaims")).isNotNull();
    }

    @Test(groups = {"api", "claims", "claim"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /Claim/RecentActivities/{entityId} for the logged-in entity returns 200 with the same shape as the entity-less variant.")
    public void getRecentActivitiesForOwnEntityReturns200() {
        Response res = claims().get("/Claim/RecentActivities/" + entityId());
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", true, "envelope isSuccess");
    }

    @Test(groups = {"api", "claims", "claim"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /Claim/testSignalR/{id} (a SignalR real-time connectivity smoke check) returns 200.")
    public void testSignalRReturns200() {
        Response res = claims().get("/Claim/testSignalR/" + FAKE_ID);
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
    }

    @Test(groups = {"api", "claims", "claim"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /Claim/ValidateClaimNumber for a made-up claim number/company/accident combination returns 200 'Claim number is valid.' — confirmed live this checks uniqueness (not existence), so an unused number is valid by definition.")
    public void validateClaimNumberForUnusedNumberReturnsValid() {
        Response res = claims().queryParam("claimNumber", "TEST123").queryParam("claimantCompanyId", FAKE_ID).queryParam("accidentId", FAKE_ID)
                .get("/Claim/ValidateClaimNumber");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "response", true, "envelope response");
    }

    @Test(groups = {"api", "claims", "claim"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /Claim/ValidateAtFaultClaimNumber for a made-up number returns 200 'At-fault claim number is valid.' — same uniqueness-check contract as ValidateClaimNumber.")
    public void validateAtFaultClaimNumberForUnusedNumberReturnsValid() {
        Response res = claims().queryParam("atFaultClaimNumber", "TEST").queryParam("atFaultCompanyId", FAKE_ID).queryParam("accidentId", FAKE_ID)
                .get("/Claim/ValidateAtFaultClaimNumber");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "response", true, "envelope response");
    }

    @Test(groups = {"api", "claims", "claim"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /Claim/ageing-Analysis returns 200 with real recoverable/payable ageing buckets (< 60 Days, 60+, 90+, 180+, 365+).")
    public void getClaimAgeingAnalysisReturns200() {
        Response res = claims().get("/Claim/ageing-Analysis");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        List<Map<String, Object>> buckets = res.jsonPath().getList("response.recoverable.buckets");
        assertThat(buckets).isNotEmpty();
    }

    /* ── Writes: validation-only (empty/fake-id, no real mutation) ──────── */

    @Test(groups = {"api", "claims", "claim"})
    @Severity(SeverityLevel.MINOR)
    @Description("POST /Claim/HandleStatus/{claimId} for a non-existent claim returns a 'Claim not found' envelope failure, not a 500.")
    public void handleStatusForNonExistentClaimReturnsEnvelopeFailure() {
        Response res = claims().body(Map.of()).post("/Claim/HandleStatus/" + FAKE_ID);
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "claims", "claim"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Claim/CreateCreditNote with an empty body fails model validation (HTTP 400): ClaimsIds is required.")
    public void createCreditNoteWithEmptyBodyReturnsValidationError() {
        Response res = claims().body(Map.of()).post("/Claim/CreateCreditNote");
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.jsonPath().getMap("errors")).containsKey("ClaimsIds");
    }

    @Test(groups = {"api", "claims", "claim"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Claim/UploadDebitNote/{claimId} with an empty body fails model validation (HTTP 400): FileBase64 is required.")
    public void uploadDebitNoteWithEmptyBodyReturnsValidationError() {
        Response res = claims().body(Map.of()).post("/Claim/UploadDebitNote/" + FAKE_ID);
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.jsonPath().getMap("errors")).containsKey("FileBase64");
    }

    @Test(groups = {"api", "claims", "claim"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Claim/{claimId}/EligibleHandlers with an empty body returns an envelope failure ('At least one permission name is required') rather than a 500.")
    public void getEligibleHandlersWithEmptyBodyReturnsEnvelopeFailure() {
        Response res = claims().body(Map.of()).post("/Claim/" + FAKE_ID + "/EligibleHandlers");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "claims", "claim"})
    @Severity(SeverityLevel.MINOR)
    @Description("PUT /Claim/OtherAttachments/{id} for a non-existent claim with an empty array returns a 'Claim not found' envelope failure.")
    public void updateOtherAttachmentsForNonExistentClaimReturnsEnvelopeFailure() {
        Response res = claims().body(List.of()).put("/Claim/OtherAttachments/" + FAKE_ID);
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "claims", "claim"})
    @Severity(SeverityLevel.MINOR)
    @Description("POST /Claim/ConfirmPayment/{claimId}?amount= for a non-existent claim returns a 'Claim not found' envelope failure.")
    public void confirmPaymentForNonExistentClaimReturnsEnvelopeFailure() {
        Response res = claims().queryParam("amount", 1).post("/Claim/ConfirmPayment/" + FAKE_ID);
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "claims", "claim"})
    @Severity(SeverityLevel.MINOR)
    @Description("POST /Claim/ClaimClosure/{id} for a non-existent claim with an empty body returns a 'Claim not found' envelope failure.")
    public void claimClosureForNonExistentClaimReturnsEnvelopeFailure() {
        Response res = claims().body(Map.of()).post("/Claim/ClaimClosure/" + FAKE_ID);
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "claims", "claim", "bug"})
    @Severity(SeverityLevel.MINOR)
    @Description("BUG-ish finding — POST /Claim/Dispute/{claimId} for a well-formed but NON-EXISTENT claim id still returns 200 'Claim Disputed Successfully' rather than a 'not found' failure — confirmed live it does not validate the claim exists before reporting success. Uses a fake id specifically so this doesn't raise a real dispute against a real claim.")
    public void disputeNonExistentClaimStillReportsSuccess() {
        Response res = claims().post("/Claim/Dispute/" + FAKE_ID);
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", true, "envelope isSuccess");
    }

    @Test(groups = {"api", "claims", "claim"})
    @Severity(SeverityLevel.NORMAL)
    @Description("PUT /Claim/EditAtFaultClaim/{claimId} with an empty body fails model validation (HTTP 400): NewCompanyId is required.")
    public void editAtFaultClaimWithEmptyBodyReturnsValidationError() {
        Response res = claims().body(Map.of()).put("/Claim/EditAtFaultClaim/" + FAKE_ID);
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.jsonPath().getMap("errors")).containsKey("NewCompanyId");
    }

    @Test(groups = {"api", "claims", "claim"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Claim/{claimId}/ChangeAtFaultInsurer with an empty body fails model validation (HTTP 400): Comment and NewInsurerCompanyId are required.")
    public void changeAtFaultInsurerWithEmptyBodyReturnsValidationError() {
        Response res = claims().body(Map.of()).post("/Claim/" + FAKE_ID + "/ChangeAtFaultInsurer");
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.jsonPath().getMap("errors")).containsKeys("Comment", "NewInsurerCompanyId");
    }

    @Test(groups = {"api", "claims", "claim"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Claim/{claimId}/Reject with an empty body fails model validation (HTTP 400): Comment is required.")
    public void rejectClaimWithEmptyBodyReturnsValidationError() {
        Response res = claims().body(Map.of()).post("/Claim/" + FAKE_ID + "/Reject");
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.jsonPath().getMap("errors")).containsKey("Comment");
    }

    @Test(groups = {"api", "claims", "claim"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Claim/{claimId}/Reallocate with an empty body fails model validation (HTTP 400): Comment and TargetCompanyId are required.")
    public void reallocateClaimWithEmptyBodyReturnsValidationError() {
        Response res = claims().body(Map.of()).post("/Claim/" + FAKE_ID + "/Reallocate");
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.jsonPath().getMap("errors")).containsKeys("Comment", "TargetCompanyId");
    }

    @Test(groups = {"api", "claims", "claim"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Claim/{claimId}/AssignHandler with an empty body returns an envelope failure ('AssignedToUserId is required') rather than a 500.")
    public void assignHandlerWithEmptyBodyReturnsEnvelopeFailure() {
        Response res = claims().body(Map.of()).post("/Claim/" + FAKE_ID + "/AssignHandler");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "claims", "claim"})
    @Severity(SeverityLevel.MINOR)
    @Description("PUT /Claim/{claimId}/ReassignUser for a non-existent claim returns a 'Claim not found' envelope failure.")
    public void reassignUserForNonExistentClaimReturnsEnvelopeFailure() {
        Response res = claims().body(Map.of()).put("/Claim/" + FAKE_ID + "/ReassignUser");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "claims", "claim"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Claim/{claimId}/Resubmit with an empty body fails model validation (HTTP 400): Comment is required.")
    public void resubmitClaimWithEmptyBodyReturnsValidationError() {
        Response res = claims().body(Map.of()).post("/Claim/" + FAKE_ID + "/Resubmit");
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.jsonPath().getMap("errors")).containsKey("Comment");
    }

    @Test(groups = {"api", "claims", "claim"})
    @Severity(SeverityLevel.MINOR)
    @Description("DELETE /Claim/{claimId} for a non-existent claim returns a 'Claim not found' envelope failure, not a raw crash.")
    public void deleteClaimForNonExistentClaimReturnsEnvelopeFailure() {
        Response res = claims().delete("/Claim/" + FAKE_ID);
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    /* ── Writes: real mutations against real claims — disabled stubs ────── */

    @Test(enabled = false, groups = {"api", "claims", "claim", "mutating", "destructive"})
    @Severity(SeverityLevel.BLOCKER)
    @Description("STUB — permanently deletes a real recovery claim. Enable only against a disposable claim created by this suite.")
    public void deleteClaimReturns200() {
        Response res = claims().delete("/Claim/placeholder-claim-id");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
    }

    @Test(enabled = false, groups = {"api", "claims", "claim", "mutating"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("STUB — raises a real, permanently-logged dispute against a real claim (see MOSADAD_DOMAIN.md §Disputes). Enable only against a disposable claim.")
    public void disputeRealClaimReturns200() {
        Response res = claims().post("/Claim/Dispute/" + REAL_CLAIM_ID);
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
    }

    @Test(enabled = false, groups = {"api", "claims", "claim", "mutating"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("STUB — closes a real claim (terminal state, see MOSADAD_DOMAIN.md §Stage 4). Enable only against a disposable claim already through settlement.")
    public void closeRealClaimReturns200() {
        Response res = claims().body(Map.of()).post("/Claim/ClaimClosure/" + REAL_CLAIM_ID);
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
    }

    @Test(enabled = false, groups = {"api", "claims", "claim", "mutating"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("STUB — reassigns a real claim to a different handler/team member, a permission-gated action. Enable only against a disposable claim with a real target userId.")
    public void assignHandlerToRealClaimReturns200() {
        Response res = claims().body(Map.of("assignedToUserId", currentUserId())).post("/Claim/" + REAL_CLAIM_ID + "/AssignHandler");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
    }
}
