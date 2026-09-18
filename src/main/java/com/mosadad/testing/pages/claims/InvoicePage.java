package com.mosadad.testing.pages.claims;

import com.mosadad.testing.pages.BasePage;
import com.microsoft.playwright.Page;

/**
 * STUB — Stage 3: Invoice (see MOSADAD_DOMAIN.md §Stage 3). Not yet explored
 * live; selectors are TODO placeholders.
 *
 * Claimant uploads final repair invoice number, credit note paid to
 * workshop, and supporting documents. System generates a recovery letter
 * and notifies the At-Fault insurer, who then Approves or Negotiates.
 * Approval moves the claim into the Settlement stage.
 */
public class InvoicePage extends BasePage {

    // TODO: verify against the live invoice screen.
    private static final String INVOICE_NUMBER_INPUT = "TODO-invoice-number-input";
    private static final String CREDIT_NOTE_INPUT = "TODO-credit-note-input";
    private static final String SUPPORTING_DOCUMENT_UPLOAD = "TODO-supporting-document-upload";
    private static final String SUBMIT_INVOICE_BUTTON = "TODO-submit-invoice-button";

    private static final String APPROVE_BUTTON = "TODO-approve-invoice-button";
    private static final String NEGOTIATE_BUTTON = "TODO-negotiate-invoice-button";

    public InvoicePage(Page page) {
        super(page);
    }

    public void submitInvoice(String invoiceNumber, String creditNote, String supportingDocPath) {
        fill(INVOICE_NUMBER_INPUT, invoiceNumber);
        fill(CREDIT_NOTE_INPUT, creditNote);
        locator(SUPPORTING_DOCUMENT_UPLOAD).setInputFiles(java.nio.file.Paths.get(supportingDocPath));
        click(SUBMIT_INVOICE_BUTTON);
    }

    public void approve() {
        click(APPROVE_BUTTON);
    }

    public void negotiate() {
        click(NEGOTIATE_BUTTON);
    }
}
