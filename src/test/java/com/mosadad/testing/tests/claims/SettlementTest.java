package com.mosadad.testing.tests.claims;

import com.mosadad.testing.base.BaseTwoActorUiTest;
import com.mosadad.testing.constants.TestDataConstants;
import io.qameta.allure.*;
import org.testng.annotations.Test;

/**
 * Stage 4: Settlement (MOSADAD_DOMAIN.md §Stage 4). Two-actor (claimant +
 * at-fault), so this extends BaseTwoActorUiTest, same as WalletTest.
 *
 * One method below — claimStatusBecomesClosedAfterSettlement — is real,
 * live-verified logic recovered from an original two-actor E2E draft (see
 * ClaimLifecycleFixtures' Javadoc for where it came from), not a TODO
 * placeholder. It's also the riskiest thing in this whole suite: it ends
 * in a real ATB Pay checkout payment against shared QA data, not a
 * sandbox — see ClaimLifecycleFixtures.requestSettleAndCloseClaim()'s
 * Javadoc, which also notes that step was still being actively debugged
 * when work on it stopped. Do not enable this without reviewing that
 * method and confirming the impact first — see CLAUDE.md's risk-level
 * guidance. settlementUpdatesWalletPosition was never built out in the
 * recovered draft; still genuinely untested.
 */
@Epic("Mosadad Recovery Claim")
@Feature("Stage 4 — Settlement")
public class SettlementTest extends BaseTwoActorUiTest {

    @Test(enabled = false, groups = {"stub", "settlement"})
    @Description("At-Fault insurer enters credit note number and uploads the credit note copy.")
    public void atFaultInsurerCanSubmitCreditNote() {
        // TODO — subsumed by claimStatusBecomesClosedAfterSettlement below,
        // which already requests/creates the credit note as part of
        // reaching its own assertion; a dedicated check of the credit-note
        // form itself wasn't built.
    }

    @Test(enabled = false, groups = {"stage", "settlement"})
    @Description("After settlement is recorded, claim status becomes '" + TestDataConstants.STATUS_CLOSED + "' — recovery complete.")
    public void claimStatusBecomesClosedAfterSettlement() {
        ClaimLifecycleFixtures.Stage1Result stage1 =
                ClaimLifecycleFixtures.createAndAcceptClaim(claimantPage, atFaultPage);
        ClaimLifecycleFixtures.QuotationResult quotation =
                ClaimLifecycleFixtures.submitAndAcceptNormalRepairQuotation(stage1);
        ClaimLifecycleFixtures.submitAndAcceptInvoice(stage1, quotation.repairCost());
        // Executes a real settlement payment — see this class's Javadoc.
        ClaimLifecycleFixtures.requestSettleAndCloseClaim(stage1);
    }

    @Test(enabled = false, groups = {"stub", "settlement", "wallet"})
    @Description("Settlement updates the Mosadad Wallet position for both insurers (§9.2 Digital Settlement Flow).")
    public void settlementUpdatesWalletPosition() {
        // TODO
    }
}
