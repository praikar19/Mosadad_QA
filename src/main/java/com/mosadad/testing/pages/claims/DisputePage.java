package com.mosadad.testing.pages.claims;

import com.mosadad.testing.pages.BasePage;
import com.microsoft.playwright.Page;

/**
 * STUB — Disputes (see MOSADAD_DOMAIN.md §Disputes). Not yet explored live;
 * selectors are TODO placeholders.
 *
 * When insurers disagree on liability, repair amount, or invoice, a
 * structured dispute is raised in-system instead of over email: claimant
 * raises it, at-fault responds, all communication + timestamps are logged.
 * Undisputed amounts can still move forward. Unresolved disputes are
 * visible to the Regulator for the full history.
 */
public class DisputePage extends BasePage {

    // TODO: verify against the live dispute screen.
    private static final String RAISE_DISPUTE_BUTTON = "TODO-raise-dispute-button";
    private static final String DISPUTE_REASON_SELECT = "TODO-dispute-reason-select";
    private static final String DISPUTE_COMMENT_INPUT = "TODO-dispute-comment-input";
    private static final String SUBMIT_DISPUTE_BUTTON = "TODO-submit-dispute-button";
    private static final String DISPUTE_HISTORY_LIST = "TODO-dispute-history-list";
    private static final String RESPOND_TO_DISPUTE_BUTTON = "TODO-respond-to-dispute-button";

    public DisputePage(Page page) {
        super(page);
    }

    public void raiseDispute(String reason, String comment) {
        click(RAISE_DISPUTE_BUTTON);
        locator(DISPUTE_REASON_SELECT).selectOption(reason);
        fill(DISPUTE_COMMENT_INPUT, comment);
        click(SUBMIT_DISPUTE_BUTTON);
    }

    public void respondToDispute(String comment) {
        fill(DISPUTE_COMMENT_INPUT, comment);
        click(RESPOND_TO_DISPUTE_BUTTON);
    }

    public int getDisputeHistoryEntryCount() {
        return locator(DISPUTE_HISTORY_LIST).count();
    }
}
