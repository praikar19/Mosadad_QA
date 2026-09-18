package com.mosadad.testing.tests.api.inthub;

import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * inthub service — Accident tag. Accident records sourced from UAE Police
 * authorities — see MOSADAD_DOMAIN.md §Stage 1 "Police Data Entry": Dubai
 * Police, Rafid, and Saeed are named integrations. {@code /Accident/Rafid}
 * and {@code /Accident/Saeed} call out to those real external government
 * systems for a genuine police report number, so their happy path stays a
 * disabled stub — only their request-validation branch (which short-circuits
 * before any external call, confirmed live) is exercised for real.
 */
@Epic("Mosadad Recovery Claim")
@Feature("IntHub API — Accident")
public class AccidentApiTest extends BaseApiTest {

    @Test(groups = {"api", "inthub", "accident"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /Accident/{accidentID} for a well-formed but non-existent id returns an 'Accident not found' envelope failure, not a 500.")
    public void getAccidentByNonExistentIdReturnsEnvelopeFailure() {
        Response res = inthub().get("/Accident/000000000000000000000000");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
        assertThat(res.jsonPath().getString("message")).as("Expected message to contain 'not found' but actual was '%s'. Response body: %s", res.jsonPath().getString("message"), res.asString()).containsIgnoringCase("not found");
    }

    @Test(groups = {"api", "inthub", "accident"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /Accident/View/{accidentID} for a well-formed but non-existent id returns the same graceful 'not found' envelope as the plain GetById.")
    public void viewAccidentByNonExistentIdReturnsEnvelopeFailure() {
        Response res = inthub().get("/Accident/View/000000000000000000000000");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "inthub", "accident"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /Accident/PhotosZip/{accidentID} for a non-existent id returns a real (transport-level) HTTP 404 — confirmed live this one diverges from the usual HTTP-200-envelope pattern.")
    public void getAccidentPhotosZipForNonExistentIdReturnsHttp404() {
        Response res = inthub().get("/Accident/PhotosZip/000000000000000000000000");
        ApiAssertions.assertStatusCode(res, 404, "HTTP status");
    }

    @Test(groups = {"api", "inthub", "accident"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Accident/Manual with an empty body fails model validation (HTTP 400), flagging Vehicles, EmirateCode, AccidentType, ReportNumber and ClaimantClaimNumber as required — no accident is created.")
    public void createManualAccidentWithEmptyBodyReturnsValidationError() {
        Response res = inthub().body(Map.of()).post("/Accident/Manual");
        ApiAssertions.assertValidationProblem(res, "Vehicles", "EmirateCode", "AccidentType", "ReportNumber", "ClaimantClaimNumber");
    }

    @Test(groups = {"api", "inthub", "accident"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Accident/Rafid with an empty body fails model validation (HTTP 400): Data is required — confirmed live this short-circuits before calling the real Rafid police system.")
    public void createRafidAccidentWithEmptyBodyReturnsValidationError() {
        Response res = inthub().body(Map.of()).post("/Accident/Rafid");
        ApiAssertions.assertValidationProblem(res, "Data");
    }

    @Test(groups = {"api", "inthub", "accident"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Accident/Saeed with an empty array returns an envelope failure ('Request is null or empty') rather than a 500 — confirmed live this short-circuits before calling the real Saeed police system.")
    public void createSaeedAccidentWithEmptyArrayReturnsEnvelopeFailure() {
        Response res = inthub().body(Collections.emptyList()).post("/Accident/Saeed");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
        assertThat(res.jsonPath().getString("message")).as("Expected message to contain 'null or empty' but actual was '%s'. Response body: %s", res.jsonPath().getString("message"), res.asString()).containsIgnoringCase("null or empty");
    }

    @Test(groups = {"api", "inthub", "accident"})
    @Severity(SeverityLevel.NORMAL)
    @Description("PUT /Accident/UpdateCompanyData/{accidentID} with an empty body against a non-existent accident returns an envelope failure (HTTP 200, isSuccess:false) rather than crashing the caller — confirmed live the backend reports an unexpected-error envelope, not a raw 500 stack trace.")
    public void updateCompanyDataForNonExistentAccidentReturnsEnvelopeFailure() {
        Response res = inthub().body(Map.of()).put("/Accident/UpdateCompanyData/000000000000000000000000");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(enabled = false, groups = {"api", "inthub", "accident", "mutating", "external"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — a real Manual accident create persists a new accident record. Enable with a fully-populated ManualAccidentDTO (vehicles, emirate, accident/report numbers) agreed with QA.")
    public void createManualAccidentWithValidPayloadReturns200() {
        Response res = inthub().body(Map.of("reportNumber", "QA-AUTOMATION-ACCIDENT")).post("/Accident/Manual");
        ApiAssertions.assertEnvelopeSuccess(res);
    }

    @Test(enabled = false, groups = {"api", "inthub", "accident", "mutating", "external"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — calls the real, external Rafid UAE Police system with a genuine report reference. Requires an authorized Rafid test report number; never run with fabricated data against a live government integration.")
    public void createRafidAccidentWithRealReportDataReturns200() {
        Response res = inthub().body(Map.of("data", "placeholder-rafid-payload")).post("/Accident/Rafid");
        ApiAssertions.assertEnvelopeSuccess(res);
    }

    @Test(enabled = false, groups = {"api", "inthub", "accident", "mutating", "external"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — calls the real, external Saeed UAE Police system with a genuine report reference. Requires an authorized Saeed test report number; never run with fabricated data against a live government integration.")
    public void createSaeedAccidentWithRealReportDataReturns200() {
        Response res = inthub().body(java.util.List.of(Map.of("reportNumber", "placeholder"))).post("/Accident/Saeed");
        ApiAssertions.assertEnvelopeSuccess(res);
    }

    @Test(enabled = false, groups = {"api", "inthub", "accident", "mutating"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — overwrites a real accident's company-supplied data. Enable only against a disposable accident record.")
    public void updateCompanyDataWithValidPayloadReturns200() {
        Response res = inthub().body(Map.of()).put("/Accident/UpdateCompanyData/placeholder-accident-id");
        ApiAssertions.assertEnvelopeSuccess(res);
    }
}
