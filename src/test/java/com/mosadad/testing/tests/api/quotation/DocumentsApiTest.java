package com.mosadad.testing.tests.api.quotation;

import com.mosadad.testing.api.ApiClient.Service;
import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** quotation service — Documents tag. Serves uploaded supporting-document content by a signed token (workshop estimates, repair amounts — see MOSADAD_DOMAIN.md §Stage 2A). */
@Epic("Mosadad Recovery Claim")
@Feature("Quotation API — Documents")
public class DocumentsApiTest extends BaseApiTest {

    @Test(groups = {"api", "quotation", "documents"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /Documents/content with no token query param fails model validation (HTTP 400): token is required.")
    public void getDocumentContentWithoutTokenReturnsValidationError() {
        Response res = quotation().get("/Documents/content");
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.jsonPath().getMap("errors")).containsKey("token");
    }

    @Test(groups = {"api", "quotation", "documents"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /Documents/content with a syntactically present but invalid/expired token returns a real HTTP 401 — confirmed live, not an envelope failure.")
    public void getDocumentContentWithInvalidTokenReturns401() {
        Response res = quotation().queryParam("token", "not-a-real-signed-token").get("/Documents/content");
        ApiAssertions.assertStatusCode(res, 401, "HTTP status");
    }

    @Test(groups = {"api", "quotation", "documents", "security"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /Documents/content is reachable without a bearer token at all (it authorizes via its own signed 'token' query param instead) — a missing Authorization header alone does not gate this endpoint; the invalid document token still yields 401 either way.")
    public void getDocumentContentWithoutBearerTokenStillGatesOnDocumentToken() {
        Response res = noAuth(Service.QUOTATION).queryParam("token", "not-a-real-signed-token").get("/Documents/content");
        ApiAssertions.assertStatusCode(res, 401, "HTTP status");
    }
}
