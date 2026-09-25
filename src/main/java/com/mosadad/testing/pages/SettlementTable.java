package com.mosadad.testing.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The claim table and "Total Selected Claims" panel shared by Bulk Settlement
 * Payment and Credit Note Bulk Details (the app's app-dynamic-table). From the
 * app's code and a live check on stage (2026-09-24): the list loads once, nothing
 * is pre-selected, ticks persist, and the panel ("Number Of Claims Selected",
 * "Total Amount") updates a moment after each tick. Rows can be disabled
 * (Payment Pending), with the reason in the row's info icon title. Claims reach
 * these lists from the backend, so a just-accepted claim may need a reload.
 */
final class SettlementTable {

    private static final String BODY_ROWS = "table tbody tr";
    private static final String ROW_CHECKBOX = "input[type='checkbox']";
    private static final String ROW_INFO_ICON = "img.info-icon";
    private static final int LOAD_ATTEMPTS = 6;
    private static final Pattern SUMMARY = Pattern.compile(
            "Number Of Claims Selected\\s*(\\d+)[\\s\\S]*?Total Amount\\s*([\\d,.]+)");

    private SettlementTable() {}

    /** Ticks exactly the given claims (by serial number), unticks every other row, and checks the result. */
    static void selectOnly(Page page, List<String> serialNumbers, String pageName) {
        Locator rows = page.locator(BODY_ROWS);
        waitForRows(page, rows, serialNumbers, pageName);
        for (String serialNumber : serialNumbers) {
            Locator row = rowOf(rows, serialNumber);
            Locator checkbox = row.locator(ROW_CHECKBOX).first();
            if (checkbox.isDisabled()) {
                Locator info = row.locator(ROW_INFO_ICON);
                String reason = info.count() > 0 ? info.first().getAttribute("title") : "no reason shown";
                throw new IllegalStateException("Claim " + serialNumber + " can't be selected on " + pageName + ": " + reason);
            }
            checkbox.check();
        }
        for (int i = 0; i < rows.count(); i++) {
            Locator row = rows.nth(i);
            Locator checkbox = row.locator(ROW_CHECKBOX);
            if (!serialNumbers.contains(serialOf(row)) && checkbox.count() > 0 && checkbox.first().isChecked()) {
                checkbox.first().uncheck();
            }
        }
        List<String> ticked = tickedSerials(rows);
        if (ticked.size() != serialNumbers.size() || !ticked.containsAll(serialNumbers)) {
            throw new IllegalStateException("Expected exactly " + serialNumbers + " ticked on " + pageName + ", got " + ticked);
        }
    }

    /**
     * Waits (up to 10s) for the panel to show the expected count and total, since it
     * lags the ticks; returns what it shows as "count / total", e.g. "2 / 7,200.00".
     */
    static String waitForSummary(Page page, int expectedCount, String expectedTotal) {
        String expected = expectedCount + " / " + expectedTotal;
        String shown = readSummary(page);
        long deadline = System.currentTimeMillis() + 10000;
        while (!shown.equals(expected) && System.currentTimeMillis() < deadline) {
            page.waitForTimeout(250);
            shown = readSummary(page);
        }
        return shown;
    }

    private static String readSummary(Page page) {
        Matcher m = SUMMARY.matcher(page.locator("body").innerText());
        return m.find() ? m.group(1) + " / " + m.group(2) : "";
    }

    /** The list loads once, so reload until every claim is listed. */
    private static void waitForRows(Page page, Locator rows, List<String> serialNumbers, String pageName) {
        List<String> shown = List.of();
        for (int attempt = 1; attempt <= LOAD_ATTEMPTS; attempt++) {
            if (attempt > 1) {
                page.reload();
            }
            try {
                page.waitForLoadState(LoadState.NETWORKIDLE, new Page.WaitForLoadStateOptions().setTimeout(10000));
            } catch (com.microsoft.playwright.PlaywrightException ignored) {
                // The app keeps a notification socket open; the row check below decides.
            }
            rows.first().waitFor(new Locator.WaitForOptions().setTimeout(15000));
            shown = new ArrayList<>();
            for (int i = 0; i < rows.count(); i++) {
                shown.add(serialOf(rows.nth(i)));
            }
            if (shown.containsAll(serialNumbers)) {
                return;
            }
        }
        throw new IllegalStateException("Claims " + serialNumbers + " not all listed on " + pageName
                + " after " + LOAD_ATTEMPTS + " loads; rows shown: " + shown);
    }

    private static Locator rowOf(Locator rows, String serialNumber) {
        return rows.filter(new Locator.FilterOptions().setHasText(serialNumber)).first();
    }

    /** Serial Number is the first text in each row (the checkbox and info icon have none). */
    private static String serialOf(Locator row) {
        String text = row.innerText().trim();
        return text.isEmpty() ? "" : text.split("\\s+")[0];
    }

    private static List<String> tickedSerials(Locator rows) {
        List<String> ticked = new ArrayList<>();
        for (int i = 0; i < rows.count(); i++) {
            Locator row = rows.nth(i);
            Locator checkbox = row.locator(ROW_CHECKBOX);
            if (checkbox.count() > 0 && checkbox.first().isChecked()) {
                ticked.add(serialOf(row));
            }
        }
        return ticked;
    }
}
