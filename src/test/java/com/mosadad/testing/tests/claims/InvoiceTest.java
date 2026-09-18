package com.mosadad.testing.tests.claims;

import com.mosadad.testing.base.BaseTwoActorUiTest;
import io.qameta.allure.*;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Stage 3: Invoice (MOSADAD_DOMAIN.md §Stage 3). Two-actor (claimant +
 * at-fault), so this extends BaseTwoActorUiTest, same as WalletTest.
 *
 * One method below — atFaultInsurerCanApproveInvoice — is real,
 * live-verified logic recovered from an original two-actor E2E draft (see
 * ClaimLifecycleFixtures' Javadoc for where it came from), not a TODO
 * placeholder. Negotiate and the recovery-letter/notification checks were
 * never built out in that draft; still genuinely untested. All methods
 * stay enabled=false regardless — see FRAMEWORK.md's verified-vs-stubbed
 * ledger and CLAUDE.md's risk-level guidance before enabling any of them.
 */
@Epic("Mosadad Recovery Claim")
@Feature("Stage 3 — Invoice")
public class InvoiceTest extends BaseTwoActorUiTest {

    @Test(enabled = false, groups = {"stub", "invoice"})
    @Description("Claimant submits final repair invoice number, credit note paid to workshop, and supporting documents.")
    public void claimantCanSubmitFinalInvoice() {
        // TODO — subsumed by atFaultInsurerCanApproveInvoice below, which
        // already submits a real invoice as part of reaching its own
        // assertion; a dedicated claimant-side-only check wasn't built.
    }

    @Test(enabled = false, groups = {"stub", "invoice"})
    @Description("Submitting an invoice generates a recovery letter and notifies the At-Fault insurer.")
    public void invoiceSubmissionGeneratesRecoveryLetterAndNotifiesAtFault() {
        // TODO — no recovery-letter/notification selector has been
        // identified yet; this wasn't covered by the recovered E2E draft.
    }

    @Test(enabled = false, groups = {"stage", "invoice"})
    @Description("At-Fault insurer can Approve the invoice, moving the claim into the Settlement stage.")
    public void atFaultInsurerCanApproveInvoice() {
        ClaimLifecycleFixtures.Stage1Result stage1 =
                ClaimLifecycleFixtures.createAndAcceptClaim(claimantPage, atFaultPage);
        ClaimLifecycleFixtures.QuotationResult quotation =
                ClaimLifecycleFixtures.submitAndAcceptNormalRepairQuotation(stage1);
        String invoiceNumber = ClaimLifecycleFixtures.submitAndAcceptInvoice(stage1, quotation.repairCost());

        assertThat(invoiceNumber)
                .as("An invoice number should have been generated and submitted")
                .isNotBlank();
    }

    @Test(enabled = false, groups = {"stub", "invoice"})
    @Description("At-Fault insurer can Negotiate the invoice instead of approving it.")
    public void atFaultInsurerCanNegotiateInvoice() {
        // TODO
    }
}
