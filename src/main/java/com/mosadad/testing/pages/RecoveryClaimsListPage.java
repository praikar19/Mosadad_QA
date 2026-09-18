package com.mosadad.testing.pages;

import com.mosadad.testing.constants.Routes;
import com.microsoft.playwright.Page;

/**
 * /entity-portal/my-claims — Recovery Claim Records module, reached via
 * RecoveryClaimsHubPage.openRecoveryClaimsList(). URL verified live
 * 2026-08-30 (RecoveryClaimsNavigationTest); page content itself not yet
 * explored — only isLoaded() is backed by anything real so far. The real
 * "Create Claim" flow (Stage 1, Manual Entry) is reached from here via
 * PotentialRecoveryClaimsListPage — see CreateManualRecoveryClaimPage /
 * CreateManualClaimPage.
 */
public class RecoveryClaimsListPage extends BasePage {

    public RecoveryClaimsListPage(Page page) {
        super(page);
    }

    public boolean isLoaded() {
        return currentUrl().contains(Routes.RECOVERY_CLAIMS_LIST);
    }
}
