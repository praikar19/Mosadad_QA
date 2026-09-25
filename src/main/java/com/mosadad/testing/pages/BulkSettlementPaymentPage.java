package com.mosadad.testing.pages;

import com.mosadad.testing.constants.Routes;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;

import java.util.List;

/**
 * /entity-portal/bulk-settlement-payment ("Bulk Settlement Payment") — the
 * at-fault's accepted claims payable to one entity, reached via Bulk Settlement
 * → Settle. See SettlementTable for how the table and panel behave. "Issue
 * Credit Note" shows "Credit Note issued successfully." and returns to Bulk
 * Settlement (from the app's code, 2026-09-24).
 */
public class BulkSettlementPaymentPage extends BasePage {

    private static final String ISSUE_CREDIT_NOTE_BTN = "Issue Credit Note";
    private static final String TOAST_MESSAGE = "#toast-container .toast-message";

    public BulkSettlementPaymentPage(Page page) {
        super(page);
    }

    public boolean isLoaded() {
        return currentUrl().contains(Routes.BULK_SETTLEMENT_PAYMENT);
    }

    @Step("Select only claims {0}")
    public BulkSettlementPaymentPage selectOnly(List<String> claimSerialNumbers) {
        SettlementTable.selectOnly(page, claimSerialNumbers, "Bulk Settlement Payment");
        return this;
    }

    /** "count / total" as the side panel shows it once it matches, e.g. "2 / 7,200.00". */
    public String waitForSummary(int expectedCount, String expectedTotal) {
        return SettlementTable.waitForSummary(page, expectedCount, expectedTotal);
    }

    /**
     * Issues the credit note for the ticked claims and waits for the return to Bulk
     * Settlement; returns the toast text. Fails with the toast if it stays on this page.
     */
    @Step("Click 'Issue Credit Note'")
    public String issueCreditNote() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(ISSUE_CREDIT_NOTE_BTN).setExact(true)).click();
        Locator toast = locator(TOAST_MESSAGE).first();
        String toastText = "";
        try {
            toast.waitFor(new Locator.WaitForOptions().setTimeout(20000));
            toastText = toast.innerText().trim();
            page.waitForURL(url -> !url.contains(Routes.BULK_SETTLEMENT_PAYMENT),
                    new Page.WaitForURLOptions().setTimeout(20000));
        } catch (com.microsoft.playwright.PlaywrightException e) {
            throw new IllegalStateException("'Issue Credit Note' didn't return to Bulk Settlement; toast: '"
                    + toastText + "'", e);
        }
        return toastText;
    }
}
