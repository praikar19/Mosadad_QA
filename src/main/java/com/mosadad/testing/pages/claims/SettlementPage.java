package com.mosadad.testing.pages.claims;

import com.mosadad.testing.pages.BasePage;
import com.microsoft.playwright.Page;

/**
 * STUB — Stage 4: Settlement (see MOSADAD_DOMAIN.md §Stage 4). Not yet
 * explored live; selectors are TODO placeholders.
 *
 * At-Fault insurer enters the credit note number and uploads the credit
 * note copy. Once done, claim status becomes Closed — recovery complete.
 * This mirrors the Digital Settlement Flow described in §9.2: claim
 * approved -> invoice approved -> credit note issued -> settlement
 * recorded -> wallet updated -> claim Closed.
 */
public class SettlementPage extends BasePage {

    // TODO: verify against the live settlement screen.
    private static final String CREDIT_NOTE_NUMBER_INPUT = "TODO-credit-note-number-input";
    private static final String CREDIT_NOTE_UPLOAD = "TODO-credit-note-upload";
    private static final String CONFIRM_SETTLEMENT_BUTTON = "TODO-confirm-settlement-button";
    private static final String CLAIM_STATUS_BADGE = "TODO-claim-status-badge";

    public SettlementPage(Page page) {
        super(page);
    }

    public void settleClaim(String creditNoteNumber, String creditNoteFilePath) {
        fill(CREDIT_NOTE_NUMBER_INPUT, creditNoteNumber);
        locator(CREDIT_NOTE_UPLOAD).setInputFiles(java.nio.file.Paths.get(creditNoteFilePath));
        click(CONFIRM_SETTLEMENT_BUTTON);
    }

    public String getClaimStatus() {
        return getText(CLAIM_STATUS_BADGE);
    }
}
