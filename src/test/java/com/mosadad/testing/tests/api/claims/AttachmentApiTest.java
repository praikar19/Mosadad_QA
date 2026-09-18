package com.mosadad.testing.tests.api.claims;

import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** claims service — Attachment tag. Supporting-document uploads across every claim stage (see MOSADAD_DOMAIN.md — every stage lists "supporting documents"). */
@Epic("Mosadad Recovery Claim")
@Feature("Claims API — Attachment")
public class AttachmentApiTest extends BaseApiTest {

    @Test(groups = {"api", "claims", "attachment"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Attachment/Upload with an empty body is a real, safe no-op — confirmed live it returns 200 'Attachements Uploaded Successfully' with empty results and uploads nothing.")
    public void uploadAttachmentWithEmptyBodyIsSafeNoOp() {
        Response res = claims().body(Map.of()).post("/Attachment/Upload");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", true, "envelope isSuccess");
    }

    @Test(groups = {"api", "claims", "attachment"})
    @Severity(SeverityLevel.MINOR)
    @Description("POST /Attachment/Generate (SAS token generation for direct blob upload) with an empty array is a safe no-op — confirmed live it returns 200 with an empty result.")
    public void generateAttachmentSasTokensWithEmptyArrayIsSafeNoOp() {
        Response res = claims().body(Collections.emptyList()).post("/Attachment/Generate");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        assertThat(res.jsonPath().getList("response")).isEmpty();
    }

    @Test(groups = {"api", "claims", "attachment"})
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /Attachment/Quotation?claimId= for a real claim with no quotation attachments returns 200 with empty quotationAttachments/otherAttachments lists, not a 404.")
    public void getQuotationAttachmentsForClaimWithNoneReturns200() {
        Response res = claims().queryParam("claimId", "6aab489ecb32564549128fa6").get("/Attachment/Quotation");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", true, "envelope isSuccess");
        assertThat(res.jsonPath().getList("response.quotationAttachments")).isEmpty();
    }

    @Test(groups = {"api", "claims", "attachment"})
    @Severity(SeverityLevel.NORMAL)
    @Description("PUT /Attachment/Quotation with an empty body fails model validation (HTTP 400): ClaimId and AddedQuotationDocuments are required.")
    public void updateQuotationAttachmentsWithEmptyBodyReturnsValidationError() {
        Response res = claims().body(Map.of()).put("/Attachment/Quotation");
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.jsonPath().getMap("errors")).containsKeys("ClaimId", "AddedQuotationDocuments");
    }

    @Test(groups = {"api", "claims", "attachment"})
    @Severity(SeverityLevel.NORMAL)
    @Description("PUT /Attachment/Invoice with an empty body fails model validation (HTTP 400): ClaimId and AddedInvoiceDocuments are required.")
    public void updateInvoiceAttachmentsWithEmptyBodyReturnsValidationError() {
        Response res = claims().body(Map.of()).put("/Attachment/Invoice");
        ApiAssertions.assertStatusCode(res, 400, "HTTP status");
        assertThat(res.jsonPath().getMap("errors")).containsKeys("ClaimId", "AddedInvoiceDocuments");
    }
}
