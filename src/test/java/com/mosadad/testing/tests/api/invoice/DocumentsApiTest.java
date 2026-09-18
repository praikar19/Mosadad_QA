package com.mosadad.testing.tests.api.invoice;

import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** invoice service — Documents tag. Same token-signed content-serving contract as quotation/settlement's Documents tags (invoice attachments — see MOSADAD_DOMAIN.md §Stage 3), confirmed live independently. */
@Epic("Mosadad Recovery Claim")
@Feature("Invoice API — Documents")
public class DocumentsApiTest extends BaseApiTest {

    @Test(groups = {"api", "invoice", "documents"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /Documents/content with no token query param fails model validation (HTTP 400): token is required.")
    public void getDocumentContentWithoutTokenReturnsValidationError() {
        Response res = invoice().get("/Documents/content");
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.jsonPath().getMap("errors")).containsKey("token");
    }

    @Test(groups = {"api", "invoice", "documents"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /Documents/content with an invalid token returns a real HTTP 401.")
    public void getDocumentContentWithInvalidTokenReturns401() {
        Response res = invoice().queryParam("token", "not-a-real-signed-token").get("/Documents/content");
        ApiAssertions.assertStatusCode(res, 401, "HTTP status");
    }
}
