package com.mosadad.testing.pages.claims;

import com.mosadad.testing.constants.Routes;
import com.mosadad.testing.pages.BasePage;
import com.mosadad.testing.pages.BulkSettlementPage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Claims in one Fast Track batch. Columns (verified live on stage): Serial
 * Number, Claim Number, Approved Claim Amount, Accident Report, Invoice,
 * Status. The claimant sees its own claim numbers as links; the at-fault sees
 * "-" until it re-uploads the sheet with its own claim numbers.
 */
public class FastTrackClaimsListPage extends BasePage {

    private static final String CLAIM_NUMBER_LINK = "table a";
    private static final String BODY_ROWS = "table tbody tr";
    private static final int CLAIM_NUMBER_COLUMN = 1;
    private static final String SEARCH_PLACEHOLDER = "Accident Number or Plate Number";
    private static final String SUBMIT_TO_AT_FAULT_BTN = "Submit to At-Fault";
    private static final String PROCEED_TO_SETTLEMENT_BTN = "Proceed to Settlement";
    // The app's own <app-batch-popup> with a native <dialog>, not a Material/Bootstrap modal (verified live).
    private static final String DIALOG = "app-batch-popup dialog.popup-container";
    private static final String CONFIRM_SUBMIT_BTN = "Submit To At-Fault";
    private static final String BACK_TO_FAST_TRACK_BTN = "Back To Fast Track";

    public FastTrackClaimsListPage(Page page) {
        super(page);
    }

    public boolean isLoaded() {
        return currentUrl().contains(Routes.FAST_TRACK_CLAIMS_LIST);
    }

    public void refresh() {
        reloadAndWaitForNetworkIdle();
    }

    /** Waits for the first row — the table renders a moment after navigation. */
    public List<String> getClaimNumbers() {
        locator(CLAIM_NUMBER_LINK).first().waitFor(new Locator.WaitForOptions().setTimeout(15000));
        return locator(CLAIM_NUMBER_LINK).allInnerTexts().stream().map(String::trim).toList();
    }

    /** Claim Number column → Status column, in table order. */
    public Map<String, String> getStatusByClaimNumber() {
        Locator rows = locator(BODY_ROWS);
        rows.first().locator("td").first().waitFor(new Locator.WaitForOptions().setTimeout(15000));
        Map<String, String> statuses = new LinkedHashMap<>();
        for (int i = 0; i < rows.count(); i++) {
            Locator cells = rows.nth(i).locator("td");
            statuses.put(cells.nth(CLAIM_NUMBER_COLUMN).innerText().trim(), cells.last().innerText().trim());
        }
        return statuses;
    }

    /** Claim Number column → Serial Number column (claimant side, after submit). */
    public Map<String, String> getSerialByClaimNumber() {
        Locator rows = locator(BODY_ROWS);
        rows.first().locator("td").first().waitFor(new Locator.WaitForOptions().setTimeout(15000));
        Map<String, String> serials = new LinkedHashMap<>();
        for (int i = 0; i < rows.count(); i++) {
            Locator cells = rows.nth(i).locator("td");
            serials.put(cells.nth(CLAIM_NUMBER_COLUMN).innerText().trim(), cells.first().innerText().trim());
        }
        return serials;
    }

    public List<String> getStatuses() {
        return List.copyOf(getStatusByClaimNumber().values());
    }

    /**
     * Filters by the claim number first, so a claim beyond the first page is found:
     * inside a batch the search box matches the claimant claim number only (not
     * accident or plate number), in the browser (verified live on stage).
     */
    @Step("Open claim '{0}'")
    public FastTrackClaimDetailsPage openClaim(String claimNumber) {
        locator(BODY_ROWS).first().locator("td").first().waitFor(new Locator.WaitForOptions().setTimeout(15000));
        page.getByPlaceholder(SEARCH_PLACEHOLDER).fill(claimNumber);
        Locator link = locator(CLAIM_NUMBER_LINK)
                .filter(new Locator.FilterOptions().setHasText(claimNumber))
                .first();
        link.click();
        page.waitForURL(url -> url.contains(Routes.FAST_TRACK_CLAIM_DETAILS),
                new Page.WaitForURLOptions().setTimeout(15000));
        return new FastTrackClaimDetailsPage(page);
    }

    public int getClaimCount() {
        Locator rows = locator(BODY_ROWS);
        rows.first().locator("td").first().waitFor(new Locator.WaitForOptions().setTimeout(15000));
        return rows.count();
    }

    /**
     * Opens the claim in row {@code index} (0-based). Needed on the at-fault side,
     * where the Claim Number column is blank until its own numbers are added: the
     * row's a.table-link is still there but empty, so it has no size and can't be
     * clicked (verified live). Navigates to its href instead.
     */
    @Step("Open claim in row {0}")
    public FastTrackClaimDetailsPage openClaimAt(int index) {
        Locator row = locator(BODY_ROWS).nth(index);
        row.locator("td").first().waitFor(new Locator.WaitForOptions().setTimeout(15000));
        String href = row.locator("a.table-link").first().getAttribute("href");
        page.navigate(java.net.URI.create(currentUrl()).resolve(href).toString());
        page.waitForURL(url -> url.contains(Routes.FAST_TRACK_CLAIM_DETAILS),
                new Page.WaitForURLOptions().setTimeout(15000));
        return new FastTrackClaimDetailsPage(page);
    }

    /**
     * Opens the Batch Summary dialog and returns its text, e.g. "2 Complete Claims
     * 0 Incomplete Claims" and the "this batch will be locked" warning. Nothing is
     * sent until {@link #confirmSubmitToAtFault()}.
     */
    @Step("Click 'Submit to At-Fault'")
    public String openSubmitToAtFaultDialog() {
        // Exact: the dialog's "Submit To At-Fault" differs only in case.
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(SUBMIT_TO_AT_FAULT_BTN).setExact(true)).click();
        Locator dialog = locator(DIALOG);
        dialog.waitFor(new Locator.WaitForOptions().setTimeout(15000));
        return dialog.innerText();
    }

    /**
     * Locks the batch and notifies the at-fault insurer. The dialog stays open
     * and switches to "Batch Submitted Successfully!" / "RC-… Has Been Sent To
     * &lt;entity&gt; For Review. The Batch Is Now Locked." (verified live); returns
     * that text, then closes it via "Back To Fast Track".
     */
    @Step("Confirm 'Submit To At-Fault'")
    public String confirmSubmitToAtFault() {
        Locator dialog = locator(DIALOG);
        dialog.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(CONFIRM_SUBMIT_BTN).setExact(true))
                .click();
        dialog.getByText("Batch Submitted Successfully").waitFor(new Locator.WaitForOptions().setTimeout(20000));
        String confirmation = dialog.innerText();
        dialog.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(BACK_TO_FAST_TRACK_BTN)).click();
        return confirmation;
    }

    public boolean isProceedToSettlementVisible() {
        try {
            getLocatorByRole(AriaRole.BUTTON, PROCEED_TO_SETTLEMENT_BTN)
                    .waitFor(new Locator.WaitForOptions().setTimeout(15000));
            return true;
        } catch (com.microsoft.playwright.PlaywrightException e) {
            log.debug("'Proceed to Settlement' did not show in time: {}", e.getMessage());
            return false;
        }
    }

    @Step("Click 'Proceed to Settlement'")
    public BulkSettlementPage clickProceedToSettlement() {
        getLocatorByRole(AriaRole.BUTTON, PROCEED_TO_SETTLEMENT_BTN).click();
        page.waitForURL(url -> url.contains(Routes.BULK_SETTLEMENT),
                new Page.WaitForURLOptions().setTimeout(15000));
        return new BulkSettlementPage(page);
    }
}
