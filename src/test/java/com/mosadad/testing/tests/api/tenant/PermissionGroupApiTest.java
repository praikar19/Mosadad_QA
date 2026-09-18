package com.mosadad.testing.tests.api.tenant;

import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** tenant service — PermissionGroup tag. Groups individual Permissions (e.g. "Entities": create/read/delete:entities) — the middle tier of the RBAC hierarchy under PermissionCategory. */
@Epic("Mosadad Recovery Claim")
@Feature("Tenant API — PermissionGroup")
public class PermissionGroupApiTest extends BaseApiTest {

    @Test(groups = {"api", "tenant", "permission-group"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /PermissionGroup returns 200 with a (possibly empty) list — the real groups are normally browsed nested under PermissionCategory/List instead.")
    public void listPermissionGroupsReturns200() {
        Response res = tenant().get("/PermissionGroup");
        ApiAssertions.assertEnvelopeSuccess(res);
        assertThat(res.jsonPath().getList("response")).isNotNull();
    }

    @Test(groups = {"api", "tenant", "permission-group"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /PermissionGroup with an empty body fails model validation (HTTP 400): Name is required.")
    public void createPermissionGroupWithEmptyBodyReturnsValidationError() {
        Response res = tenant().body(Map.of()).post("/PermissionGroup");
        ApiAssertions.assertValidationProblem(res, "Name");
    }

    @Test(enabled = false, groups = {"api", "tenant", "permission-group", "mutating"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — creates a real permission group in the shared RBAC tree. Enable with explicit sign-off on the group name to seed.")
    public void createPermissionGroupWithValidPayloadReturns200() {
        Response res = tenant().body(Map.of("name", "QA Automation Group")).post("/PermissionGroup");
        ApiAssertions.assertEnvelopeSuccess(res);
    }

    @Test(enabled = false, groups = {"api", "tenant", "permission-group", "mutating"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — attaches Permissions to a group, changing what every role referencing it can do. Enable only against a disposable group.")
    public void addPermissionsToGroupReturns200() {
        Response res = tenant().body(Map.of("permissions", List.of("placeholder-permission-id")))
                .put("/PermissionGroup/placeholder-group-id/Permissions");
        ApiAssertions.assertEnvelopeSuccess(res);
    }
}
