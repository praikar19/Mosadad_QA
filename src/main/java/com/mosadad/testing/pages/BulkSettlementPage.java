package com.mosadad.testing.pages;

import com.mosadad.testing.constants.Routes;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;

/**
 * /entity-portal/bulk-settlement — one row per counterpart entity. From the app's
 * code (2026-09-24): "Settle" is enabled when totalPayable &gt; 0 and opens Bulk
 * Settlement Payment; "Request Settlement" is enabled when totalReceivable &gt; 0
 * and shows "Settlement request has been sent successfully." So the at-fault side,
 * which owes, normally only gets Settle.
 */
public class BulkSettlementPage extends BasePage {

    private static final String TOAST_MESSAGE = "#toast-container .toast-message";
    private static final String REQUEST_SETTLEMENT_BTN = "Request Settlement";
    private static final String SETTLE_BTN = "Settle";

    public BulkSettlementPage(Page page) {
        super(page);
    }

    /** Exact path: bulk-settlement-payment shares this prefix. */
    public boolean isLoaded() {
        return java.net.URI.create(currentUrl()).getPath().equals(Routes.BULK_SETTLEMENT);
    }

    private Locator entityRow(String entityName) {
        Locator row = locator("tr").filter(new Locator.FilterOptions().setHasText(entityName)).first();
        row.waitFor(new Locator.WaitForOptions().setTimeout(15000));
        return row;
    }

    private Locator rowButton(String entityName, String name) {
        return entityRow(entityName).getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName(name).setExact(true));
    }

    public boolean isRequestSettlementEnabled(String entityName) {
        return rowButton(entityName, REQUEST_SETTLEMENT_BTN).isEnabled();
    }

    /** Returns the toast text. */
    @Step("Request Settlement for '{0}'")
    public String requestSettlement(String entityName) {
        rowButton(entityName, REQUEST_SETTLEMENT_BTN).click();
        Locator toast = locator(TOAST_MESSAGE).first();
        toast.waitFor(new Locator.WaitForOptions().setTimeout(15000));
        return toast.innerText().trim();
    }

    @Step("Settle with '{0}'")
    public BulkSettlementPaymentPage settle(String entityName) {
        rowButton(entityName, SETTLE_BTN).click();
        page.waitForURL(url -> url.contains(Routes.BULK_SETTLEMENT_PAYMENT),
                new Page.WaitForURLOptions().setTimeout(15000));
        return new BulkSettlementPaymentPage(page);
    }
}
