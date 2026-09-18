package com.mosadad.testing.tests.api.tenant;

import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * tenant service — SystemCodes tag. A generic lookup-list endpoint keyed by
 * a "master code" (e.g. EMIRATE, COUNTRY, GENDER). Confirmed live
 * 2026-09-17: every master code tried on this QA tenant returns HTTP 200
 * with an empty list rather than 404/validation error — the endpoint
 * doesn't reject unknown codes, it just returns nothing for them, so the
 * assertion here is intentionally structural (a list, not its contents).
 */
@Epic("Mosadad Recovery Claim")
@Feature("Tenant API — SystemCodes")
public class SystemCodesApiTest extends BaseApiTest {

    @Test(groups = {"api", "tenant", "system-codes"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /SystemCodes/GetByMasterCode/{masterCode} for a plausible code (EMIRATE) returns 200 with a list response (empty on this QA tenant — no system codes are seeded).")
    public void getSystemCodesByMasterCodeReturns200() {
        Response res = tenant().get("/SystemCodes/GetByMasterCode/EMIRATE");
        ApiAssertions.assertEnvelopeSuccess(res);
        assertThat(res.jsonPath().getList("response")).isNotNull();
    }

    @Test(groups = {"api", "tenant", "system-codes"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /SystemCodes/GetByMasterCode/{masterCode} for a nonsense master code still returns 200 with an empty list — confirmed live, the endpoint does not validate the code against a known set.")
    public void getSystemCodesByUnknownMasterCodeReturns200WithEmptyList() {
        Response res = tenant().get("/SystemCodes/GetByMasterCode/NOT_A_REAL_MASTER_CODE_XYZ");
        ApiAssertions.assertEnvelopeSuccess(res);
        assertThat(res.jsonPath().getList("response")).isEmpty();
    }

    @Test(groups = {"api", "tenant", "system-codes"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /SystemCodes/GetByMasterCode/COUNTRY returns 200 and a list field, exercising another valid master-code lookup without mutating any tenant data.")
    public void getSystemCodesByCountryMasterCodeReturns200List() {
        Response res = tenant().get("/SystemCodes/GetByMasterCode/COUNTRY");
        ApiAssertions.assertEnvelopeSuccess(res);
        assertThat(res.jsonPath().getList("response")).isNotNull();
    }
}
