package com.mosadad.testing.tests.api.tenant;

import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** tenant service — PermissionCategory tag. Groups PermissionGroups (which in turn group Permissions) under a module, e.g. "Admin" under the Recovery Claims module. */
@Epic("Mosadad Recovery Claim")
@Feature("Tenant API — PermissionCategory")
public class PermissionCategoryApiTest extends BaseApiTest {

    @Test(groups = {"api", "tenant", "permission-category"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /PermissionCategory/List with an empty filter returns 200 with the platform's real RBAC tree: categories nesting permissionGroups nesting permissions.")
    public void listPermissionCategoriesReturns200() {
        Response res = tenant().body(Map.of()).post("/PermissionCategory/List");
        ApiAssertions.assertEnvelopeSuccess(res);
        List<Map<String, Object>> categories = res.jsonPath().getList("response");
        assertThat(categories).isNotEmpty();
        assertThat(categories.get(0)).containsKeys("id", "name", "moduleId", "permissionGroups");
    }

    @Test(groups = {"api", "tenant", "permission-category"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /PermissionCategory with an empty body fails model validation (HTTP 400): Name and ModuleId are required.")
    public void createPermissionCategoryWithEmptyBodyReturnsValidationError() {
        Response res = tenant().body(Map.of()).post("/PermissionCategory");
        ApiAssertions.assertValidationProblem(res, "Name", "ModuleId");
    }

    @Test(enabled = false, groups = {"api", "tenant", "permission-category", "mutating"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — creates a real category in the shared RBAC tree. Enable only with a disposable moduleId fixture agreed with QA.")
    public void createPermissionCategoryWithValidPayloadReturns200() {
        Response res = tenant().body(Map.of("name", "QA Automation Category", "moduleId", "placeholder", "interface", 0))
                .post("/PermissionCategory");
        ApiAssertions.assertEnvelopeSuccess(res);
    }

    @Test(enabled = false, groups = {"api", "tenant", "permission-category", "mutating"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — attaches PermissionGroups to a category, changing what every role scoped to it can assign. Enable only against a disposable category.")
    public void addPermissionGroupsToCategoryReturns200() {
        Response res = tenant().body(Map.of("permissionGroupIds", List.of("placeholder-group-id")))
                .put("/PermissionCategory/placeholder-category-id/PermissionGroups");
        ApiAssertions.assertEnvelopeSuccess(res);
    }
}
