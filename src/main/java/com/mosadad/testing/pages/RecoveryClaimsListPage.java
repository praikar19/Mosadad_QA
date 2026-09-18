package com.mosadad.testing.pages;

import com.mosadad.testing.constants.Routes;
import com.microsoft.playwright.Page;

/**
 * /entity-portal/my-claims — Recovery Claim Records module, reached via
 * RecoveryClaimsHubPage.openRecoveryClaimsList(). URL verified live
 * 2026-08-30 (RecoveryClaimsNavigationTest); page content itself not yet
 * explored — only isLoaded() is backed by anything real so far. Likely
 * entry point for the real "Create Claim" flow (Stage 1) once explored —
 * see ClaimRegistrationPage.
 */
public class RecoveryClaimsListPage extends BasePage {

    public RecoveryClaimsListPage(Page page) {
        super(page);
    }

    public boolean isLoaded() {
        return currentUrl().contains(Routes.RECOVERY_CLAIMS_LIST);
    }
}
