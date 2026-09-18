package com.mosadad.testing.tests.api.tenant;

import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * tenant service — Entity tag. An "Entity" is one of the 38 insurers
 * registered on Mosadad (see MOSADAD_DOMAIN.md §Actors). GET/List reads
 * verified live 2026-09-17. Create/Update/Delete/Activation are real
 * mutations against the shared QA tenant list — the "happy path" for those
 * stays a disabled stub (enabled=false) pending explicit sign-off to
 * actually create/disable/delete entities in the shared environment;
 * their required-field validation IS exercised live below, since sending
 * a deliberately empty body never persists anything.
 */
@Epic("Mosadad Recovery Claim")
@Feature("Tenant API — Entity")
public class EntityApiTest extends BaseApiTest {

    @Test(groups = {"api", "tenant", "entity"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /Entity/GetById/{EntityID} for the logged-in Claimant Insurer's own entity returns 200 with matching id and name.")
    public void getEntityByIdForOwnEntityReturns200() {
        Response res = tenant().get("/Entity/GetById/" + entityId());
        ApiAssertions.assertEnvelopeSuccess(res);
        assertThat(res.jsonPath().getString("response.id")).isEqualTo(entityId());
        assertThat(res.jsonPath().getString("response.name")).isNotBlank();
        assertThat(res.jsonPath().getString("response.entityType")).isNotBlank();
    }

    @Test(groups = {"api", "tenant", "entity"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /Entity/InternalGetById/{EntityID} returns the same entity as GetById (internal variant, same data).")
    public void internalGetEntityByIdReturns200() {
        Response res = tenant().get("/Entity/InternalGetById/" + entityId());
        ApiAssertions.assertEnvelopeSuccess(res);
        assertThat(res.jsonPath().getString("response.id")).isEqualTo(entityId());
    }

    @Test(groups = {"api", "tenant", "entity"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /Entity/GetById/{EntityID} with a syntactically valid but non-existent Mongo ObjectId returns an envelope failure, not a 500.")
    public void getEntityByIdWithNonExistentIdReturnsEnvelopeFailure() {
        Response res = tenant().get("/Entity/GetById/000000000000000000000000");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "tenant", "entity"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Entity/List with a minimal page filter returns a paged, non-empty list of the 38 UAE insurers (see MOSADAD_DOMAIN.md).")
    public void listEntitiesWithPageFilterReturns200() {
        Response res = tenant().body(Map.of("pageSize", 5, "pageNumber", 1)).post("/Entity/List");
        ApiAssertions.assertEnvelopeSuccess(res);
        assertThat(res.jsonPath().getList("response.items")).isNotEmpty();
        assertThat(res.jsonPath().getInt("response.items.size()")).isLessThanOrEqualTo(5);
    }

    @Test(groups = {"api", "tenant", "entity"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /Entity/GetById/{EntityID} with a malformed, non-ObjectId string returns a safe envelope failure instead of a server-side exception.")
    public void getEntityByIdWithMalformedIdReturnsEnvelopeFailure() {
        Response res = tenant().get("/Entity/GetById/not-a-valid-entity-id");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "tenant", "entity"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /Entity/ListAllContactPersons/{EntityID} for the logged-in entity returns 200 with a list of contact persons.")
    public void listAllContactPersonsForOwnEntityReturns200() {
        Response res = tenant().get("/Entity/ListAllContactPersons/" + entityId());
        ApiAssertions.assertEnvelopeSuccess(res);
        assertThat(res.jsonPath().getList("response")).isNotNull();
    }

    @Test(groups = {"api", "tenant", "entity"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Entity/Create with an empty body fails ASP.NET Core model validation (HTTP 400) flagging Name and EntityType as required — confirmed live, no entity is persisted.")
    public void createEntityWithEmptyBodyReturnsValidationError() {
        Response res = tenant().body(Map.of()).post("/Entity/Create");
        ApiAssertions.assertValidationProblem(res, "Name", "EntityType");
    }

    @Test(enabled = false, groups = {"api", "tenant", "entity", "mutating"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("STUB — creating a real Entity (insurer) persists a new row in the shared QA tenant list. Enable once product/QA sign off on seeding a disposable test insurer (and its cleanup via DELETE /Entity/{id}).")
    public void createEntityWithValidPayloadReturns200() {
        Response res = tenant().body(Map.of(
                "name", "QA Automation Test Insurer",
                "nameAr", "شركة اختبار",
                "entityType", "Insurance Company",
                "isActive", true
        )).post("/Entity/Create");
        ApiAssertions.assertEnvelopeSuccess(res);
    }

    @Test(enabled = false, groups = {"api", "tenant", "entity", "mutating"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("STUB — updates the logged-in entity's real profile fields in the shared QA environment. Enable only against a disposable entity, never the primary Dubai QA account.")
    public void updateEntityReturns200() {
        Response res = tenant().body(Map.of("id", entityId(), "name", "Should Not Actually Run")).put("/Entity/Update/" + entityId());
        ApiAssertions.assertEnvelopeSuccess(res);
    }

    @Test(enabled = false, groups = {"api", "tenant", "entity", "mutating", "destructive"})
    @Severity(SeverityLevel.BLOCKER)
    @Description("STUB — toggles Activation (enable/disable login) for a real entity. Never run against the shared Dubai QA account; only against a disposable entity created by createEntityWithValidPayloadReturns200.")
    public void toggleEntityActivationReturns200() {
        Response res = tenant().put("/Entity/Activation/" + entityId() + "/true");
        ApiAssertions.assertEnvelopeSuccess(res);
    }

    @Test(enabled = false, groups = {"api", "tenant", "entity", "mutating", "destructive"})
    @Severity(SeverityLevel.BLOCKER)
    @Description("STUB — permanently deletes an Entity from the shared QA tenant list. Only ever run against a disposable entity created by this same suite, never a real insurer.")
    public void deleteEntityReturns200() {
        Response res = tenant().delete("/Entity/some-disposable-entity-id");
        ApiAssertions.assertEnvelopeSuccess(res);
    }
}
