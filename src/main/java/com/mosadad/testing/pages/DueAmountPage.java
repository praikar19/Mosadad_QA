package com.mosadad.testing.pages;

import com.mosadad.testing.constants.Routes;
import com.microsoft.playwright.Page;

/**
 * /entity-portal/due-amount — Financial module, reached via
 * RecoveryClaimsHubPage.openDueAmount(). URL verified live 2026-08-30
 * (RecoveryClaimsNavigationTest); page content itself not yet explored —
 * only isLoaded() is backed by anything real so far.
 */
public class DueAmountPage extends BasePage {

    public DueAmountPage(Page page) {
        super(page);
    }

    public boolean isLoaded() {
        return currentUrl().contains(Routes.DUE_AMOUNT);
    }
}
