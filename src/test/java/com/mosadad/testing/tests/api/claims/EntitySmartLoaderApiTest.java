package com.mosadad.testing.tests.api.claims;

import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** claims service — EntitySmartLoader tag. Per-entity field-mapping config for bulk/smart data import (maps internal fields like "claim_number" to an insurer's own external column names — e.g. "Claim No.", "Accident Number") — supports Fast Track's Excel import (see MOSADAD_DOMAIN.md §Fast Track). */
@Epic("Mosadad Recovery Claim")
@Feature("Claims API — EntitySmartLoader")
public class EntitySmartLoaderApiTest extends BaseApiTest {

    @Test(groups = {"api", "claims", "entity-smart-loader"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /EntitySmartLoader/GetByEntityId for the logged-in entity returns 200 with real field-mapping config (internalField/externalField pairs).")
    public void getSmartLoaderForOwnEntityReturns200() {
        Response res = claims().get("/EntitySmartLoader/GetByEntityId");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", true, "envelope isSuccess");
        assertThat(res.jsonPath().getList("response.smartLoaders")).isNotEmpty();
    }

    @Test(groups = {"api", "claims", "entity-smart-loader"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /EntitySmartLoader/GetById/{id} for a non-existent id returns a 'not found' envelope failure.")
    public void getSmartLoaderByNonExistentIdReturnsEnvelopeFailure() {
        Response res = claims().get("/EntitySmartLoader/GetById/000000000000000000000000");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
        assertThat(res.jsonPath().getString("message")).as("Expected message to contain 'not found' but actual was '%s'. Response body: %s", res.jsonPath().getString("message"), res.asString()).containsIgnoringCase("not found");
    }

    @Test(groups = {"api", "claims", "entity-smart-loader"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /EntitySmartLoader/Create with an empty body fails model validation (HTTP 400): SmartLoaders is required — no mapping is created.")
    public void createSmartLoaderWithEmptyBodyReturnsValidationError() {
        Response res = claims().body(java.util.Map.of()).post("/EntitySmartLoader/Create");
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.jsonPath().getMap("errors")).containsKey("SmartLoaders");
    }

    @Test(groups = {"api", "claims", "entity-smart-loader"})
    @Severity(SeverityLevel.NORMAL)
    @Description("PUT /EntitySmartLoader/Edit with an empty body fails model validation (HTTP 400): Id, ExternalField and InternalField are required.")
    public void editSmartLoaderWithEmptyBodyReturnsValidationError() {
        Response res = claims().body(java.util.Map.of()).put("/EntitySmartLoader/Edit");
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.jsonPath().getMap("errors")).containsKeys("Id", "ExternalField", "InternalField");
    }

    @Test(enabled = false, groups = {"api", "claims", "entity-smart-loader", "mutating"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — adds real field mappings to the logged-in entity's live smart-loader config. Enable only against a disposable entity/field.")
    public void createSmartLoaderWithValidPayloadReturns200() {
        Response res = claims().body(java.util.Map.of("smartLoaders", java.util.List.of())).post("/EntitySmartLoader/Create");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
    }

    @Test(enabled = false, groups = {"api", "claims", "entity-smart-loader", "mutating", "destructive"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — DELETE /EntitySmartLoader/Delete/{smartLoaderId} permanently removes a real field mapping. Enable only against a disposable mapping.")
    public void deleteSmartLoaderByIdReturns200() {
        Response res = claims().delete("/EntitySmartLoader/Delete/placeholder-id");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
    }

    @Test(enabled = false, groups = {"api", "claims", "entity-smart-loader", "mutating", "destructive", "unscoped"})
    @Severity(SeverityLevel.BLOCKER)
    @Description("STUB — DELETE /EntitySmartLoader/Delete (no id) — an unscoped delete, same risky shape as the AcceptAll/Config-Update family. Never enable against the shared QA environment without explicit sign-off, and without first confirming exactly what an id-less delete targets.")
    public void deleteSmartLoaderUnscoped_NeverRunWithoutSignOff() {
        Response res = claims().delete("/EntitySmartLoader/Delete");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
    }
}
