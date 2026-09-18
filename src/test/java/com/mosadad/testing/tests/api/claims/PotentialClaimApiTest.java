package com.mosadad.testing.tests.api.claims;

import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * claims service — PotentialClaim tag. Accident records surfaced from
 * inthub as candidates before a Claimant Insurer turns them into a real
 * Recovery Claim (see MOSADAD_DOMAIN.md §Stage 1 — the "Potential Recovery
 * Claims" list on the dashboard). "Start"/"RestartClaim" promote a
 * potential claim into a real one.
 */
@Epic("Mosadad Recovery Claim")
@Feature("Claims API — PotentialClaim")
public class PotentialClaimApiTest extends BaseApiTest {

    @Test(groups = {"api", "claims", "potential-claim"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /PotentialClaim/List with an empty filter returns 200 with real, non-empty potential claim records.")
    public void listPotentialClaimsReturns200() {
        Response res = claims().body(Map.of()).post("/PotentialClaim/List");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.jsonPath().getList("response.items")).isNotEmpty();
    }

    @Test(groups = {"api", "claims", "potential-claim"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /PotentialClaim/{claimID} for a non-existent id returns a 'Claim not found' envelope failure, not a 500.")
    public void getPotentialClaimByNonExistentIdReturnsEnvelopeFailure() {
        Response res = claims().get("/PotentialClaim/000000000000000000000000");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
        assertThat(res.jsonPath().getString("message")).as("Expected message to contain 'not found' but actual was '%s'. Response body: %s", res.jsonPath().getString("message"), res.asString()).containsIgnoringCase("not found");
    }

    @Test(groups = {"api", "claims", "potential-claim"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /PotentialClaim/ListStatistics/{type} returns 200 with real aggregate stats: totalNumberOfClaims, most-frequent counterpart companies, and a 12-month breakdown.")
    public void getPotentialClaimStatisticsReturns200() {
        Response res = claims().get("/PotentialClaim/ListStatistics/1");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.jsonPath().getInt("response.totalNumberOfClaims")).isGreaterThan(0);
        assertThat(res.jsonPath().getList("response.monthlyCounts")).hasSize(12);
    }

    @Test(groups = {"api", "claims", "potential-claim"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /PotentialClaim/AdvancedSearchList with an empty filter returns 200 with real recoverable/payable potential claim buckets.")
    public void advancedSearchPotentialClaimsReturns200() {
        Response res = claims().body(Map.of()).post("/PotentialClaim/AdvancedSearchList");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.jsonPath().getList("response.recoverableClaims.items")).isNotNull();
    }

    @Test(groups = {"api", "claims", "potential-claim"})
    @Severity(SeverityLevel.MINOR)
    @Description("POST /PotentialClaim/Export/Excel with an empty filter returns a real, non-empty .xlsx export.")
    public void exportPotentialClaimsToExcelReturnsFile() {
        Response res = claims().body(Map.of()).post("/PotentialClaim/Export/Excel");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.asByteArray().length).isGreaterThan(0);
    }

    @Test(groups = {"api", "claims", "potential-claim"})
    @Severity(SeverityLevel.MINOR)
    @Description("POST /PotentialClaim/Export/Pdf with an empty filter returns a non-empty file, same read-only export contract as Export/Excel.")
    public void exportPotentialClaimsToPdfReturnsFile() {
        Response res = claims().body(Map.of()).post("/PotentialClaim/Export/Pdf");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.asByteArray().length).isGreaterThan(0);
    }

    @Test(groups = {"api", "claims", "potential-claim"})
    @Severity(SeverityLevel.MINOR)
    @Description("POST /PotentialClaim/Export/Csv with an empty filter returns a non-empty file, same read-only export contract as Export/Excel.")
    public void exportPotentialClaimsToCsvReturnsFile() {
        Response res = claims().body(Map.of()).post("/PotentialClaim/Export/Csv");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.asByteArray().length).isGreaterThan(0);
    }

    @Test(groups = {"api", "claims", "potential-claim"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /PotentialClaim with an empty array returns an envelope failure (statusCode 500) rather than silently creating nothing — confirmed live, no potential claims are created either way.")
    public void createPotentialClaimsWithEmptyArrayReturnsEnvelopeFailure() {
        Response res = claims().body(Collections.emptyList()).post("/PotentialClaim");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "claims", "potential-claim"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /PotentialClaim/ManualClaim with an empty body fails model validation (HTTP 400), flagging AccidentId, AccidentTime, AccidentNumber, AccidentEmirate and both parties' data/attachments as required — no claim is created.")
    public void createManualPotentialClaimWithEmptyBodyReturnsValidationError() {
        Response res = claims().body(Map.of()).post("/PotentialClaim/ManualClaim");
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.jsonPath().getMap("errors")).containsKeys("AccidentId", "AccidentTime", "AccidentNumber", "AccidentEmirate");
    }

    @Test(groups = {"api", "claims", "potential-claim"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /PotentialClaim/Start/{ClaimID} with an empty body fails model validation (HTTP 400): RequiredDocuments and ClaimantClaimNumber are required — checked before the fake id is even looked up.")
    public void startPotentialClaimWithEmptyBodyReturnsValidationError() {
        Response res = claims().body(Map.of()).post("/PotentialClaim/Start/000000000000000000000000");
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.jsonPath().getMap("errors")).containsKeys("RequiredDocuments", "ClaimantClaimNumber");
    }

    @Test(groups = {"api", "claims", "potential-claim"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /PotentialClaim/RestartClaim/{ClaimID} with an empty body fails the same model validation as Start.")
    public void restartPotentialClaimWithEmptyBodyReturnsValidationError() {
        Response res = claims().body(Map.of()).post("/PotentialClaim/RestartClaim/000000000000000000000000");
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.jsonPath().getMap("errors")).containsKeys("RequiredDocuments", "ClaimantClaimNumber");
    }

    @Test(enabled = false, groups = {"api", "claims", "potential-claim", "mutating"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — POST /PotentialClaim/Create/OCRManualClaim — OCR-driven claim creation from a scanned police report image. Requires a real image upload; not exercised without a real fixture file and sign-off.")
    public void createOcrManualClaimReturns200() {
        Response res = claims().body(Map.of()).post("/PotentialClaim/Create/OCRManualClaim");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
    }

    @Test(enabled = false, groups = {"api", "claims", "potential-claim", "mutating"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("STUB — promotes a real potential claim into a Stage-1-registered recovery claim (see MOSADAD_DOMAIN.md §Stage 1). Enable only against a disposable potential claim.")
    public void startPotentialClaimWithValidPayloadReturns200() {
        Response res = claims().body(Map.of("claimantClaimNumber", "QA-TEST")).post("/PotentialClaim/Start/placeholder-claim-id");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
    }
}
