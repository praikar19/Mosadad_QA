package com.mosadad.testing.tests.api.claims;

import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * claims service — Intimation tag. Generates/deletes the formal intimation
 * letters referenced across the claim lifecycle (Approval Letter on
 * quotation acceptance, Recovery Letter on invoice — see
 * MOSADAD_DOMAIN.md §Stage 2/3). All 4 operations are PUT and id-scoped (no
 * unscoped-bulk variant, unlike Quotation/Invoice/ExtraExpenses Accept).
 *
 * <p><b>Bug finding, confirmed live 2026-09-17:</b> for a non-existent
 * claimId with an empty body, {@code HandleQuotationIntimationLetter} and
 * {@code HandleDeleteIntimationLetter} fail gracefully (HTTP 200 envelope,
 * isSuccess:false), but {@code HandleInvoiceIntimationLetter} and
 * {@code HandleLouIntimationLetter} throw an unhandled
 * {@code NullReferenceException} in {@code CollectDataRepository
 * .CreateIntimationLetterRequest} — a raw HTTP 500 with the full .NET stack
 * trace (and internal server IPs/hostnames) returned directly in the
 * response body, because ASP.NET Core's Developer Exception Page is enabled
 * on this QA deployment. Worth reporting to the backend team both as an
 * unhandled-null-reference bug and as an information-disclosure hygiene
 * issue (stack traces + internal network details should not reach API
 * clients, even in QA).
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
