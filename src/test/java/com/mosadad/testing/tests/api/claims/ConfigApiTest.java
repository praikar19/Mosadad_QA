package com.mosadad.testing.tests.api.claims;

import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * claims service — Config tag. Per-entity SLA/config settings — the
 * platform-configurable backing for MOSADAD_DOMAIN.md's SLA windows (72h
 * Normal Repair / 168h Total Loss) and Fast Track approval thresholds.
 *
 * <p><b>Caution — {@code POST /Config/Update} is another unscoped-body-only
 * write</b> in the same family as {@code QuotationApiTest}'s
 * {@code AcceptAll}: confirmed live an empty body returns 200
 * {@code "Config updated successfully."} rather than a validation error or
 * a no-op. It is never called by this suite, not even with an empty body —
 * only a permanently-disabled stub documents it.
 *
 * <p>Also confirmed live: the logged-in Super Admin QA account does NOT
 * hold the {@code read:fast-track-threshold} permission — every
 * FastTrackApprovalThreshold read returns a real HTTP 403 with that exact
 * permission name in its errors array, a genuine "authenticated but
 * forbidden" case distinct from the 401s elsewhere in this suite.
 */
@Epic("Mosadad Recovery Claim")
@Feature("Claims API — Config")
public class ConfigApiTest extends BaseApiTest {

    @Test(groups = {"api", "claims", "config"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /Config/Get/{Type} for a real config type (1) returns 200 with the SLA/smart-loader config, including systemColumns for smart loaders.")
    public void getConfigByRealTypeReturns200() {
        Response res = claims().get("/Config/Get/1");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", true, "envelope isSuccess");
        assertThat(res.jsonPath().getString("response.id")).isNotBlank();
    }

    @Test(groups = {"api", "claims", "config"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /Config/GetSlaPeriods returns 200 with the real SLA period config for every stage (Claim, Quotation, Invoice, Settlement, ExtraExpenses).")
    public void getSlaPeriodsReturns200() {
        Response res = claims().get("/Config/GetSlaPeriods");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.jsonPath().getMap("response")).containsKeys("Claim", "Quotation", "Invoice", "Settlement", "ExtraExpenses");
    }

    @Test(groups = {"api", "claims", "config"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /Config/SlaItem/{slaItemId} for a non-existent id returns 200 with a default/empty SLA item shape rather than a 404 — confirmed live.")
    public void getSlaItemForNonExistentIdReturnsDefaultShape() {
        Response res = claims().get("/Config/SlaItem/000000000000000000000000");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", true, "envelope isSuccess");
    }

    @Test(groups = {"api", "claims", "config", "authorization"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /Config/FastTrackApprovalThreshold/Default returns a real HTTP 403 for the logged-in QA account — it lacks the 'read:fast-track-threshold' permission. A genuine authenticated-but-forbidden case, confirmed live and distinct from every 401 elsewhere in this suite.")
    public void getDefaultFastTrackThresholdWithoutPermissionReturns403() {
        Response res = claims().get("/Config/FastTrackApprovalThreshold/Default");
        ApiAssertions.assertStatusCode(res, 403, "HTTP status");
        assertThat(res.jsonPath().getString("message")).as("Expected message to contain 'read:fast-track-threshold' but actual was '%s'. Response body: %s", res.jsonPath().getString("message"), res.asString()).contains("read:fast-track-threshold");
    }

    @Test(groups = {"api", "claims", "config", "authorization"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /Config/FastTrackApprovalThreshold/{entityId} for the logged-in entity returns the same 403 permission denial as the Default variant.")
    public void getEntityFastTrackThresholdWithoutPermissionReturns403() {
        Response res = claims().get("/Config/FastTrackApprovalThreshold/" + entityId());
        ApiAssertions.assertStatusCode(res, 403, "HTTP status");
    }

    @Test(groups = {"api", "claims", "config"})
    @Severity(SeverityLevel.MINOR)
    @Description("DELETE /Config/Delete/{configId}/{slaItemId} for non-existent, well-formed ids returns an envelope failure (statusCode 500), not a raw crash.")
    public void deleteSlaItemForNonExistentIdsReturnsEnvelopeFailure() {
        Response res = claims().delete("/Config/Delete/000000000000000000000000/000000000000000000000000");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "claims", "config"})
    @Severity(SeverityLevel.MINOR)
    @Description("PUT /Config/ChangeStatus/{configId}/{slaItemId} for non-existent, well-formed ids returns an envelope failure (statusCode 500), not a raw crash.")
    public void changeSlaItemStatusForNonExistentIdsReturnsEnvelopeFailure() {
        Response res = claims().put("/Config/ChangeStatus/000000000000000000000000/000000000000000000000000");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(enabled = false, groups = {"api", "claims", "config", "mutating", "destructive", "unscoped"})
    @Severity(SeverityLevel.BLOCKER)
    @Description("STUB — POST /Config/Update is unscoped (no id) and confirmed live to write a real config change even with an empty body. Never enable against the shared QA environment without explicit sign-off.")
    public void updateConfigUnscopedIsRisky_NeverRunWithoutSignOff() {
        Response res = claims().body(java.util.Map.of()).post("/Config/Update");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
    }

    @Test(enabled = false, groups = {"api", "claims", "config", "mutating", "authorization"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — sets a real Fast Track approval threshold for an entity. Requires a QA account holding write:fast-track-threshold (the current Super Admin QA login does not); enable once that permission is granted.")
    public void updateFastTrackThresholdReturns200() {
        Response res = claims().body(java.util.Map.of()).put("/Config/FastTrackApprovalThreshold/Default");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
    }
}
