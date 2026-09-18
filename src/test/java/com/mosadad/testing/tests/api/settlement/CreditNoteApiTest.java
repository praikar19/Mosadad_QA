package com.mosadad.testing.tests.api.settlement;

import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * settlement service — CreditNote tag. Stage 4 of the claim lifecycle (see
 * MOSADAD_DOMAIN.md §Stage 4: at-fault insurer issues a credit note,
 * settlement is recorded, claim closes). "Bulk" here means aggregated
 * across many claims against one counterpart insurer (Fast Track's
 * counterpart — see MOSADAD_DOMAIN.md §Fast Track), not a bulk-write.
 *
 * <p><b>Caution — {@code GET /CreditNote/Notify/{otherEntityId}/{claimId}}
 * is a real side-effecting action despite the GET verb.</b> Confirmed live
 * it queues/sends a real notification and returns
 * {@code "notification sent successfully":true} even for two well-formed
 * but non-existent ids — it does not validate that either id refers to a
 * real record before "succeeding". There is no read-only way to probe it;
 * the test below deliberately uses fake ids to keep the call deterministic
 * and avoid notifying a real counterpart insurer, but every run of it still
 * performs a real action. Consider excluding the {@code "notifies"} group
 * from routine CI runs if that's undesirable.
 */
@Epic("Mosadad Recovery Claim")
@Feature("Settlement API — CreditNote")
public class CreditNoteApiTest extends BaseApiTest {

    @Test(groups = {"api", "settlement", "credit-note"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /CreditNote/Bulk/Payable with an empty filter returns 200 with real per-counterpart-insurer payable credit note totals.")
    public void listBulkPayableCreditNotesReturns200() {
        Response res = settlement().body(Map.of()).post("/CreditNote/Bulk/Payable");
        ApiAssertions.assertEnvelopeSuccess(res);
        assertThat(res.jsonPath().getList("response.items")).isNotNull();
    }

    @Test(groups = {"api", "settlement", "credit-note"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /CreditNote/Bulk/Recoverable with an empty filter returns 200 with real per-counterpart-insurer recoverable credit note totals.")
    public void listBulkRecoverableCreditNotesReturns200() {
        Response res = settlement().body(Map.of()).post("/CreditNote/Bulk/Recoverable");
        ApiAssertions.assertEnvelopeSuccess(res);
        List<Map<String, Object>> items = res.jsonPath().getList("response.items");
        assertThat(items).isNotEmpty();
        assertThat(items.get(0)).containsKeys("name", "numberOfClaims", "creditNoteAmount", "otherEntityId");
    }

    @Test(groups = {"api", "settlement", "credit-note"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /CreditNote/Bulk/Details/{isFaulty}/{entityId} for a real counterpart entity returns 200 with the individual claims behind that bulk total.")
    public void listBulkCreditNoteDetailsForRealEntityReturns200() {
        Response res = settlement().body(Map.of()).post("/CreditNote/Bulk/Details/true/671e4678958a85e6c27fa456");
        ApiAssertions.assertEnvelopeSuccess(res);
        assertThat(res.jsonPath().getList("response.items")).isNotNull();
    }

    @Test(groups = {"api", "settlement", "credit-note"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /CreditNote/Bulk/History with an empty filter returns 200 with real past bulk-settlement transactions.")
    public void listBulkHistoryReturns200() {
        Response res = settlement().body(Map.of()).post("/CreditNote/Bulk/History");
        ApiAssertions.assertEnvelopeSuccess(res);
        List<Map<String, Object>> items = res.jsonPath().getList("response.items");
        assertThat(items).isNotEmpty();
        assertThat(items.get(0)).containsKeys("transactionId", "insuranceCompanyName", "totalAmount");
    }

    @Test(groups = {"api", "settlement", "credit-note"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /CreditNote/Bulk/History/{transactionId} for a real transaction (from Bulk/History) with a page filter returns 200 with the claims inside that transaction.")
    public void getBulkHistoryDetailsForRealTransactionReturns200() {
        String transactionId = settlement().body(Map.of()).post("/CreditNote/Bulk/History")
                .jsonPath().getString("response.items[0].transactionId");
        Response res = settlement().body(Map.of("pageSize", 10, "pageNumber", 1)).post("/CreditNote/Bulk/History/" + transactionId);
        ApiAssertions.assertEnvelopeSuccess(res);
        assertThat(res.jsonPath().getList("response.items")).isNotNull();
    }

    @Test(groups = {"api", "settlement", "credit-note"})
    @Severity(SeverityLevel.MINOR)
    @Description("POST /CreditNote/Bulk/History/{transactionId} with NO page filter fails server-side with a raw Mongo aggregation error (envelope statusCode 500, '$skip: null') instead of defaulting pagination — a real gap confirmed live, not this suite's assumption; callers must always supply pageSize/pageNumber.")
    public void getBulkHistoryDetailsWithoutPageFilterReturnsServerError() {
        Response res = settlement().body(Map.of()).post("/CreditNote/Bulk/History/8fdb0b42-1997-4d00-87e9-9b522f0d46a4");
        ApiAssertions.assertStatusCode(res, 200, "HTTP status");
        ApiAssertions.assertJsonBoolean(res, "isSuccess", false, "envelope isSuccess");
    }

    @Test(groups = {"api", "settlement", "credit-note", "notifies"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /CreditNote/Notify/{otherEntityId}/{claimId} always reports success, even for two well-formed but non-existent ids — confirmed live it does not validate either id first. Deliberately uses fake ids to avoid notifying a real counterpart insurer, but this is still a real, non-idempotent call every time it runs — see class javadoc.")
    public void notifyCreditNoteWithFakeIdsStillReturnsSuccess() {
        Response res = settlement().get("/CreditNote/Notify/000000000000000000000000/000000000000000000000000");
        ApiAssertions.assertEnvelopeSuccess(res);
        ApiAssertions.assertJsonBoolean(res, "response", true, "envelope response");
    }
}
