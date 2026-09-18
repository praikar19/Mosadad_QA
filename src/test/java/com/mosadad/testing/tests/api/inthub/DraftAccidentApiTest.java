package com.mosadad.testing.tests.api.inthub;

import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * inthub service — DraftAccident tag. A draft/staging accident record
 * before it's attached to a real recovery claim — see MOSADAD_DOMAIN.md
 * §Stage 1 "Police Data Entry" and "Manual Entry".
 *
 * <p>Confirmed live 2026-09-17: despite the path parameter being named
 * {@code {reportNumber}}, {@code GET /DraftAccident/{reportNumber}} actually
 * expects the draft's own Mongo id, not its human-readable police report
 * number (a real reportNumber like "223010383622111111432421" 404s; the
 * matching record's {@code id} field works).
 */
@Epic("Mosadad Recovery Claim")
@Feature("IntHub API — DraftAccident")
public class DraftAccidentApiTest extends BaseApiTest {

    @Test(groups = {"api", "inthub", "draft-accident"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /DraftAccident/List with an empty filter returns 200 with a real, non-empty list of draft accident records.")
    public void listDraftAccidentsWithEmptyFilterReturns200() {
        Response res = inthub().body(Map.of()).post("/DraftAccident/List");
        ApiAssertions.assertEnvelopeSuccess(res);
        List<Map<String, Object>> items = res.jsonPath().getList("response.items");
        assertThat(items).isNotEmpty();
        assertThat(items.get(0)).containsKeys("id", "reportNumber");
    }

    @Test(groups = {"api", "inthub", "draft-accident"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /DraftAccident/{id} for a real id (discovered from DraftAccident/List) returns 200 with the full accident detail, including the same reportNumber shown in the list.")
    public void getDraftAccidentByRealIdReturns200() {
        Response listRes = inthub().body(Map.of()).post("/DraftAccident/List");
        String id = listRes.jsonPath().getString("response.items[0].id");
        String reportNumber = listRes.jsonPath().getString("response.items[0].reportNumber");

        Response res = inthub().get("/DraftAccident/" + id);
        ApiAssertions.assertEnvelopeSuccess(res);
        assertThat(res.jsonPath().getString("response.reportNumber")).isEqualTo(reportNumber);
    }

    @Test(groups = {"api", "inthub", "draft-accident"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /DraftAccident/{id} for a well-formed but non-existent id returns an 'Accident not found' envelope failure, not a 500.")
    public void getDraftAccidentWithNonExistentIdReturnsEnvelopeFailure() {
        Response res = inthub().get("/DraftAccident/000000000000000000000000");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
        assertThat(res.jsonPath().getString("message")).as("Expected message to contain 'not found' but actual was '%s'. Response body: %s", res.jsonPath().getString("message"), res.asString()).containsIgnoringCase("not found");
    }

    @Test(groups = {"api", "inthub", "draft-accident"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /DraftAccident with an empty body fails model validation (HTTP 400), flagging Vehicles, EmirateCode, AccidentType, ReportNumber and ClaimantClaimNumber as required — no draft is persisted.")
    public void createDraftAccidentWithEmptyBodyReturnsValidationError() {
        Response res = inthub().body(Map.of()).post("/DraftAccident");
        ApiAssertions.assertValidationProblem(res, "Vehicles", "EmirateCode", "AccidentType", "ReportNumber", "ClaimantClaimNumber");
    }

    @Test(groups = {"api", "inthub", "draft-accident"})
    @Severity(SeverityLevel.MINOR)
    @Description("POST /DraftAccident/Export/Excel with an empty filter returns a non-empty file — a genuinely read-only export, safe to run live.")
    public void exportDraftAccidentsToExcelReturnsFile() {
        Response res = inthub().body(Map.of()).post("/DraftAccident/Export/Excel");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.asByteArray().length).isGreaterThan(0);
    }

    @Test(groups = {"api", "inthub", "draft-accident"})
    @Severity(SeverityLevel.MINOR)
    @Description("POST /DraftAccident/Export/Pdf with an empty filter returns a non-empty file, same read-only export contract as Export/Excel.")
    public void exportDraftAccidentsToPdfReturnsFile() {
        Response res = inthub().body(Map.of()).post("/DraftAccident/Export/Pdf");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.asByteArray().length).isGreaterThan(0);
    }

    @Test(groups = {"api", "inthub", "draft-accident"})
    @Severity(SeverityLevel.MINOR)
    @Description("POST /DraftAccident/Export/Csv with an empty filter returns a non-empty file, same read-only export contract as Export/Excel.")
    public void exportDraftAccidentsToCsvReturnsFile() {
        Response res = inthub().body(Map.of()).post("/DraftAccident/Export/Csv");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.asByteArray().length).isGreaterThan(0);
    }

    @Test(enabled = false, groups = {"api", "inthub", "draft-accident", "mutating"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — creates a real draft accident record in the shared QA data. Enable with a disposable, fully-populated ManualAccidentDTO payload (vehicles, emirate, accident type, report/claim numbers) agreed with QA.")
    public void createDraftAccidentWithValidPayloadReturns200() {
        Response res = inthub().body(Map.of("reportNumber", "QA-AUTOMATION-DRAFT")).post("/DraftAccident");
        ApiAssertions.assertEnvelopeSuccess(res);
    }
}
