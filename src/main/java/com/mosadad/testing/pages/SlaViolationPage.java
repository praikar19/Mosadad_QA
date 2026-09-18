package com.mosadad.testing.pages;

import com.mosadad.testing.constants.Routes;
import com.microsoft.playwright.Page;

/**
 * /entity-portal/sla-violation — Dashboard module, reached via
 * RecoveryClaimsHubPage.openSlaViolation(). URL verified live 2026-08-30
 * (RecoveryClaimsNavigationTest); page content itself not yet explored —
 * only isLoaded() is backed by anything real so far.
 */
public class SlaViolationPage extends BasePage {

    public SlaViolationPage(Page page) {
        super(page);
    }

    public boolean isLoaded() {
        return currentUrl().contains(Routes.SLA_VIOLATION);
    }
}
