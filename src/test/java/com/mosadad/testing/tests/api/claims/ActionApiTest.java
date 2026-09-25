package com.mosadad.testing.tests.api.claims;

import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

/**
 * claims service — Action. The whole controller returns a gateway 404 on this
 * environment; the other operations are kept as disabled stubs.
 */
@Epic("Mosadad Recovery Claim")
@Feature("Claims API — Action")
public class ActionApiTest extends BaseApiTest {

    @Test(groups = {"api", "claims", "action"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /Action/GetAll — confirmed live unreachable through the gateway on this QA environment (bare HTTP 404), despite being documented in Swagger and requiring no path params.")
    public void getAllActionsIsUnreachableOnThisGateway() {
        Response res = claims().get("/Action/GetAll");
        ApiAssertions.assertStatusCode(res, 404, "gateway route for /Action/GetAll");
    }

    @Test(groups = {"api", "claims", "action"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /Action/GetById/{id} for a fake id returns the same gateway-level 404 seen for the rest of this controller — read-only and non-mutating.")
    public void getActionByIdForMissingIdIs404() {
        Response res = claims().get("/Action/GetById/000000000000000000000000");
        ApiAssertions.assertStatusCode(res, 404, "gateway route for /Action/GetById/{id}");
    }

    @Test(enabled = false, groups = {"api", "claims", "action"})
    @Severity(SeverityLevel.MINOR)
    @Description("STUB — pending Action being wired up on the gateway. GET /Action/GetById/{id}.")
    public void getActionByIdReturns200() { }

    @Test(enabled = false, groups = {"api", "claims", "action", "mutating"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — pending Action being wired up on the gateway. POST /Action/Create.")
    public void createActionReturns200() { }

    @Test(enabled = false, groups = {"api", "claims", "action", "mutating"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — pending Action being wired up on the gateway. POST /Action/Fire — fires a workflow action against real claim data.")
    public void fireActionReturns200() { }

    @Test(enabled = false, groups = {"api", "claims", "action", "mutating"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — pending Action being wired up on the gateway. PUT /Action/Update/{id}.")
    public void updateActionReturns200() { }

    @Test(enabled = false, groups = {"api", "claims", "action", "mutating", "destructive"})
    @Severity(SeverityLevel.NORMAL)
    @Description("STUB — pending Action being wired up on the gateway. DELETE /Action/Delete/{id}.")
    public void deleteActionReturns200() { }
}
