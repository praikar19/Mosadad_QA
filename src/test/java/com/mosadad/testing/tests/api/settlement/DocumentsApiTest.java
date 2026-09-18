package com.mosadad.testing.tests.api.settlement;

import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** settlement service — Documents tag. Same token-signed content-serving contract as quotation's Documents tag (credit note copies — see MOSADAD_DOMAIN.md §Stage 4), confirmed live independently since each microservice has its own implementation. */
@Epic("Mosadad Recovery Claim")
@Feature("Settlement API — Documents")
public class DocumentsApiTest extends BaseApiTest {

    @Test(groups = {"api", "settlement", "documents"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /Documents/content with no token query param fails model validation (HTTP 400): token is required.")
    public void getDocumentContentWithoutTokenReturnsValidationError() {
        Response res = settlement().get("/Documents/content");
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.jsonPath().getMap("errors")).containsKey("token");
    }

    @Test(groups = {"api", "settlement", "documents"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /Documents/content with an invalid token returns a real HTTP 401.")
    public void getDocumentContentWithInvalidTokenReturns401() {
        Response res = settlement().queryParam("token", "not-a-real-signed-token").get("/Documents/content");
        ApiAssertions.assertStatusCode(res, 401, "HTTP status");
    }
}
