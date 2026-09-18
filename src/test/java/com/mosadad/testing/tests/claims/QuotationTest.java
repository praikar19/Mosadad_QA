package com.mosadad.testing.tests.claims;

import com.mosadad.testing.base.BaseUiTest;
import com.mosadad.testing.constants.TestDataConstants;
import io.qameta.allure.*;
import org.testng.annotations.Test;

/**
 * STUB SUITE — Stage 2: Quotation, Normal Repair / Total Loss / Salvage
 * (MOSADAD_DOMAIN.md §Stage 2). All methods disabled — the real Quotation
 * screen hasn't been walked live yet, so there are no verified selectors
 * to test against; see pages/claims/QuotationPage for the TODO locators
 * and FRAMEWORK.md's verified-vs-stubbed ledger.
 */
@Epic("Mosadad Recovery Claim")
@Feature("Stage 2 — Quotation")
public class QuotationTest extends BaseUiTest {

    @Test(enabled = false, groups = {"stub", "quotation"})
    @Description("Normal Repair: claimant uploads workshop estimate + repair amount; system starts the "
            + TestDataConstants.NORMAL_REPAIR_SLA_HOURS + "-hour SLA timer.")
    public void normalRepairQuotationStartsSlaTimer() {
        // TODO
    }

    @Test(enabled = false, groups = {"stub", "quotation"})
    @Description("At-Fault insurer can Approve a Normal Repair quotation, generating an Approval Letter.")
    public void atFaultInsurerCanApproveNormalRepairQuotation() {
        // TODO
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
