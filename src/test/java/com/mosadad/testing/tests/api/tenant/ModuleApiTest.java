package com.mosadad.testing.tests.api.tenant;

import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** tenant service — Module tag. The platform's top-level modules (Recovery Claims, Company Details, FNOL, ...) that drive the entity portal's sidebar. */
@Epic("Mosadad Recovery Claim")
@Feature("Tenant API — Module")
public class ModuleApiTest extends BaseApiTest {

    @Test(groups = {"api", "tenant", "module"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /Module/ListAll returns 200 with a non-empty list of modules, each carrying a route, moduleName and isActive flag.")
    public void listAllModulesReturns200() {
        Response res = tenant().get("/Module/ListAll");
        ApiAssertions.assertEnvelopeSuccess(res);
        assertThat(res.jsonPath().getList("response")).isNotEmpty();
        assertThat(res.jsonPath().getString("response[0].moduleName")).isNotBlank();
        assertThat(res.jsonPath().getString("response[0].route")).isNotBlank();
    }

    @Test(groups = {"api", "tenant", "module"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /Module/ListAll returns consistently structured module metadata for every item in the list — read-only coverage for the tenant menu model.")
    public void listAllModulesContainsStructuredMetadata() {
        Response res = tenant().get("/Module/ListAll");
        ApiAssertions.assertEnvelopeSuccess(res);
        java.util.List<java.util.Map<String, Object>> modules = res.jsonPath().getList("response");
        assertThat(modules).isNotEmpty();
        assertThat(modules).allSatisfy(module ->
                assertThat(module).containsKeys("id", "moduleName", "route", "isActive")
        );
    }
}
