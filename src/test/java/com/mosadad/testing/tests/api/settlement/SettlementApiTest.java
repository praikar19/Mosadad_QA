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
 * settlement service — Settlement tag. The Digital Settlement Flow's
 * per-counterpart-insurer summary (see MOSADAD_DOMAIN.md §Wallet, Settlement
 * Rail & UAE PASS). Despite the name, {@code POST /Settlement/{otherEntityId}}
 * is a real/list endpoint (confirmed live: "Settlement Details Retrieved
 * Successfully"), not a create — matching the {@code Settlement/Companies}
 * search pattern.
 */
@Epic("Mosadad Recovery Claim")
@Feature("Settlement API — Settlement")
public class SettlementApiTest extends BaseApiTest {

    @Test(groups = {"api", "settlement"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /Settlement/Summary returns 200 with the entity's real totalPayable/totalReceivable position — the numbers behind the 'My Wallets' dashboard entry (see MOSADAD_DOMAIN.md §Wallet).")
    public void getSettlementSummaryReturns200() {
        Response res = settlement().get("/Settlement/Summary");
        ApiAssertions.assertEnvelopeSuccess(res);
        assertThat(res.jsonPath().getObject("response.totalPayable", Double.class)).isNotNull();
        assertThat(res.jsonPath().getObject("response.totalReceivable", Double.class)).isNotNull();
    }

    @Test(groups = {"api", "settlement"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Settlement/Companies with an empty body returns 200 with real per-counterpart-insurer payable/receivable/net amounts.")
    public void listSettlementCompaniesReturns200() {
        Response res = settlement().body(Map.of()).post("/Settlement/Companies");
        ApiAssertions.assertEnvelopeSuccess(res);
        List<Map<String, Object>> items = res.jsonPath().getList("response.items");
        assertThat(items).isNotEmpty();
        assertThat(items.get(0)).containsKeys("companyName", "companyId", "totalPayable", "totalReceivable", "netAmount");
    }

    @Test(groups = {"api", "settlement"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /Settlement/{otherEntityId} for a real counterpart entity returns 200 with that counterpart's settlement detail list (possibly empty).")
    public void getSettlementDetailsForRealCounterpartReturns200() {
        Response res = settlement().body(Map.of()).post("/Settlement/671e4678958a85e6c27fa456");
        ApiAssertions.assertEnvelopeSuccess(res);
        assertThat(res.jsonPath().getList("response.items")).isNotNull();
    }

    @Test(groups = {"api", "settlement", "notifies"})
    @Severity(SeverityLevel.MINOR)
    @Description("GET /Settlement/Notify/{otherEntityId}/{claimId} always reports success, even for two well-formed but non-existent ids — confirmed live, same unvalidated-notify behavior as CreditNoteApiTest's Notify endpoint. Deliberately uses fake ids so no real counterpart is notified.")
    public void notifySettlementWithFakeIdsStillReturnsSuccess() {
        Response res = settlement().get("/Settlement/Notify/000000000000000000000000/000000000000000000000000");
        ApiAssertions.assertEnvelopeSuccess(res);
        ApiAssertions.assertJsonBoolean(res, "response", true, "envelope response");
    }
}
