package com.mosadad.testing.tests.api.claims;

import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * claims service — Intimation letters. Known bug: the Invoice and LOU letter
 * endpoints return HTTP 500 with a full stack trace for an unknown claimId.
 */
@Epic("Mosadad Recovery Claim")
@Feature("Claims API — Intimation")
public class IntimationApiTest extends BaseApiTest {

    @Test(groups = {"api", "claims", "intimation"})
    @Severity(SeverityLevel.NORMAL)
    @Description("PUT /Intimation/HandleQuotationIntimationLetter/{claimId} for a non-existent claim with an empty body returns an envelope failure (statusCode 500), not a raw crash.")
    public void handleQuotationIntimationLetterForNonExistentClaimReturnsEnvelopeFailure() {
        Response res = claims().body(Map.of()).put("/Intimation/HandleQuotationIntimationLetter/000000000000000000000000");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "claims", "intimation", "bug"})
    @Severity(SeverityLevel.NORMAL)
    @Description("BUG (see class javadoc) — PUT /Intimation/HandleInvoiceIntimationLetter/{claimId} for a non-existent claim with an empty body throws an unhandled NullReferenceException: raw HTTP 500 with a full .NET stack trace in the body, not the usual envelope failure.")
    public void handleInvoiceIntimationLetterForNonExistentClaimThrowsUnhandledException() {
        Response res = claims().body(Map.of()).put("/Intimation/HandleInvoiceIntimationLetter/000000000000000000000000");
        ApiAssertions.assertStatusCode(res, 500, "HTTP status");
        assertThat(res.asString()).containsIgnoringCase("NullReferenceException");
    }

    @Test(groups = {"api", "claims", "intimation", "bug"})
    @Severity(SeverityLevel.NORMAL)
    @Description("BUG (see class javadoc) — PUT /Intimation/HandleLouIntimationLetter/{claimId} for a non-existent claim with an empty body throws the same unhandled NullReferenceException as HandleInvoiceIntimationLetter.")
    public void handleLouIntimationLetterForNonExistentClaimThrowsUnhandledException() {
        Response res = claims().body(Map.of()).put("/Intimation/HandleLouIntimationLetter/000000000000000000000000");
        ApiAssertions.assertStatusCode(res, 500, "HTTP status");
        assertThat(res.asString()).containsIgnoringCase("NullReferenceException");
    }

    @Test(groups = {"api", "claims", "intimation"})
    @Severity(SeverityLevel.NORMAL)
    @Description("PUT /Intimation/HandleDeleteIntimationLetter/{claimId} for a non-existent claim with an empty body returns an envelope failure, not a raw crash.")
    public void handleDeleteIntimationLetterForNonExistentClaimReturnsEnvelopeFailure() {
        Response res = claims().body(Map.of()).put("/Intimation/HandleDeleteIntimationLetter/000000000000000000000000");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }
}
