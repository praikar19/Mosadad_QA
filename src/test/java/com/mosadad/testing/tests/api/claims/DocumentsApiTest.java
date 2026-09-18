package com.mosadad.testing.tests.api.claims;

import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** claims service — Documents tag. Same token-signed content-serving contract as quotation/settlement/invoice's Documents tags, confirmed live independently for the claims service. */
@Epic("Mosadad Recovery Claim")
@Feature("Claims API — Documents")
public class DocumentsApiTest extends BaseApiTest {

    @Test(groups = {"api", "claims", "documents"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /Documents/content with no token query param fails model validation (HTTP 400): token is required.")
    public void getDocumentContentWithoutTokenReturnsValidationError() {
        Response res = claims().get("/Documents/content");
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.jsonPath().getMap("errors")).containsKey("token");
    }
}
