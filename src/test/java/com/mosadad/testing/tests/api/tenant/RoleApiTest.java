package com.mosadad.testing.tests.api.tenant;

import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** tenant service — Role tag. Roles bundle permission categories/groups (see PermissionCategoryApiTest) and drive per-user access — see also user thresholds (recovery-amount approval limits). */
@Epic("Mosadad Recovery Claim")
@Feature("Tenant API — Role")
public class RoleApiTest extends BaseApiTest {

    @Test(groups = {"api", "tenant", "role"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /Role/IdNameList returns 200 with the tenant's real roles as lightweight {id, name} pairs (e.g. \"Head of Claims\", \"Claim Handler\").")
    public void getRoleIdNameListReturns200() {
        Response res = tenant().get("/Role/IdNameList");
        ApiAssertions.assertEnvelopeSuccess(res);
        List<Map<String, Object>> roles = res.jsonPath().getList("response");
        assertThat(roles).isNotEmpty();
        assertThat(roles.get(0)).containsKeys("id", "name");
    }

    @Test(groups = {"api", "tenant", "role"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /Role/GetRoleByID/{ID} for a real role id (from IdNameList) returns 200 with its full permission-category tree.")
    public void getRoleByIdForRealRoleReturns200() {
        String roleId = tenant().get("/Role/IdNameList").jsonPath().getString("response[0].id");
        Response res = tenant().get("/Role/GetRoleByID/" + roleId);
        ApiAssertions.assertEnvelopeSuccess(res);
        assertThat(res.jsonPath().getString("response.id")).isEqualTo(roleId);
        assertThat(res.jsonPath().getList("response.permissionCategories")).isNotNull();
    }

    @Test(groups = {"api", "tenant", "role"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /Role/GetRoleByID/{ID} for a well-formed but non-existent id returns a 'No Role Found' envelope failure, not a 500 — confirmed live even for the current user's own roleId (670cfe5a...), which is a virtual Super Admin role not stored as a document.")
    public void getRoleByIdWithNonExistentIdReturnsEnvelopeFailure() {
        Response res = tenant().get("/Role/GetRoleByID/000000000000000000000000");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
        assertThat(res.jsonPath().getString("message")).as("Expected message to contain 'No Role Found' but actual was '%s'. Response body: %s", res.jsonPath().getString("message"), res.asString()).containsIgnoringCase("No Role Found");
    }

    @Test(groups = {"api", "tenant", "role"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /Role/GetRoleByID/{ID} with a malformed ID string returns a safe envelope failure instead of a server error.")
    public void getRoleByIdWithMalformedIdReturnsEnvelopeFailure() {
        Response res = tenant().get("/Role/GetRoleByID/not-a-valid-role-id");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "tenant", "role"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Role/ListAll with an empty filter returns 200 with a paged, non-empty list of full role records.")
    public void listAllRolesReturns200() {
        Response res = tenant().body(Map.of()).post("/Role/ListAll");
        ApiAssertions.assertEnvelopeSuccess(res);
        assertThat(res.jsonPath().getList("response.items")).isNotEmpty();
    }

    @Test(groups = {"api", "tenant", "role"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /Role/GetUserThresholds for the logged-in Super Admin returns 200 with the caller's own roleId/roleName and (empty) thresholds — Super Admin bypasses recovery-amount approval limits.")
    public void getUserThresholdsForLoggedInUserReturns200() {
        Response res = tenant().get("/Role/GetUserThresholds");
        ApiAssertions.assertEnvelopeSuccess(res);
        assertThat(res.jsonPath().getString("response.roleId")).isNotBlank();
        assertThat(res.jsonPath().getString("response.roleName")).isNotBlank();
    }

    @Test(groups = {"api", "tenant", "role"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /Role/check-thresholds for the logged-in Super Admin always returns true (bypasses threshold checks), confirmed live.")
    public void checkThresholdsForSuperAdminBypassesCheck() {
        Response res = tenant().queryParam("permissionName", "test").queryParam("workshopType", 1).queryParam("value", 100)
                .get("/Role/check-thresholds");
        ApiAssertions.assertEnvelopeSuccess(res);
        ApiAssertions.assertJsonBoolean(res, "response", true, "envelope response");
    }

    @Test(groups = {"api", "tenant", "role"})
    @Severity(SeverityLevel.MINOR)
    @Description("POST /Role/check-multiple-thresholds with an empty array returns an envelope failure ('requests are required') rather than a 500.")
    public void checkMultipleThresholdsWithEmptyArrayReturnsEnvelopeFailure() {
        Response res = tenant().body(Collections.emptyList()).post("/Role/check-multiple-thresholds");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "tenant", "role"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Role with an empty body fails model validation (HTTP 400): Name and Description are required — confirmed live, no role is persisted.")
    public void createRoleWithEmptyBodyReturnsValidationError() {
        Response res = tenant().body(Map.of()).post("/Role");
        ApiAssertions.assertValidationProblem(res, "Name", "Description");
    }

    @Test(enabled = false, groups = {"api", "tenant", "role", "mutating"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — creates a real role visible to every user in the shared tenant. Enable with explicit sign-off on the role name to seed.")
    public void createRoleWithValidPayloadReturns200() {
        Response res = tenant().body(Map.of(
                "name", "QA Automation Role",
                "nameAr", "دور اختبار",
                "description", "Role created by API automation",
                "permissions", List.of()
        )).post("/Role");
        ApiAssertions.assertEnvelopeSuccess(res);
    }

    @Test(enabled = false, groups = {"api", "tenant", "role", "mutating"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("STUB — replaces a real role's permission set, changing access for every user assigned to it. Enable only against a disposable role.")
    public void updateRolePermissionsReturns200() {
        Response res = tenant().body(Map.of("name", "QA Automation Role", "permissions", List.of()))
                .put("/Role/placeholder-role-id/Permission");
        ApiAssertions.assertEnvelopeSuccess(res);
    }

    @Test(enabled = false, groups = {"api", "tenant", "role", "mutating", "destructive"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("STUB — permanently deletes a role from the shared tenant, breaking any user still assigned to it. Only ever run against a disposable role created by this suite.")
    public void deleteRoleReturns200() {
        Response res = tenant().delete("/Role/placeholder-role-id");
        ApiAssertions.assertEnvelopeSuccess(res);
    }
}
