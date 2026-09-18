package com.mosadad.testing.tests.claims;

import com.mosadad.testing.base.BaseUiTest;
import io.qameta.allure.*;
import org.testng.annotations.Test;

/**
 * STUB SUITE — Stage 3: Invoice (MOSADAD_DOMAIN.md §Stage 3). All methods
 * disabled — the real Invoice screen hasn't been walked live yet, so
 * there are no verified selectors to test against. See FRAMEWORK.md's
 * verified-vs-stubbed ledger.
 */
@Epic("Mosadad Recovery Claim")
@Feature("Stage 3 — Invoice")
public class InvoiceTest extends BaseUiTest {

    @Test(enabled = false, groups = {"stub", "invoice"})
    @Description("Claimant submits final repair invoice number, credit note paid to workshop, and supporting documents.")
    public void claimantCanSubmitFinalInvoice() {
        // TODO
    }

    @Test(enabled = false, groups = {"stub", "invoice"})
    @Description("Submitting an invoice generates a recovery letter and notifies the At-Fault insurer.")
    public void invoiceSubmissionGeneratesRecoveryLetterAndNotifiesAtFault() {
        // TODO
    }

    @Test(enabled = false, groups = {"stub", "invoice"})
    @Description("At-Fault insurer can Approve the invoice, moving the claim into the Settlement stage.")
    public void atFaultInsurerCanApproveInvoice() {
        // TODO
    }

    @Test(enabled = false, groups = {"stub", "invoice"})
    @Description("At-Fault insurer can Negotiate the invoice instead of approving it.")
    public void atFaultInsurerCanNegotiateInvoice() {
        // TODO
    }
}
