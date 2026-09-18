package com.mosadad.testing.tests.claims;

import com.mosadad.testing.base.BaseUiTest;
import com.mosadad.testing.constants.TestDataConstants;
import io.qameta.allure.*;
import org.testng.annotations.Test;

/**
 * STUB SUITE — Stage 4: Settlement (MOSADAD_DOMAIN.md §Stage 4). All methods
 * disabled — the real Settlement screen hasn't been walked live yet, so
 * there are no verified selectors to test against. See FRAMEWORK.md's
 * verified-vs-stubbed ledger.
 */
@Epic("Mosadad Recovery Claim")
@Feature("Stage 4 — Settlement")
public class SettlementTest extends BaseUiTest {

    @Test(enabled = false, groups = {"stub", "settlement"})
    @Description("At-Fault insurer enters credit note number and uploads the credit note copy.")
    public void atFaultInsurerCanSubmitCreditNote() {
        // TODO
    }

    @Test(enabled = false, groups = {"stub", "settlement"})
    @Description("After settlement is recorded, claim status becomes '" + TestDataConstants.STATUS_CLOSED + "' — recovery complete.")
    public void claimStatusBecomesClosedAfterSettlement() {
        // TODO
    }

    @Test(enabled = false, groups = {"stub", "settlement", "wallet"})
    @Description("Settlement updates the Mosadad Wallet position for both insurers (§9.2 Digital Settlement Flow).")
    public void settlementUpdatesWalletPosition() {
        // TODO
    }
}
