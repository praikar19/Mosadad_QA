package com.mosadad.testing.pages;

import com.mosadad.testing.constants.Routes;
import com.microsoft.playwright.Page;

/**
 * /entity-portal/bulk-settlement — Financial module, reached via
 * RecoveryClaimsHubPage.openBulkSettlement(). URL verified live 2026-08-30
 * (RecoveryClaimsNavigationTest); page content itself not yet explored —
 * only isLoaded() is backed by anything real so far.
 */
public class BulkSettlementPage extends BasePage {

    public BulkSettlementPage(Page page) {
        super(page);
    }

    public boolean isLoaded() {
        return currentUrl().contains(Routes.BULK_SETTLEMENT);
    }
}
