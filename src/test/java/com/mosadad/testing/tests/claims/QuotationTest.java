package com.mosadad.testing.tests.claims;

import com.mosadad.testing.base.BaseTwoActorUiTest;
import com.mosadad.testing.constants.TestDataConstants;
import io.qameta.allure.*;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Stage 2: Quotation, Normal Repair / Total Loss / Salvage
 * (MOSADAD_DOMAIN.md §Stage 2). Two-actor (claimant + at-fault), so this
 * extends BaseTwoActorUiTest, same as WalletTest.
 *
 * One method below — atFaultInsurerCanApproveNormalRepairQuotation — is
 * real, live-verified logic recovered from an original two-actor E2E
 * draft (see ClaimLifecycleFixtures' Javadoc for where it came from), not
 * a TODO placeholder. The other methods are still genuinely untested —
 * Negotiate, Total Loss, Salvage, and the SLA-timer/missed-deadline
 * assertions were never built out in that draft either. All methods stay
 * enabled=false regardless — see FRAMEWORK.md's verified-vs-stubbed
 * ledger and CLAUDE.md's risk-level guidance before enabling any of them.
 */
@Epic("Mosadad Recovery Claim")
@Feature("Stage 2 — Quotation")
public class QuotationTest extends BaseTwoActorUiTest {

    @Test(enabled = false, groups = {"stub", "quotation"})
    @Description("Normal Repair: claimant uploads workshop estimate + repair amount; system starts the "
            + TestDataConstants.NORMAL_REPAIR_SLA_HOURS + "-hour SLA timer.")
    public void normalRepairQuotationStartsSlaTimer() {
        // TODO — no SLA-timer selector has been identified yet; this method
        // wasn't covered by the recovered E2E draft either.
    }

    @Test(enabled = false, groups = {"stage", "quotation"})
    @Description("At-Fault insurer can Approve a Normal Repair quotation, generating an Approval Letter.")
    public void atFaultInsurerCanApproveNormalRepairQuotation() {
        ClaimLifecycleFixtures.Stage1Result stage1 =
                ClaimLifecycleFixtures.createAndAcceptClaim(claimantPage, atFaultPage);
        ClaimLifecycleFixtures.QuotationResult quotation =
                ClaimLifecycleFixtures.submitAndAcceptNormalRepairQuotation(stage1);

        assertThat(quotation.quotationNumber())
                .as("A quotation number should be assigned on submission")
                .isNotBlank();
    }

    @Test(enabled = false, groups = {"stub", "quotation"})
    @Description("At-Fault insurer can Negotiate a Normal Repair quotation; negotiation continues in-system until both sides agree.")
    public void atFaultInsurerCanNegotiateNormalRepairQuotation() {
        // TODO
    }

    @Test(enabled = false, groups = {"stub", "quotation"})
    @Description("Total Loss: claimant submits sum insured + total loss amount; system starts the "
            + TestDataConstants.TOTAL_LOSS_SLA_HOURS + "-hour (7-day) SLA timer.")
    public void totalLossDeclarationStartsSlaTimer() {
        // TODO
    }

    @Test(enabled = false, groups = {"stub", "quotation", "salvage"})
    @Description("Salvage: once Total Loss is approved, claimant enters buyer details + salvage amount; system auto-adds "
            + TestDataConstants.SALVAGE_VAT_PERCENT + "% VAT and updates the total claim amount.")
    public void salvageAmountGetsVatAutoAdded() {
        // TODO — assert displayed total == salvageAmount * 1.05
    }

    @Test(enabled = false, groups = {"stub", "quotation"})
    @Description("Missed SLA response deadlines must be recorded (visible to Regulator per §Why Response Timers Matter).")
    public void missedSlaDeadlineIsRecorded() {
        // TODO — requires Regulator test credentials, not yet provisioned.
    }
}
