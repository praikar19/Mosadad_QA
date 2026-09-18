package com.mosadad.testing.tests.api.claims;

import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * claims service — Dashboard tag. The largest tag in the API (66
 * operations) — every widget behind the Entity, Admin and Regulator
 * dashboards (see MOSADAD_DOMAIN.md's "Verified vs Stubbed" section: Claims
 * Report, SLA Violation, and every ranking/export report reachable from
 * them). Systematically probed live 2026-09-17 (see class-level findings
 * below) rather than hand-tested one at a time — 66 endpoints share only a
 * handful of real shapes:
 *
 * <ul>
 *   <li><b>Plain reports</b> (GET with optional period/date query params,
 *       or POST with an empty filter body) — all return real 200 data.</li>
 *   <li><b>Exports</b> — POST with body {@code {"exportFormat":"Excel"|"Pdf"|"Csv"}}
 *       (confirmed live: a string enum, "xlsx" is rejected) return a real
 *       non-empty file for 26 of 29 export endpoints.</li>
 *   <li><b>Admin/Regulator financial reports</b> (4 non-export +
 *       5 export variants) require real entity/counterpart ids
 *       (EntityIds, or AtFaultIds+ClaimantId) beyond just exportFormat —
 *       validation-only here.</li>
 *   <li><b>3 confirmed backend bugs</b> (real HTTP 500, not the usual
 *       envelope) — see {@code exportAdminTasksOverdueReportThrowsServerError}
 *       and siblings below.</li>
 * </ul>
 */
@Epic("Mosadad Recovery Claim")
@Feature("Claims API — Dashboard")
public class DashboardApiTest extends BaseApiTest {

    private static final Map<String, Object> EXCEL = Map.of("exportFormat", "Excel");

    private void assertReportOk(Response res) {
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", true, "envelope isSuccess");
    }

    private void assertExportOk(Response res) {
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.asByteArray().length).isGreaterThan(0);
    }

    /* ── Plain GET reports ─────────────────────────────────────────────── */

    @Test(groups = {"api", "claims", "dashboard"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /Dashboard returns 200 with the real Entity dashboard summary — the exact call the live dashboard makes on load (see FRAMEWORK.md's captured network traffic).")
    public void getEntityDashboardReturns200() { assertReportOk(claims().get("/Dashboard")); }

    @Test(groups = {"api", "claims", "dashboard", "bug"})
    @Severity(SeverityLevel.NORMAL)
    @Description("BUG, confirmed live — GET /Dashboard/GetRegulatorDashboard returns HTTP 200 but an envelope failure ('Error something went wrong', inner statusCode 500) for the current QA login, not real regulator data.")
    public void getRegulatorDashboardThrowsEnvelopeError() {
        Response res = claims().get("/Dashboard/GetRegulatorDashboard");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "claims", "dashboard"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /Dashboard/PotentialClaims returns 200 with real potential-claims dashboard data.")
    public void getDashboardPotentialClaimsReturns200() { assertReportOk(claims().get("/Dashboard/PotentialClaims")); }

    @Test(groups = {"api", "claims", "dashboard"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /Dashboard/{entityId} for the logged-in entity returns 200 with the same shape as the entity-less variant.")
    public void getDashboardForOwnEntityReturns200() { assertReportOk(claims().get("/Dashboard/" + entityId())); }

    @Test(groups = {"api", "claims", "dashboard"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /Dashboard/PotentialClaims/{entityId} for the logged-in entity returns 200.")
    public void getDashboardPotentialClaimsForOwnEntityReturns200() { assertReportOk(claims().get("/Dashboard/PotentialClaims/" + entityId())); }

    @Test(groups = {"api", "claims", "dashboard"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /Dashboard/NetPayableReceivable returns 200 with real net payable/receivable position.")
    public void getNetPayableReceivableReturns200() { assertReportOk(claims().get("/Dashboard/NetPayableReceivable")); }

    @Test(groups = {"api", "claims", "dashboard"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /Dashboard/RecoveredPaidKpi returns 200 with real recovered/paid KPI data.")
    public void getRecoveredPaidKpiReturns200() { assertReportOk(claims().get("/Dashboard/RecoveredPaidKpi")); }

    @Test(groups = {"api", "claims", "dashboard"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /Dashboard/RecoveredPaidKpi/{entityId} for the logged-in entity returns 200.")
    public void getRecoveredPaidKpiForOwnEntityReturns200() { assertReportOk(claims().get("/Dashboard/RecoveredPaidKpi/" + entityId())); }

    @Test(groups = {"api", "claims", "dashboard"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /Dashboard/RecoverablePayableValues returns 200 with the real recoverable/payable value totals — feeds the dashboard's headline KPI cards (see FRAMEWORK.md: 'Total Recoverable Value' / 'Total Payable Value').")
    public void getRecoverablePayableValuesReturns200() { assertReportOk(claims().get("/Dashboard/RecoverablePayableValues")); }

    @Test(groups = {"api", "claims", "dashboard"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /Dashboard/RecoverablePayableValues/{entityId} for the logged-in entity returns 200.")
    public void getRecoverablePayableValuesForOwnEntityReturns200() { assertReportOk(claims().get("/Dashboard/RecoverablePayableValues/" + entityId())); }

    /* ── Plain POST reports (empty filter body) ──────────────────────────── */

    @Test(groups = {"api", "claims", "dashboard"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Dashboard/RegulatorRecoveryExposure with an empty body returns 200 with real regulator exposure data.")
    public void getRegulatorRecoveryExposureReturns200() { assertReportOk(claims().body(Map.of()).post("/Dashboard/RegulatorRecoveryExposure")); }

    @Test(groups = {"api", "claims", "dashboard"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Dashboard/CompanyStatistics with an empty body returns 200 with real per-company statistics.")
    public void getCompanyStatisticsReturns200() { assertReportOk(claims().body(Map.of()).post("/Dashboard/CompanyStatistics")); }

    @Test(groups = {"api", "claims", "dashboard"})
    @Severity(SeverityLevel.MINOR)
    @Description("POST /Dashboard/CompanyStatistics/Details with an empty body returns 200 with the drill-down detail behind CompanyStatistics.")
    public void getCompanyStatisticsDetailsReturns200() { assertReportOk(claims().body(Map.of()).post("/Dashboard/CompanyStatistics/Details")); }

    @Test(groups = {"api", "claims", "dashboard", "bug"})
    @Severity(SeverityLevel.NORMAL)
    @Description("BUG, confirmed live — POST /Dashboard/Admin/TasksOverdueReport with an empty body returns HTTP 200 but an envelope failure ('Error something went wrong', inner statusCode 500) — the non-export report has the same underlying issue as its Export sibling (see exportAdminTasksOverdueReportThrowsServerError).")
    public void getAdminTasksOverdueReportThrowsEnvelopeError() {
        Response res = claims().body(Map.of()).post("/Dashboard/Admin/TasksOverdueReport");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "claims", "dashboard", "bug"})
    @Severity(SeverityLevel.NORMAL)
    @Description("BUG, confirmed live — POST /Dashboard/TasksOverdueReport with an empty body returns HTTP 200 but the same envelope failure as the Admin variant.")
    public void getTasksOverdueReportThrowsEnvelopeError() {
        Response res = claims().body(Map.of()).post("/Dashboard/TasksOverdueReport");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "claims", "dashboard"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("POST /Dashboard/CriticalSlaBreaches with an empty body returns 200 — the exact call the live dashboard makes (see FRAMEWORK.md's captured network traffic).")
    public void getCriticalSlaBreachesReturns200() { assertReportOk(claims().body(Map.of()).post("/Dashboard/CriticalSlaBreaches")); }

    @Test(groups = {"api", "claims", "dashboard"})
    @Severity(SeverityLevel.MINOR)
    @Description("POST /Dashboard/CriticalSlaBreaches/All with an empty body returns 200 with the paged/full variant.")
    public void getAllCriticalSlaBreachesReturns200() { assertReportOk(claims().body(Map.of()).post("/Dashboard/CriticalSlaBreaches/All")); }

    @Test(groups = {"api", "claims", "dashboard"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Dashboard/TopClaims/All with an empty body returns 200 with the real Top Claims ranking.")
    public void getTopClaimsAllReturns200() { assertReportOk(claims().body(Map.of()).post("/Dashboard/TopClaims/All")); }

    @Test(groups = {"api", "claims", "dashboard"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Dashboard/TopCompanies/All with an empty body returns 200 with the real Top Companies ranking.")
    public void getTopCompaniesAllReturns200() { assertReportOk(claims().body(Map.of()).post("/Dashboard/TopCompanies/All")); }

    @Test(groups = {"api", "claims", "dashboard"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Dashboard/TopInsurersOutstanding with an empty body returns 200.")
    public void getTopInsurersOutstandingReturns200() { assertReportOk(claims().body(Map.of()).post("/Dashboard/TopInsurersOutstanding")); }

    @Test(groups = {"api", "claims", "dashboard"})
    @Severity(SeverityLevel.MINOR)
    @Description("POST /Dashboard/TopInsurersOutstanding/All with an empty body returns 200 with the paged/full variant.")
    public void getTopInsurersOutstandingAllReturns200() { assertReportOk(claims().body(Map.of()).post("/Dashboard/TopInsurersOutstanding/All")); }

    @Test(groups = {"api", "claims", "dashboard"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Dashboard/InsurerByOutstanding with an empty body returns 200 with real per-insurer outstanding-amount data.")
    public void getInsurerByOutstandingReturns200() { assertReportOk(claims().body(Map.of()).post("/Dashboard/InsurerByOutstanding")); }

    @Test(groups = {"api", "claims", "dashboard"})
    @Severity(SeverityLevel.MINOR)
    @Description("POST /Dashboard/NetPayableReceivable/All with an empty body returns 200 with the paged/full variant.")
    public void getNetPayableReceivableAllReturns200() { assertReportOk(claims().body(Map.of()).post("/Dashboard/NetPayableReceivable/All")); }

    @Test(groups = {"api", "claims", "dashboard"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Dashboard/ClaimAgeingDetailed with an empty body returns 200 with the detailed ageing breakdown behind Claim/ageing-Analysis's summary buckets.")
    public void getClaimAgeingDetailedReturns200() { assertReportOk(claims().body(Map.of()).post("/Dashboard/ClaimAgeingDetailed")); }

    @Test(groups = {"api", "claims", "dashboard"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Dashboard/ClaimAgeingAnalysis requires 'payable' (true=Payable/false=Recoverable) — confirmed live with it set returns 200 with real ageing buckets (totalValue, totalClaims, per-threshold counts).")
    public void getClaimAgeingAnalysisWithPayableFlagReturns200() {
        assertReportOk(claims().body(Map.of("payable", true)).post("/Dashboard/ClaimAgeingAnalysis"));
    }

    @Test(groups = {"api", "claims", "dashboard", "bug"})
    @Severity(SeverityLevel.MINOR)
    @Description("Message-quality bug, confirmed live — POST /Dashboard/ClaimAgeingAnalysis with an empty body fails with message 'Claim Type is required', but the request schema (ClaimAgeingAnalysisWidgetRequestDto) has no 'claim type' field at all — the actually-missing field is 'payable' (see getClaimAgeingAnalysisWithPayableFlagReturns200). A stale/misapplied validation message, not a real 'claim type' concept on this endpoint.")
    public void getClaimAgeingAnalysisWithEmptyBodyReturnsMisleadingMessage() {
        Response res = claims().body(Map.of()).post("/Dashboard/ClaimAgeingAnalysis");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
        assertThat(res.jsonPath().getString("message")).as("Expected message to contain 'Claim Type is required' but actual was '%s'. Response body: %s", res.jsonPath().getString("message"), res.asString()).containsIgnoringCase("Claim Type is required");
    }

    @Test(groups = {"api", "claims", "dashboard"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("POST /Dashboard/OverallSlaPerformance with an empty body returns 200 — the exact call the live dashboard makes (see FRAMEWORK.md's captured network traffic), despite being a POST.")
    public void getOverallSlaPerformanceReturns200() { assertReportOk(claims().body(Map.of()).post("/Dashboard/OverallSlaPerformance")); }

    @Test(groups = {"api", "claims", "dashboard"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Dashboard/ProcessingTimeMetrics with an empty body returns 200 with real claim-processing-time metrics.")
    public void getProcessingTimeMetricsReturns200() { assertReportOk(claims().body(Map.of()).post("/Dashboard/ProcessingTimeMetrics")); }

    @Test(groups = {"api", "claims", "dashboard"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Dashboard/SlaBreachSummary with an empty body returns 200 with a real SLA breach summary.")
    public void getSlaBreachSummaryReturns200() { assertReportOk(claims().body(Map.of()).post("/Dashboard/SlaBreachSummary")); }

    @Test(groups = {"api", "claims", "dashboard"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Dashboard/RegulatorRecoveryClaimCycle with an empty body returns 200 with real recovery-claim-cycle data for the Regulator view.")
    public void getRegulatorRecoveryClaimCycleReturns200() { assertReportOk(claims().body(Map.of()).post("/Dashboard/RegulatorRecoveryClaimCycle")); }

    /* ── Admin/Regulator financial reports needing real entity ids — validation-only ── */

    @Test(groups = {"api", "claims", "dashboard"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Dashboard/Admin/FinancialReport with an empty body fails model validation (HTTP 400): AtFaultIds and ClaimantId are required.")
    public void getAdminFinancialReportWithEmptyBodyReturnsValidationError() {
        Response res = claims().body(Map.of()).post("/Dashboard/Admin/FinancialReport");
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.jsonPath().getMap("errors")).containsKeys("AtFaultIds", "ClaimantId");
    }

    @Test(groups = {"api", "claims", "dashboard"})
    @Severity(SeverityLevel.MINOR)
    @Description("POST /Dashboard/Admin/FinancialReport/Details with an empty body fails the same model validation as the summary report.")
    public void getAdminFinancialReportDetailsWithEmptyBodyReturnsValidationError() {
        Response res = claims().body(Map.of()).post("/Dashboard/Admin/FinancialReport/Details");
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.jsonPath().getMap("errors")).containsKeys("AtFaultIds", "ClaimantId");
    }

    @Test(groups = {"api", "claims", "dashboard"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Dashboard/Admin/SpecialReport with an empty body fails model validation (HTTP 400): EntityIds is required.")
    public void getAdminSpecialReportWithEmptyBodyReturnsValidationError() {
        Response res = claims().body(Map.of()).post("/Dashboard/Admin/SpecialReport");
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.jsonPath().getMap("errors")).containsKey("EntityIds");
    }

    @Test(groups = {"api", "claims", "dashboard"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Dashboard/Admin/FinancialPositioningReport with an empty body fails the same model validation as SpecialReport: EntityIds is required.")
    public void getAdminFinancialPositioningReportWithEmptyBodyReturnsValidationError() {
        Response res = claims().body(Map.of()).post("/Dashboard/Admin/FinancialPositioningReport");
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.jsonPath().getMap("errors")).containsKey("EntityIds");
    }

    /* ── Exports: {"exportFormat":"Excel"} returns a real file ──────────── */

    @Test(groups = {"api", "claims", "dashboard", "export"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Dashboard/TopClaims/Export with exportFormat:Excel returns a real, non-empty file.")
    public void exportTopClaimsReturnsFile() { assertExportOk(claims().body(EXCEL).post("/Dashboard/TopClaims/Export")); }

    @Test(groups = {"api", "claims", "dashboard", "export"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Dashboard/TopCompanies/Export with exportFormat:Excel returns a real, non-empty file.")
    public void exportTopCompaniesReturnsFile() { assertExportOk(claims().body(EXCEL).post("/Dashboard/TopCompanies/Export")); }

    @Test(groups = {"api", "claims", "dashboard", "export"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Dashboard/TopInsurersOutstanding/Export with exportFormat:Excel returns a real, non-empty file.")
    public void exportTopInsurersOutstandingReturnsFile() { assertExportOk(claims().body(EXCEL).post("/Dashboard/TopInsurersOutstanding/Export")); }

    @Test(groups = {"api", "claims", "dashboard", "export"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Dashboard/NetPayableReceivable/Export with exportFormat:Excel returns a real, non-empty file.")
    public void exportNetPayableReceivableReturnsFile() { assertExportOk(claims().body(EXCEL).post("/Dashboard/NetPayableReceivable/Export")); }

    @Test(groups = {"api", "claims", "dashboard", "export"})
    @Severity(SeverityLevel.MINOR)
    @Description("POST /Dashboard/CompanyStatistics/Export with exportFormat:Excel returns a real, non-empty file.")
    public void exportCompanyStatisticsReturnsFile() { assertExportOk(claims().body(EXCEL).post("/Dashboard/CompanyStatistics/Export")); }

    @Test(groups = {"api", "claims", "dashboard", "export"})
    @Severity(SeverityLevel.MINOR)
    @Description("POST /Dashboard/CompanyStatistics/Details/Export with exportFormat:Excel returns a real, non-empty file.")
    public void exportCompanyStatisticsDetailsReturnsFile() { assertExportOk(claims().body(EXCEL).post("/Dashboard/CompanyStatistics/Details/Export")); }

    @Test(groups = {"api", "claims", "dashboard", "export"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Dashboard/TotalRecoveredPaidAmount/Export with exportFormat:Excel returns a real, non-empty file.")
    public void exportTotalRecoveredPaidAmountReturnsFile() { assertExportOk(claims().body(EXCEL).post("/Dashboard/TotalRecoveredPaidAmount/Export")); }

    @Test(groups = {"api", "claims", "dashboard", "export"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Dashboard/TotalRecoverablePayableValue/Export with exportFormat:Excel returns a real, non-empty file.")
    public void exportTotalRecoverablePayableValueReturnsFile() { assertExportOk(claims().body(EXCEL).post("/Dashboard/TotalRecoverablePayableValue/Export")); }

    @Test(groups = {"api", "claims", "dashboard", "export"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Dashboard/SLAPerformanceDashboard/Export with exportFormat:Excel returns a real, non-empty file.")
    public void exportSlaPerformanceDashboardReturnsFile() { assertExportOk(claims().body(EXCEL).post("/Dashboard/SLAPerformanceDashboard/Export")); }

    @Test(groups = {"api", "claims", "dashboard", "export"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Dashboard/RecoveryClaimCycle/Export with exportFormat:Excel returns a real, non-empty file.")
    public void exportRecoveryClaimCycleReturnsFile() { assertExportOk(claims().body(EXCEL).post("/Dashboard/RecoveryClaimCycle/Export")); }

    @Test(groups = {"api", "claims", "dashboard", "export"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Dashboard/ClaimAgeingAnalysis/Export with exportFormat:Excel returns a real, non-empty file.")
    public void exportClaimAgeingAnalysisReturnsFile() { assertExportOk(claims().body(EXCEL).post("/Dashboard/ClaimAgeingAnalysis/Export")); }

    @Test(groups = {"api", "claims", "dashboard", "export"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Dashboard/CriticalSLABreaches/Export with exportFormat:Excel returns a real, non-empty file.")
    public void exportCriticalSlaBreachesReturnsFile() { assertExportOk(claims().body(EXCEL).post("/Dashboard/CriticalSLABreaches/Export")); }

    @Test(groups = {"api", "claims", "dashboard", "export"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Dashboard/ProcessingTimeMetrics/Export with exportFormat:Excel returns a real, non-empty file.")
    public void exportProcessingTimeMetricsReturnsFile() { assertExportOk(claims().body(EXCEL).post("/Dashboard/ProcessingTimeMetrics/Export")); }

    @Test(groups = {"api", "claims", "dashboard", "export"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Dashboard/FullReport/Export with exportFormat:Excel returns a real, non-empty file.")
    public void exportFullReportReturnsFile() { assertExportOk(claims().body(EXCEL).post("/Dashboard/FullReport/Export")); }

    @Test(groups = {"api", "claims", "dashboard", "export"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Dashboard/ActionRequired/Export with exportFormat:Excel returns a real, non-empty file.")
    public void exportActionRequiredReturnsFile() { assertExportOk(claims().body(EXCEL).post("/Dashboard/ActionRequired/Export")); }

    @Test(groups = {"api", "claims", "dashboard", "export"})
    @Severity(SeverityLevel.MINOR)
    @Description("POST /Dashboard/ActionRerquiredCombind/Export with exportFormat:Excel returns a real, non-empty file — note the real path is misspelled ('Rerquired'/'Combind'), confirmed against the live Swagger spec; ActionRequiredCombined/Export below is the correctly-spelled sibling.")
    public void exportActionRerquiredCombindMisspelledPathReturnsFile() { assertExportOk(claims().body(EXCEL).post("/Dashboard/ActionRerquiredCombind/Export")); }

    @Test(groups = {"api", "claims", "dashboard", "export"})
    @Severity(SeverityLevel.MINOR)
    @Description("POST /Dashboard/ActionRequiredCombined/Export (correctly spelled) with exportFormat:Excel returns a real, non-empty file.")
    public void exportActionRequiredCombinedReturnsFile() { assertExportOk(claims().body(EXCEL).post("/Dashboard/ActionRequiredCombined/Export")); }

    @Test(groups = {"api", "claims", "dashboard", "export"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Dashboard/ClaimRanking/Export with exportFormat:Excel returns a real, non-empty file.")
    public void exportClaimRankingReturnsFile() { assertExportOk(claims().body(EXCEL).post("/Dashboard/ClaimRanking/Export")); }

    @Test(groups = {"api", "claims", "dashboard", "export"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Dashboard/InProcessClaims/Export with exportFormat:Excel returns a real, non-empty file.")
    public void exportInProcessClaimsReturnsFile() { assertExportOk(claims().body(EXCEL).post("/Dashboard/InProcessClaims/Export")); }

    @Test(groups = {"api", "claims", "dashboard", "export"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Dashboard/PotentialClaims/Export with exportFormat:Excel returns a real, non-empty file.")
    public void exportPotentialClaimsDashboardReturnsFile() { assertExportOk(claims().body(EXCEL).post("/Dashboard/PotentialClaims/Export")); }

    @Test(groups = {"api", "claims", "dashboard", "export"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Dashboard/AverageProcessingTime/Export with exportFormat:Excel returns a real, non-empty file.")
    public void exportAverageProcessingTimeReturnsFile() { assertExportOk(claims().body(EXCEL).post("/Dashboard/AverageProcessingTime/Export")); }

    @Test(groups = {"api", "claims", "dashboard", "export"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Dashboard/Regulator/FullReport/Export with exportFormat:Excel returns a real, non-empty file.")
    public void exportRegulatorFullReportReturnsFile() { assertExportOk(claims().body(EXCEL).post("/Dashboard/Regulator/FullReport/Export")); }

    @Test(groups = {"api", "claims", "dashboard", "export"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Dashboard/Regulator/InProcessClaims/Export with exportFormat:Excel returns a real, non-empty file.")
    public void exportRegulatorInProcessClaimsReturnsFile() { assertExportOk(claims().body(EXCEL).post("/Dashboard/Regulator/InProcessClaims/Export")); }

    @Test(groups = {"api", "claims", "dashboard", "export"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Dashboard/Regulator/AverageProcessingTime/Export with exportFormat:Excel returns a real, non-empty file.")
    public void exportRegulatorAverageProcessingTimeReturnsFile() { assertExportOk(claims().body(EXCEL).post("/Dashboard/Regulator/AverageProcessingTime/Export")); }

    @Test(groups = {"api", "claims", "dashboard", "export"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Dashboard/MyClaims/Export with exportFormat:Excel returns a real, non-empty file.")
    public void exportMyClaimsReturnsFile() { assertExportOk(claims().body(EXCEL).post("/Dashboard/MyClaims/Export")); }

    /* ── Export endpoints needing more than exportFormat — validation-only ── */

    @Test(groups = {"api", "claims", "dashboard", "export"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Dashboard/Admin/FinancialReport/Export with only exportFormat still fails model validation (HTTP 400): AtFaultIds and ClaimantId are also required.")
    public void exportAdminFinancialReportWithoutIdsReturnsValidationError() {
        Response res = claims().body(EXCEL).post("/Dashboard/Admin/FinancialReport/Export");
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.jsonPath().getMap("errors")).containsKeys("AtFaultIds", "ClaimantId");
    }

    @Test(groups = {"api", "claims", "dashboard", "export"})
    @Severity(SeverityLevel.MINOR)
    @Description("POST /Dashboard/Admin/FinancialReport/Details/Export with only exportFormat fails the same model validation.")
    public void exportAdminFinancialReportDetailsWithoutIdsReturnsValidationError() {
        Response res = claims().body(EXCEL).post("/Dashboard/Admin/FinancialReport/Details/Export");
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.jsonPath().getMap("errors")).containsKeys("AtFaultIds", "ClaimantId");
    }

    @Test(groups = {"api", "claims", "dashboard", "export"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Dashboard/Admin/SpecialReport/Export with only exportFormat still fails model validation (HTTP 400): EntityIds is also required.")
    public void exportAdminSpecialReportWithoutIdsReturnsValidationError() {
        Response res = claims().body(EXCEL).post("/Dashboard/Admin/SpecialReport/Export");
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.jsonPath().getMap("errors")).containsKey("EntityIds");
    }

    @Test(groups = {"api", "claims", "dashboard", "export"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Dashboard/Admin/FinancialPositioningReport/Export with only exportFormat fails the same model validation as SpecialReport/Export.")
    public void exportAdminFinancialPositioningReportWithoutIdsReturnsValidationError() {
        Response res = claims().body(EXCEL).post("/Dashboard/Admin/FinancialPositioningReport/Export");
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.jsonPath().getMap("errors")).containsKey("EntityIds");
    }

    @Test(groups = {"api", "claims", "dashboard", "export"})
    @Severity(SeverityLevel.MINOR)
    @Description("POST /Dashboard/Regulator/ClaimAgeingAnalysis/Export with only exportFormat returns a real HTTP 400 plain-string error 'Payable is required (false = Recoverable, true = Payable)' — a business rule, not the usual ProblemDetails shape.")
    public void exportRegulatorClaimAgeingAnalysisWithoutPayableFlagReturnsError() {
        Response res = claims().body(EXCEL).post("/Dashboard/Regulator/ClaimAgeingAnalysis/Export");
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.asString()).containsIgnoringCase("Payable is required");
    }

    /* ── Confirmed backend bugs: real HTTP 500 with a raw string error body ── */

    @Test(groups = {"api", "claims", "dashboard", "export", "bug"})
    @Severity(SeverityLevel.NORMAL)
    @Description("BUG, confirmed live — POST /Dashboard/Admin/TasksOverdueReport/Export with exportFormat:Excel throws a real HTTP 500: 'Cannot deserialize a DateTime from BsonType Null' while mapping AccidentReportDate — a data-quality bug (some record has a null accident report date the export DTO doesn't tolerate), not something this test's input caused.")
    public void exportAdminTasksOverdueReportThrowsServerError() {
        Response res = claims().body(EXCEL).post("/Dashboard/Admin/TasksOverdueReport/Export");
        ApiAssertions.assertStatusCode(res, 500, "HTTP status");
        assertThat(res.asString()).containsIgnoringCase("BsonType");
    }

    @Test(groups = {"api", "claims", "dashboard", "export", "bug"})
    @Severity(SeverityLevel.NORMAL)
    @Description("BUG, confirmed live — POST /Dashboard/TasksOverdueReport/Export throws the same real HTTP 500 BsonType deserialization error as the Admin variant.")
    public void exportTasksOverdueReportThrowsServerError() {
        Response res = claims().body(EXCEL).post("/Dashboard/TasksOverdueReport/Export");
        ApiAssertions.assertStatusCode(res, 500, "HTTP status");
        assertThat(res.asString()).containsIgnoringCase("BsonType");
    }

    @Test(groups = {"api", "claims", "dashboard", "export", "bug"})
    @Severity(SeverityLevel.NORMAL)
    @Description("BUG, confirmed live — POST /Dashboard/Regulator/PotentialClaims/Export with exportFormat:Excel throws a real HTTP 500: 'Command aggregate failed: the limit must be positive' — a Mongo aggregation pipeline bug (missing/zero page-size default), same class of issue as settlement CreditNoteApiTest's Bulk/History/{transactionId} finding.")
    public void exportRegulatorPotentialClaimsThrowsServerError() {
        Response res = claims().body(EXCEL).post("/Dashboard/Regulator/PotentialClaims/Export");
        ApiAssertions.assertStatusCode(res, 500, "HTTP status");
        assertThat(res.asString()).containsIgnoringCase("aggregate failed");
    }
}
