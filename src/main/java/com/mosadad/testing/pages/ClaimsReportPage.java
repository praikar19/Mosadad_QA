package com.mosadad.testing.pages;

import com.mosadad.testing.constants.Routes;
import com.microsoft.playwright.Page;

/**
 * /entity-portal/financial-statement-report — Dashboard module, reached via
 * RecoveryClaimsHubPage.openClaimsReport(). URL verified live 2026-08-30
 * (RecoveryClaimsNavigationTest); page content itself not yet explored —
 * only isLoaded() is backed by anything real so far.
 */
public class ClaimsReportPage extends BasePage {

    public ClaimsReportPage(Page page) {
        super(page);
    }

    public boolean isLoaded() {
        return currentUrl().contains(Routes.CLAIMS_REPORT);
    }
}
