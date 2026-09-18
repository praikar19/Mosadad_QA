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

/**
 * tenant service — User tag. Login and RefreshToken (also under this tag in
 * Swagger) are covered in {@code AuthApiTest} instead, since they're
 * cross-cutting auth concerns exercised by every other test class.
 */
@Epic("Mosadad Recovery Claim")
@Feature("Tenant API — User")
public class UserApiTest extends BaseApiTest {

    @Test(groups = {"api", "tenant", "user"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /User/LoadUser/{userID} for the logged-in user returns 200 with the real user record (email matches the QA login).")
    public void loadUserForOwnUserIdReturns200() {
        Response res = tenant().get("/User/LoadUser/" + currentUserId());
        ApiAssertions.assertEnvelopeSuccess(res);
        assertThat(res.jsonPath().getString("response.id"))
            .as("Expected response.id='%s' but actual was '%s'. Response body: %s", currentUserId(), res.jsonPath().getString("response.id"), res.asString())
            .isEqualTo(currentUserId());
        assertThat(res.jsonPath().getString("response.email"))
            .as("Expected email 'Dubaiqa@gmail.com' but actual was '%s'. Response body: %s", res.jsonPath().getString("response.email"), res.asString())
            .isEqualToIgnoringCase("Dubaiqa@gmail.com");
        assertThat(res.jsonPath().getString("response.entityId"))
            .as("Expected entityId='%s' but actual was '%s'. Response body: %s", entityId(), res.jsonPath().getString("response.entityId"), res.asString())
            .isEqualTo(entityId());
    }

    @Test(groups = {"api", "tenant", "user"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /User/Profile/{userID} for the logged-in user returns 200 with the same profile shape as LoadUser.")
    public void getUserProfileForOwnUserIdReturns200() {
        Response res = tenant().get("/User/Profile/" + currentUserId());
        ApiAssertions.assertEnvelopeSuccess(res);
        assertThat(res.jsonPath().getString("response.id")).isEqualTo(currentUserId());
    }

    @Test(groups = {"api", "tenant", "user"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /User/Profile/{userID} for a non-existent user id returns a safe envelope failure instead of a server crash.")
    public void getUserProfileForNonExistentUserIdReturnsEnvelopeFailure() {
        Response res = tenant().get("/User/Profile/000000000000000000000000");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "tenant", "user"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /User/LoadUserComplete/{userID} for the logged-in user returns 200 with the extended profile (roles included).")
    public void loadUserCompleteForOwnUserIdReturns200() {
        Response res = tenant().get("/User/LoadUserComplete/" + currentUserId());
        ApiAssertions.assertEnvelopeSuccess(res);
        assertThat(res.jsonPath().getString("response.userId")).isEqualTo(currentUserId());
        assertThat(res.jsonPath().getList("response.roles")).isNotNull();
    }

    @Test(groups = {"api", "tenant", "user"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /User/Auth0 for the logged-in user returns 200 with the linked Auth0/B2C identity for the current session's JWT.")
    public void getAuth0UserForLoggedInSessionReturns200() {
        Response res = tenant().get("/User/Auth0");
        ApiAssertions.assertEnvelopeSuccess(res);
        assertThat(res.jsonPath().getString("response.id")).isEqualTo(currentUserId());
        assertThat(res.jsonPath().getString("response.auth0Id")).isNotBlank();
    }

    @Test(groups = {"api", "tenant", "user"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /User/LoadUserComplete/{userID} for a non-existent id returns a safe envelope failure rather than an internal error.")
    public void loadUserCompleteForNonExistentUserIdReturnsEnvelopeFailure() {
        Response res = tenant().get("/User/LoadUserComplete/000000000000000000000000");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "tenant", "user"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /User/List with an empty filter returns 200 with a paged, non-empty list of real tenant users.")
    public void listUsersWithEmptyFilterReturns200() {
        Response res = tenant().body(Map.of()).post("/User/List");
        ApiAssertions.assertEnvelopeSuccess(res);
        assertThat(res.jsonPath().getList("response.items")).isNotEmpty();
    }

    @Test(groups = {"api", "tenant", "user"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /User/dropdown with no filters returns 200 with a paged user list suitable for a picker UI.")
    public void getUserDropdownWithNoFiltersReturns200() {
        Response res = tenant().get("/User/dropdown");
        ApiAssertions.assertEnvelopeSuccess(res);
        assertThat(res.jsonPath().getList("response.items")).isNotNull();
    }

    @Test(groups = {"api", "tenant", "user"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /User/Roles with an empty filter returns 200 with the tenant's role list (same underlying data as Role/ListAll, exposed again under User for the user-role-assignment UI).")
    public void listRolesForUserAssignmentReturns200() {
        Response res = tenant().body(Map.of()).post("/User/Roles");
        ApiAssertions.assertEnvelopeSuccess(res);
        assertThat(res.jsonPath().getList("response.items")).isNotEmpty();
    }

    @Test(groups = {"api", "tenant", "user"})
    @Severity(SeverityLevel.MINOR)
    @Description("POST /User/Create with an empty array is a real, safe no-op — confirmed live it returns 200 with an empty result and creates nothing, unlike a single malformed item (see next test).")
    public void createUsersWithEmptyArrayIsSafeNoOp() {
        Response res = tenant().body(Collections.emptyList()).post("/User/Create");
        ApiAssertions.assertEnvelopeSuccess(res);
        assertThat(res.jsonPath().getList("response")).isEmpty();
    }

    @Test(groups = {"api", "tenant", "user"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /User/Create with one empty object in the array fails server-side (envelope statusCode 500) rather than silently creating a broken user — confirmed live.")
    public void createUsersWithMalformedItemReturnsEnvelopeFailure() {
        Response res = tenant().body(List.of(Map.of())).post("/User/Create");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "tenant", "user"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /User/SendForgetPasswordEmail/{type} for an email with no matching account returns an envelope 'No User Found' failure rather than a 500, and (confirmed live) sends no real email.")
    public void sendForgetPasswordEmailForUnknownEmailReturnsEnvelopeFailure() {
        Response res = tenant().body(Map.of("emailAddress", "no-such-qa-user-" + System.currentTimeMillis() + "@example.invalid"))
                .post("/User/SendForgetPasswordEmail/0");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
        assertThat(res.jsonPath().getString("message")).as("Expected message to contain 'No User Found' but actual was '%s'. Response body: %s", res.jsonPath().getString("message"), res.asString()).containsIgnoringCase("No User Found");
    }

    @Test(groups = {"api", "tenant", "user"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /User/ResetPassword with an empty body fails model validation (HTTP 400): Token and NewPassword are required.")
    public void resetPasswordWithEmptyBodyReturnsValidationError() {
        Response res = tenant().body(Map.of()).post("/User/ResetPassword");
        ApiAssertions.assertValidationProblem(res, "Token", "NewPassword");
    }

    @Test(groups = {"api", "tenant", "user"})
    @Severity(SeverityLevel.NORMAL)
    @Description("PUT /User/UpdateUser/{userID} with an empty body fails model validation (HTTP 400): Email, UserName, CountryId, UserTypeId and MobileNumber are required — no real user is touched.")
    public void updateUserWithEmptyBodyReturnsValidationError() {
        Response res = tenant().body(Map.of()).put("/User/UpdateUser/000000000000000000000000");
        ApiAssertions.assertValidationProblem(res, "Email", "UserName", "CountryId", "UserTypeId", "MobileNumber");
    }

    @Test(groups = {"api", "tenant", "user"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /User/{id}/Role with an empty body fails model validation (HTTP 400): Roles is required.")
    public void assignRoleWithEmptyBodyReturnsValidationError() {
        Response res = tenant().body(Map.of()).post("/User/000000000000000000000000/Role");
        ApiAssertions.assertValidationProblem(res, "Roles");
    }

    @Test(groups = {"api", "tenant", "user"})
    @Severity(SeverityLevel.MINOR)
    @Description("POST /User/ExportToExcel with an empty filter returns a real, non-empty .xlsx file — a genuinely read-only export, safe to run live.")
    public void exportUsersToExcelReturnsXlsxFile() {
        Response res = tenant().body(Map.of()).post("/User/ExportToExcel");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.header("Content-Type")).contains("spreadsheetml");
        assertThat(res.asByteArray().length).isGreaterThan(0);
    }

    @Test(groups = {"api", "tenant", "user"})
    @Severity(SeverityLevel.MINOR)
    @Description("POST /User/ExportToCsv with an empty filter returns a non-empty file, same read-only export contract as ExportToExcel.")
    public void exportUsersToCsvReturnsFile() {
        Response res = tenant().body(Map.of()).post("/User/ExportToCsv");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.asByteArray().length).isGreaterThan(0);
    }

    @Test(groups = {"api", "tenant", "user"})
    @Severity(SeverityLevel.MINOR)
    @Description("POST /User/ExportToPdf with an empty filter returns a non-empty file, same read-only export contract as ExportToExcel.")
    public void exportUsersToPdfReturnsFile() {
        Response res = tenant().body(Map.of()).post("/User/ExportToPdf");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.asByteArray().length).isGreaterThan(0);
    }

    @Test(enabled = false, groups = {"api", "tenant", "user", "mutating"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("STUB — creates a real user account in the shared QA tenant. Enable only with a disposable email/role fixture agreed with QA.")
    public void createUserWithValidPayloadReturns200() {
        Response res = tenant().body(List.of(Map.of(
                "userName", "qa-automation-user",
                "email", "qa-automation-user@example.invalid",
                "mobileNumber", "0500000000",
                "entityId", entityId()
        ))).post("/User/Create");
        ApiAssertions.assertEnvelopeSuccess(res);
    }

    @Test(enabled = false, groups = {"api", "tenant", "user", "mutating"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("STUB — overwrites a real user's profile fields. Enable only against a disposable user, never the primary Dubai QA login (would lock the whole suite out).")
    public void updateUserWithValidPayloadReturns200() {
        Response res = tenant().body(Map.of("userName", "Should Not Actually Run")).put("/User/UpdateUser/placeholder-user-id");
        ApiAssertions.assertEnvelopeSuccess(res);
    }

    @Test(enabled = false, groups = {"api", "tenant", "user", "mutating", "destructive"})
    @Severity(SeverityLevel.BLOCKER)
    @Description("STUB — toggles a real user's Activation (enable/disable login). Never run against the primary Dubai QA login.")
    public void toggleUserActivationReturns200() {
        Response res = tenant().put("/User/Activation/placeholder-user-id/true");
        ApiAssertions.assertEnvelopeSuccess(res);
    }

    @Test(enabled = false, groups = {"api", "tenant", "user", "mutating"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — reassigns which roles a real user holds, changing their real permissions. Enable only against a disposable user.")
    public void assignRoleToUserWithValidPayloadReturns200() {
        Response res = tenant().body(Map.of("roles", List.of("placeholder-role-id"))).post("/User/placeholder-user-id/Role");
        ApiAssertions.assertEnvelopeSuccess(res);
    }

    @Test(enabled = false, groups = {"api", "tenant", "user", "mutating", "destructive"})
    @Severity(SeverityLevel.BLOCKER)
    @Description("STUB — permanently deletes a user from the shared QA tenant. Only ever run against a disposable user created by this suite.")
    public void deleteUserReturns200() {
        Response res = tenant().delete("/User/placeholder-user-id");
        ApiAssertions.assertEnvelopeSuccess(res);
    }

    @Test(enabled = false, groups = {"api", "tenant", "user", "mutating"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — POST /User/Logout invalidates the real, shared session token cached by ApiClient for the whole suite. Enabling this would log out every other API test class running afterward; run in isolation only.")
    public void logoutInvalidatesSession() {
        Response res = tenant().post("/User/Logout");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
    }
}
