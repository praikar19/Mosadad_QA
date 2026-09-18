package com.mosadad.testing.tests.api.tenant;

import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * tenant service — Permission tag. A single write-only endpoint (no GET/List
 * exists for bare Permission — browsing happens via
 * {@code PermissionCategory/List}, which nests permissions per group).
 */
@Epic("Mosadad Recovery Claim")
@Feature("Tenant API — Permission")
public class PermissionApiTest extends BaseApiTest {

    @Test(groups = {"api", "tenant", "permission"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Permission with an empty body fails model validation (HTTP 400), flagging PermissionName and Description as required — confirmed live, no permission is persisted.")
    public void createPermissionWithEmptyBodyReturnsValidationError() {
        Response res = tenant().body(Map.of()).post("/Permission");
        ApiAssertions.assertValidationProblem(res, "PermissionName", "Description");
    }

    @Test(enabled = false, groups = {"api", "tenant", "permission", "mutating"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — creating a real Permission adds a new row to the shared RBAC permission catalog used by every entity on the platform. Enable only with explicit sign-off on the permission name to seed.")
    public void createPermissionWithValidPayloadReturns200() {
        Response res = tenant().body(Map.of(
                "permissionName", "qa:automation-test-permission",
                "description", "QA automation smoke permission"
        )).post("/Permission");
        ApiAssertions.assertEnvelopeSuccess(res);
    }
}
