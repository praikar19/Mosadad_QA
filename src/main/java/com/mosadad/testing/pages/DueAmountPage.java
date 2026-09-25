package com.mosadad.testing.pages;

import com.mosadad.testing.constants.Routes;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;

/**
 * /entity-portal/due-amount — the at-fault's payable credit notes, one row per
 * counterpart entity (Name, Number Of Claims, Credit Note Amount, Settle).
 * Settle opens Credit Note Bulk Details. Verified live on stage 2026-09-24.
 */
public class DueAmountPage extends BasePage {

    public DueAmountPage(Page page) {
        super(page);
    }

    public boolean isLoaded() {
        return currentUrl().contains(Routes.DUE_AMOUNT);
    }

    @Step("Settle credit notes with '{0}'")
    public CreditNoteBulkPage settle(String entityName) {
        Locator row = locator("tr").filter(new Locator.FilterOptions().setHasText(entityName)).first();
        row.waitFor(new Locator.WaitForOptions().setTimeout(15000));
        row.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Settle").setExact(true)).click();
        page.waitForURL(url -> url.contains(Routes.CREDIT_NOTE_BULK), new Page.WaitForURLOptions().setTimeout(15000));
        return new CreditNoteBulkPage(page);
    }
}
