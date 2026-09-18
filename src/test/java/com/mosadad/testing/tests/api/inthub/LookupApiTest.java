package com.mosadad.testing.tests.api.inthub;

import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * inthub service — Lookup tag. Generic reference-data endpoint. Note there
 * are two distinct lookup surfaces confirmed live: {@code Lookup/Resource}
 * (used by the real Angular dashboard to resolve entity names — see
 * FRAMEWORK.md's captured network traffic) works for {@code name=Entity};
 * the plain {@code Lookup}/{@code Lookup/Paged}/{@code Lookup/{id}/Children}
 * family returns "No lookups found" for every category tried on this QA
 * tenant (Entity, Emirate, AccidentType, EmirateCode, VehicleColor,
 * PlateSource/Color, Nationality, Country) — this QA environment's generic
 * Lookup collection appears to be unseeded, so these assert the graceful
 * envelope failure shape rather than real data.
 */
@Epic("Mosadad Recovery Claim")
@Feature("IntHub API — Lookup")
public class LookupApiTest extends BaseApiTest {

    @Test(groups = {"api", "inthub", "lookup"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /Lookup/Resource?name=Entity&type=Insurance Company returns 200 with the real list of UAE insurer entities — this is the exact call the live Angular dashboard makes to resolve entity names (confirmed via captured network traffic).")
    public void getResourceLookupForEntityInsuranceCompanyReturns200() {
        Response res = inthub().queryParam("name", "Entity").queryParam("type", "Insurance Company").get("/Lookup/Resource");
        ApiAssertions.assertEnvelopeSuccess(res);
        List<Map<String, Object>> items = res.jsonPath().getList("response");
        assertThat(items).isNotEmpty();
        assertThat(items.get(0)).containsKeys("id", "name");
    }

    @Test(groups = {"api", "inthub", "lookup"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /Lookup?name={category} for a plausible category (Emirate) returns a graceful 'No lookups found' envelope failure on this QA tenant rather than a 500 — confirmed live for 7 different category guesses.")
    public void getLookupByNameReturnsGracefulEnvelopeFailure() {
        Response res = inthub().queryParam("name", "Emirate").get("/Lookup");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
        assertThat(res.jsonPath().getString("message")).as("Expected message to contain 'No lookups found' but actual was '%s'. Response body: %s", res.jsonPath().getString("message"), res.asString()).containsIgnoringCase("No lookups found");
    }

    @Test(groups = {"api", "inthub", "lookup"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /Lookup/{id}/Children for a real entity id (from Lookup/Resource) returns a graceful 'No lookups found' envelope failure rather than a 500 — this QA tenant's generic Lookup collection has no child records for it.")
    public void getLookupChildrenReturnsGracefulEnvelopeFailure() {
        Response res = inthub().get("/Lookup/" + entityId() + "/Children");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "inthub", "lookup"})
    @Severity(SeverityLevel.MINOR)
    @Description("POST /Lookup/Paged with an empty body returns a graceful 'No lookups found' envelope failure rather than a 500.")
    public void pagedLookupWithEmptyBodyReturnsGracefulEnvelopeFailure() {
        Response res = inthub().body(Map.of()).post("/Lookup/Paged");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }
}
