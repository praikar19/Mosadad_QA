package com.mosadad.testing.pages;

import com.mosadad.testing.constants.Routes;
import com.mosadad.testing.pages.claims.FastTrackClaimsListPage;
import com.mosadad.testing.pages.claims.FastTrackCreateBatchPage;
import com.mosadad.testing.pages.claims.FastTrackReuploadBatchPage;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.List;

/**
 * Fast Track batch list. The at-fault insurer's batches are under the
 * "Payable Claims" tab (a plain span, not a button); each row's ⋮ menu is a
 * Bootstrap dropdown. Don't use the old Fast Track333.xlsx template here —
 * its columns don't match.
 */
public class FastTrackPage extends BasePage {

    private static final String BATCH_LINK = "a.table-link";
    private static final String ROW_ACTIONS_TOGGLE = "button.dropdown-toggle";
    private static final String ROW_STATUS = ".fasttrack-status";
    private static final String SEARCH_PLACEHOLDER = "Accident Number or Plate Number";

    public FastTrackPage(Page page) {
        super(page);
    }

    public boolean isLoaded() {
        return currentUrl().contains(Routes.FAST_TRACK);
    }

    @Step("Click 'Create New Batch'")
    public FastTrackCreateBatchPage clickCreateNewBatch() {
        getLocatorByRole(AriaRole.BUTTON, "Create New Batch").click();
        return new FastTrackCreateBatchPage(page);
    }

    /** The tab is clickable before the list behind it has loaded, so wait for a row. */
    @Step("Open the 'Payable Claims' tab")
    public FastTrackPage openPayableClaimsTab() {
        page.getByText("Payable Claims", new Page.GetByTextOptions().setExact(true)).first().click();
        locator(BATCH_LINK).first().waitFor(new Locator.WaitForOptions().setTimeout(15000));
        return this;
    }

    /**
     * The list is oldest-first, 10 per page, so a new batch is often not on page 1.
     * Filters by the batch code instead: despite its placeholder, the search box
     * matches batch codes, server-side, as you type (verified live on stage).
     */
    private Locator batchRow(String batchCode) {
        Locator row = locator("tr").filter(new Locator.FilterOptions()
                .setHas(page.locator(BATCH_LINK, new Page.LocatorOptions().setHasText(batchCode))));
        locator(BATCH_LINK).first().waitFor(new Locator.WaitForOptions().setTimeout(15000));
        if (row.count() == 0) {
            page.getByPlaceholder(SEARCH_PLACEHOLDER).fill(batchCode);
        }
        try {
            row.waitFor(new Locator.WaitForOptions().setTimeout(15000));
        } catch (com.microsoft.playwright.PlaywrightException e) {
            throw new IllegalStateException("Batch " + batchCode + " not found by searching the Fast Track list", e);
        }
        return row;
    }

    public String getBatchStatus(String batchCode) {
        return batchRow(batchCode).locator(ROW_STATUS).innerText().trim();
    }

    @Step("Open batch '{0}'")
    public FastTrackClaimsListPage openBatch(String batchCode) {
        batchRow(batchCode).locator(BATCH_LINK).click();
        page.waitForURL(url -> url.contains(Routes.FAST_TRACK_CLAIMS_LIST),
                new Page.WaitForURLOptions().setTimeout(15000));
        return new FastTrackClaimsListPage(page);
    }

    /** The actions in a batch row's ⋮ menu; closes the menu again. */
    public List<String> getRowActions(String batchCode) {
        Locator row = batchRow(batchCode);
        row.locator(ROW_ACTIONS_TOGGLE).click();
        Locator items = row.locator(".dropdown-menu .dropdown-item");
        items.first().waitFor(new Locator.WaitForOptions().setTimeout(5000));
        List<String> actions = items.allInnerTexts().stream().map(String::trim).toList();
        page.keyboard().press("Escape");
        return actions;
    }

    /** Fails fast, listing what the menu does offer, when {@code action} isn't in it. */
    private void clickRowAction(String batchCode, String action) {
        Locator row = batchRow(batchCode);
        row.locator(ROW_ACTIONS_TOGGLE).click();
        Locator items = row.locator(".dropdown-menu .dropdown-item");
        items.first().waitFor(new Locator.WaitForOptions().setTimeout(5000));
        Locator item = items.filter(new Locator.FilterOptions().setHasText(action));
        if (item.count() == 0) {
            throw new IllegalStateException("Batch " + batchCode + " has no '" + action + "' action — the ⋮ menu offers "
                    + items.allInnerTexts().stream().map(String::trim).toList());
        }
        item.click();
    }

    /**
     * ⋮ → Download Sheet. The app fetches claims/FastTrack/AtFaultTemplate/{batchId}
     * and builds the file in the browser; it only has rows once the batch is submitted.
     */
    @Step("Download the at-fault sheet for batch '{0}'")
    public byte[] downloadSheet(String batchCode) {
        Download download = page.waitForDownload(() -> clickRowAction(batchCode, "Download Sheet"));
        try {
            return Files.readAllBytes(download.path());
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read the downloaded sheet for batch " + batchCode, e);
        }
    }

    /**
     * The front end only offers this on the Payable tab when the batch status is
     * 2 (ClaimantUploadAttachment) and the user has update:claims. On stage
     * (2026-09-24) a submitted batch comes back with status 5, so the item never shows.
     */
    @Step("Open 'Upload Excel' for batch '{0}'")
    public FastTrackReuploadBatchPage openUploadExcel(String batchCode) {
        clickRowAction(batchCode, "Upload Excel");
        return new FastTrackReuploadBatchPage(page);
    }
}
