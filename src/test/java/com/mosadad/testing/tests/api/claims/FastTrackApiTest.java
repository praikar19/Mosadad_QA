package com.mosadad.testing.tests.api.claims;

import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * claims service — FastTrack tag. Bulk Excel-upload claim recovery for
 * high-volume, simple claims against the same counterpart insurer (see
 * MOSADAD_DOMAIN.md §Fast Track — Why It Exists). The largest tag by
 * endpoint count after Dashboard (43 operations): batch lifecycle
 * (create/save/submit/close), per-item corrections, document upload
 * (including chunked large-file upload), SAI (Smart AI?) document
 * ingestion, and exports.
 */
@Epic("Mosadad Recovery Claim")
@Feature("Claims API — FastTrack")
public class FastTrackApiTest extends BaseApiTest {

    private static final String FAKE_ID = "000000000000000000000000";
    private static final String REAL_BATCH_ID = "69bcf0fce3435748c25a7173"; // real batch, different (Qatar) company — used for batch-scoped reads only

    /* ── Reads ─────────────────────────────────────────────────────────── */

    @Test(groups = {"api", "claims", "fast-track"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("POST /FastTrack/List with an empty filter returns 200 with real, non-empty Fast Track batches (batchCode, entityName, status).")
    public void listFastTrackBatchesReturns200() {
        Response res = claims().body(Map.of()).post("/FastTrack/List");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        List<Map<String, Object>> items = res.jsonPath().getList("response.items");
        assertThat(items).isNotEmpty();
        assertThat(items.get(0)).containsKeys("id", "batchCode", "entityName", "status");
    }

    @Test(groups = {"api", "claims", "fast-track"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /FastTrack/List/{batchId} for a real batch with an empty filter returns 200 with that batch's claim items (possibly empty).")
    public void listFastTrackItemsForRealBatchReturns200() {
        Response res = claims().body(Map.of()).post("/FastTrack/List/" + REAL_BATCH_ID);
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.jsonPath().getList("response.items")).isNotNull();
    }

    @Test(groups = {"api", "claims", "fast-track"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /FastTrack/atfault/batch/{batchId}/list for a real batch with an empty filter returns 200 — the At-Fault counterpart's view of a batch's claims.")
    public void listAtFaultBatchClaimsForRealBatchReturns200() {
        Response res = claims().body(Map.of()).post("/FastTrack/atfault/batch/" + REAL_BATCH_ID + "/list");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", true, "envelope isSuccess");
    }

    @Test(groups = {"api", "claims", "fast-track"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /FastTrack/{claimId} for a non-existent claim/item returns a 'not found' envelope failure, not a 500.")
    public void getFastTrackByNonExistentClaimIdReturnsEnvelopeFailure() {
        Response res = claims().get("/FastTrack/" + FAKE_ID);
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "claims", "fast-track"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /FastTrack/Item/{itemId} for a non-existent item returns a 'Fast track item not found' envelope failure.")
    public void getFastTrackItemByNonExistentIdReturnsEnvelopeFailure() {
        Response res = claims().get("/FastTrack/Item/" + FAKE_ID);
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "claims", "fast-track"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /FastTrack/BatchItemsStats/{batchId} for a real batch returns 200 with real aggregate stats (batchStatus, validItemsCount, issuesCounts).")
    public void getBatchItemsStatsForRealBatchReturns200() {
        Response res = claims().get("/FastTrack/BatchItemsStats/" + REAL_BATCH_ID);
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.jsonPath().getString("response.batchId")).isEqualTo(REAL_BATCH_ID);
        assertThat(res.jsonPath().getMap("response.issuesCounts")).isNotNull();
    }

    @Test(groups = {"api", "claims", "fast-track"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /FastTrack/AtFaultTemplate/{batchId} for a non-existent batch returns a 'Batch not found' envelope failure.")
    public void getAtFaultTemplateForNonExistentBatchReturnsEnvelopeFailure() {
        Response res = claims().get("/FastTrack/AtFaultTemplate/" + FAKE_ID);
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "claims", "fast-track"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /FastTrack/Template/{batchId} returns 200 with a real base64-encoded template file even for a non-existent batch id — confirmed live this is a generic template, not batch-specific data.")
    public void getBatchTemplateReturns200RegardlessOfBatchId() {
        Response res = claims().get("/FastTrack/Template/" + FAKE_ID);
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.jsonPath().getString("response")).isNotBlank();
    }

    @Test(groups = {"api", "claims", "fast-track"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /FastTrack/EmptyTemplate returns 200 with a real, non-empty blank Excel template for bulk claim upload.")
    public void getEmptyTemplateReturns200() {
        Response res = claims().get("/FastTrack/EmptyTemplate");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.asByteArray().length).isGreaterThan(0);
    }

    @Test(groups = {"api", "claims", "fast-track"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /FastTrack/batch/{batchId}/validation for a real batch owned by a DIFFERENT company returns a real HTTP 403 'permission to view this batch' — a genuine cross-tenant authorization check, confirmed live: this batch IS visible in the shared /FastTrack/List, but its validation detail is not.")
    public void getBatchValidationForOtherCompanysBatchReturns403() {
        Response res = claims().get("/FastTrack/batch/" + REAL_BATCH_ID + "/validation");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
        assertThat(res.jsonPath().getString("message")).as("Expected message to contain 'permission' but actual was '%s'. Response body: %s", res.jsonPath().getString("message"), res.asString()).containsIgnoringCase("permission");
    }

    @Test(groups = {"api", "claims", "fast-track"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /FastTrack/Batch/{batchId}/ClosureEligibility for a real batch returns 200 with real closure-eligibility data (isEligible, totalClaims, pendingClaims).")
    public void getBatchClosureEligibilityForRealBatchReturns200() {
        Response res = claims().get("/FastTrack/Batch/" + REAL_BATCH_ID + "/ClosureEligibility");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.jsonPath().getString("response.batchId")).isEqualTo(REAL_BATCH_ID);
    }

    @Test(groups = {"api", "claims", "fast-track"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /FastTrack/Batch/{batchId}/ClosureCounts for a real batch returns 200 with real closure counts (paymentSuccessfulCount, incompleteClaimsCount).")
    public void getBatchClosureCountsForRealBatchReturns200() {
        Response res = claims().get("/FastTrack/Batch/" + REAL_BATCH_ID + "/ClosureCounts");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.jsonPath().getString("response.batchId")).isEqualTo(REAL_BATCH_ID);
    }

    @Test(groups = {"api", "claims", "fast-track"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /FastTrack/SAI/Documents/{batchId}/{docId}/File for a non-existent document returns a real HTTP 404 with a 'Document not found' message.")
    public void getSaiDocumentFileForNonExistentDocReturns404() {
        Response res = claims().get("/FastTrack/SAI/Documents/" + FAKE_ID + "/" + FAKE_ID + "/File");
        ApiAssertions.assertStatusCode(res, 404, "HTTP status");
    }

    @Test(groups = {"api", "claims", "fast-track"})
    @Severity(SeverityLevel.MINOR)
    @Description("POST /FastTrack/Export/Excel with an empty filter returns a real, non-empty .xlsx export.")
    public void exportFastTrackToExcelReturnsFile() {
        Response res = claims().body(Map.of()).post("/FastTrack/Export/Excel");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.asByteArray().length).isGreaterThan(0);
    }

    @Test(groups = {"api", "claims", "fast-track"})
    @Severity(SeverityLevel.MINOR)
    @Description("POST /FastTrack/Export/Pdf with an empty filter returns a non-empty file.")
    public void exportFastTrackToPdfReturnsFile() {
        Response res = claims().body(Map.of()).post("/FastTrack/Export/Pdf");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.asByteArray().length).isGreaterThan(0);
    }

    @Test(groups = {"api", "claims", "fast-track"})
    @Severity(SeverityLevel.MINOR)
    @Description("POST /FastTrack/Export/Csv with an empty filter returns a non-empty file.")
    public void exportFastTrackToCsvReturnsFile() {
        Response res = claims().body(Map.of()).post("/FastTrack/Export/Csv");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.asByteArray().length).isGreaterThan(0);
    }

    /* ── Writes: validation-only / fake-scoped (no real mutation) ────────── */

    @Test(groups = {"api", "claims", "fast-track"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /FastTrack with an empty body fails model validation (HTTP 400): DocumentBase64 and FaultyEntityId are required — no batch is created.")
    public void createFastTrackBatchWithEmptyBodyReturnsValidationError() {
        Response res = claims().body(Map.of()).post("/FastTrack");
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.jsonPath().getMap("errors")).containsKeys("DocumentBase64", "FaultyEntityId");
    }

    @Test(groups = {"api", "claims", "fast-track"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /FastTrack/Save with an empty body fails the same model validation as the plain create.")
    public void saveFastTrackBatchWithEmptyBodyReturnsValidationError() {
        Response res = claims().body(Map.of()).post("/FastTrack/Save");
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.jsonPath().getMap("errors")).containsKeys("DocumentBase64", "FaultyEntityId");
    }

    @Test(groups = {"api", "claims", "fast-track"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /FastTrack/SubmitCreatedBatchToAtFault with an empty body fails model validation (HTTP 400): BatchId is required.")
    public void submitCreatedBatchWithEmptyBodyReturnsValidationError() {
        Response res = claims().body(Map.of()).post("/FastTrack/SubmitCreatedBatchToAtFault");
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.jsonPath().getMap("errors")).containsKey("BatchId");
    }

    @Test(groups = {"api", "claims", "fast-track"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /FastTrack/{batchId} (create a faulty/at-fault-side batch) for a fake batch id with an empty body fails model validation (HTTP 400): DocumentBase64 is required.")
    public void createFaultyFastTrackWithEmptyBodyReturnsValidationError() {
        Response res = claims().body(Map.of()).post("/FastTrack/" + FAKE_ID);
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.jsonPath().getMap("errors")).containsKey("DocumentBase64");
    }

    @Test(groups = {"api", "claims", "fast-track"})
    @Severity(SeverityLevel.MINOR)
    @Description("PUT /FastTrack/{batchId}/AtFaultClaimNumbers — confirmed live unreachable through the gateway on this QA environment (bare HTTP 404), the same shape as ActionApiTest's finding, despite being documented in Swagger.")
    public void updateAtFaultClaimNumbersIsUnreachableOnThisGateway() {
        Response res = claims().body(Map.of()).put("/FastTrack/" + FAKE_ID + "/AtFaultClaimNumbers");
        ApiAssertions.assertStatusCode(res, 404, "HTTP status");
    }

    @Test(groups = {"api", "claims", "fast-track"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /FastTrack/Documents/{claimId} with an empty document array returns a 'No documents provided' envelope failure rather than a 500.")
    public void uploadFastTrackDocumentsWithEmptyArrayReturnsEnvelopeFailure() {
        Response res = claims().body(Collections.emptyList()).post("/FastTrack/Documents/" + FAKE_ID);
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
        assertThat(res.jsonPath().getString("message")).as("Expected message to contain 'No documents' but actual was '%s'. Response body: %s", res.jsonPath().getString("message"), res.asString()).containsIgnoringCase("No documents");
    }

    @Test(groups = {"api", "claims", "fast-track"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /FastTrack/BatchDocuments/{batchId} with an empty document array returns a 'No documents provided' envelope failure.")
    public void uploadFastTrackBatchDocumentsWithEmptyArrayReturnsEnvelopeFailure() {
        Response res = claims().body(Collections.emptyList()).post("/FastTrack/BatchDocuments/" + FAKE_ID);
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "claims", "fast-track"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /FastTrack/BatchDocuments/Zip/{batchId} with an empty file array fails model validation (HTTP 400): at least one file is required.")
    public void uploadFastTrackBatchDocumentsZipWithEmptyArrayReturnsValidationError() {
        Response res = claims().body(Collections.emptyList()).post("/FastTrack/BatchDocuments/Zip/" + FAKE_ID);
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "claims", "fast-track"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /FastTrack/ItemDocuments/{itemId} with an empty document array returns a 'No documents provided' envelope failure.")
    public void uploadFastTrackItemDocumentsWithEmptyArrayReturnsEnvelopeFailure() {
        Response res = claims().body(Collections.emptyList()).post("/FastTrack/ItemDocuments/" + FAKE_ID);
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "claims", "fast-track"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /FastTrack/BatchDocuments/Chunked/Initiate/{batchId} with an empty body fails model validation (HTTP 400): FileName is required — no chunked upload session is created.")
    public void initiateChunkedUploadWithEmptyBodyReturnsValidationError() {
        Response res = claims().body(Map.of()).post("/FastTrack/BatchDocuments/Chunked/Initiate/" + FAKE_ID);
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.jsonPath().getMap("errors")).containsKey("FileName");
    }

    @Test(groups = {"api", "claims", "fast-track"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /FastTrack/RequestDocument with an empty body fails model validation (HTTP 400): ClaimId is required.")
    public void requestDocumentWithEmptyBodyReturnsValidationError() {
        Response res = claims().body(Map.of()).post("/FastTrack/RequestDocument");
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.jsonPath().getMap("errors")).containsKey("ClaimId");
    }

    @Test(groups = {"api", "claims", "fast-track"})
    @Severity(SeverityLevel.NORMAL)
    @Description("PATCH /FastTrack/BatchItems/{batchId} with an empty body returns a 'No updates provided' envelope failure rather than a 500.")
    public void patchBatchItemsWithEmptyBodyReturnsEnvelopeFailure() {
        Response res = claims().body(Map.of()).patch("/FastTrack/BatchItems/" + FAKE_ID);
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
        assertThat(res.jsonPath().getString("message")).as("Expected message to contain 'No updates' but actual was '%s'. Response body: %s", res.jsonPath().getString("message"), res.asString()).containsIgnoringCase("No updates");
    }

    @Test(groups = {"api", "claims", "fast-track"})
    @Severity(SeverityLevel.NORMAL)
    @Description("PATCH /FastTrack/batch/{batchId}/item/{itemId} with an empty body returns a 'No fields provided' envelope failure.")
    public void patchBatchItemWithEmptyBodyReturnsEnvelopeFailure() {
        Response res = claims().body(Map.of()).patch("/FastTrack/batch/" + FAKE_ID + "/item/" + FAKE_ID);
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
        assertThat(res.jsonPath().getString("message")).as("Expected message to contain 'No fields' but actual was '%s'. Response body: %s", res.jsonPath().getString("message"), res.asString()).containsIgnoringCase("No fields");
    }

    @Test(groups = {"api", "claims", "fast-track"})
    @Severity(SeverityLevel.MINOR)
    @Description("DELETE /FastTrack/batch/{batchId}/item/{itemId} for a non-existent batch returns a 'Batch not found' envelope failure.")
    public void deleteBatchItemForNonExistentBatchReturnsEnvelopeFailure() {
        Response res = claims().delete("/FastTrack/batch/" + FAKE_ID + "/item/" + FAKE_ID);
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "claims", "fast-track"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /FastTrack/Batch/{batchId}/Close for a non-existent batch returns a real HTTP 404 'Batch not found'.")
    public void closeBatchForNonExistentBatchReturns404() {
        Response res = claims().body(Map.of()).post("/FastTrack/Batch/" + FAKE_ID + "/Close");
        ApiAssertions.assertStatusCode(res, 404, "HTTP status");
    }

    @Test(groups = {"api", "claims", "fast-track"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /FastTrack/SAI/Submit/{batchId} for a non-existent batch returns a 'Batch not found' envelope failure.")
    public void submitSaiForNonExistentBatchReturnsEnvelopeFailure() {
        Response res = claims().post("/FastTrack/SAI/Submit/" + FAKE_ID);
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "claims", "fast-track"})
    @Severity(SeverityLevel.MINOR)
    @Description("POST /FastTrack/SAI/Item/{itemId}/MissingDocument with a JSON body returns HTTP 415 Unsupported Media Type — confirmed live this endpoint expects multipart/form-data, not application/json.")
    public void reportSaiMissingDocumentWithJsonBodyReturns415() {
        Response res = claims().body(Map.of()).post("/FastTrack/SAI/Item/" + FAKE_ID + "/MissingDocument");
        ApiAssertions.assertStatusCode(res, 415, "HTTP status");
    }

    @Test(groups = {"api", "claims", "fast-track"})
    @Severity(SeverityLevel.NORMAL)
    @Description("PATCH /FastTrack/Item/{itemId}/FieldCorrection with an empty body fails model validation (HTTP 400): NewValue and FieldName are required.")
    public void fieldCorrectionWithEmptyBodyReturnsValidationError() {
        Response res = claims().body(Map.of()).patch("/FastTrack/Item/" + FAKE_ID + "/FieldCorrection");
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.jsonPath().getMap("errors")).containsKeys("NewValue", "FieldName");
    }

    @Test(groups = {"api", "claims", "fast-track"})
    @Severity(SeverityLevel.MINOR)
    @Description("POST /FastTrack/Claim/{claimId}/ReevaluateApproval for a non-existent claim returns a 'Claim not found' envelope failure.")
    public void reevaluateApprovalForNonExistentClaimReturnsEnvelopeFailure() {
        Response res = claims().post("/FastTrack/Claim/" + FAKE_ID + "/ReevaluateApproval");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "claims", "fast-track"})
    @Severity(SeverityLevel.NORMAL)
    @Description("PUT /FastTrack/Claim/{claimId}/AtFaultDetails with an empty body fails model validation (HTTP 400): AtFaultClaimNumber is required.")
    public void updateAtFaultDetailsWithEmptyBodyReturnsValidationError() {
        Response res = claims().body(Map.of()).put("/FastTrack/Claim/" + FAKE_ID + "/AtFaultDetails");
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.jsonPath().getMap("errors")).containsKey("AtFaultClaimNumber");
    }

    @Test(groups = {"api", "claims", "fast-track"})
    @Severity(SeverityLevel.MINOR)
    @Description("POST /FastTrack/ExportBatch/Excel/{batchId} for a real batch with an empty filter returns a non-empty file.")
    public void exportBatchToExcelReturnsFile() {
        Response res = claims().body(Map.of()).post("/FastTrack/ExportBatch/Excel/" + REAL_BATCH_ID);
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.asByteArray().length).isGreaterThan(0);
    }

    @Test(groups = {"api", "claims", "fast-track"})
    @Severity(SeverityLevel.MINOR)
    @Description("POST /FastTrack/ExportBatch/Pdf/{batchId} for a real batch returns a non-empty file.")
    public void exportBatchToPdfReturnsFile() {
        Response res = claims().body(Map.of()).post("/FastTrack/ExportBatch/Pdf/" + REAL_BATCH_ID);
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.asByteArray().length).isGreaterThan(0);
    }

    @Test(groups = {"api", "claims", "fast-track"})
    @Severity(SeverityLevel.MINOR)
    @Description("POST /FastTrack/ExportBatch/Csv/{batchId} for a real batch returns a non-empty file.")
    public void exportBatchToCsvReturnsFile() {
        Response res = claims().body(Map.of()).post("/FastTrack/ExportBatch/Csv/" + REAL_BATCH_ID);
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.asByteArray().length).isGreaterThan(0);
    }

    /* ── Writes: real mutations against real batches — disabled stubs ───── */

    @Test(enabled = false, groups = {"api", "claims", "fast-track", "mutating"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("STUB — creates a real Fast Track batch from a real Excel file (base64-encoded). Requires a real, valid workbook fixture (see src/main/resources/Fast Track333.xlsx) and a real faulty entity id.")
    public void createFastTrackBatchWithValidPayloadReturns200() {
        Response res = claims().body(Map.of("faultyEntityId", "placeholder", "documentBase64", "placeholder")).post("/FastTrack");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
    }

    @Test(enabled = false, groups = {"api", "claims", "fast-track", "mutating"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("STUB — submits a real, created Fast Track batch to the At-Fault insurer, notifying them (see MOSADAD_DOMAIN.md §Fast Track). Enable only against a disposable batch.")
    public void submitCreatedBatchToAtFaultWithValidPayloadReturns200() {
        Response res = claims().body(Map.of("batchId", "placeholder")).post("/FastTrack/SubmitCreatedBatchToAtFault");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
    }

    @Test(enabled = false, groups = {"api", "claims", "fast-track", "mutating"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("STUB — closes a real Fast Track batch (terminal state). Enable only against a disposable batch that has actually met closure eligibility.")
    public void closeBatchWithValidPayloadReturns200() {
        Response res = claims().body(Map.of()).post("/FastTrack/Batch/placeholder-batch-id/Close");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
    }

    @Test(enabled = false, groups = {"api", "claims", "fast-track", "mutating"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — uploads a real document to a real Fast Track batch/claim/item. Requires a real file fixture; not exercised without one.")
    public void uploadDocumentsWithRealFileReturns200() {
        Response res = claims().body(List.of(Map.of())).post("/FastTrack/Documents/placeholder-claim-id");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
    }
}
