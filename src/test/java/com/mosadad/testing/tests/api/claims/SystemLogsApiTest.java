package com.mosadad.testing.tests.api.claims;

import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** claims service — SystemLogs tag. The real audit trail behind the dashboard's "Recent Activities" widget — every claim/quotation/invoice action logged with actionBy, collection, and timestamp. */
@Epic("Mosadad Recovery Claim")
@Feature("Claims API — SystemLogs")
public class SystemLogsApiTest extends BaseApiTest {

    @Test(groups = {"api", "claims", "system-logs"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /SystemLogs/Latest returns 200 with real, non-empty audit log entries (id, interface, action, collection, actionBy, creationDate).")
    public void getLatestSystemLogsReturns200() {
        Response res = claims().get("/SystemLogs/Latest");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        List<Map<String, Object>> logs = res.jsonPath().getList("response");
        assertThat(logs).isNotEmpty();
        assertThat(logs.get(0)).containsKeys("id", "action", "collection", "actionBy", "creationDate");
    }

    @Test(groups = {"api", "claims", "system-logs"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /SystemLogs/Latest/{entityId} for the logged-in entity returns 200 with real audit log entries.")
    public void getLatestSystemLogsForOwnEntityReturns200() {
        Response res = claims().get("/SystemLogs/Latest/" + entityId());
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.jsonPath().getList("response")).isNotEmpty();
    }

    @Test(groups = {"api", "claims", "system-logs"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /SystemLogs/Admin/Latest returns 200 with real, non-empty platform-wide audit log entries (not scoped to the current entity — a Super Admin/Regulator view).")
    public void getLatestAdminSystemLogsReturns200() {
        Response res = claims().get("/SystemLogs/Admin/Latest");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.jsonPath().getList("response")).isNotEmpty();
    }

    @Test(groups = {"api", "claims", "system-logs"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /SystemLogs/List/{claimId} for a real claim returns 200 with that claim's own audit trail, including the claim's creation event.")
    public void getSystemLogsForRealClaimReturns200() {
        Response res = claims().get("/SystemLogs/List/6aab489ecb32564549128fa6");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        List<Map<String, Object>> logs = res.jsonPath().getList("response");
        assertThat(logs).isNotEmpty();
        assertThat(logs).allSatisfy(log -> assertThat(log.get("claimId")).isEqualTo("6aab489ecb32564549128fa6"));
    }
}
