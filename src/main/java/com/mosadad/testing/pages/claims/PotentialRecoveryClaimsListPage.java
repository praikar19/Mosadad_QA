package com.mosadad.testing.pages.claims;

import com.mosadad.testing.constants.Routes;
import com.microsoft.playwright.Page;
import com.mosadad.testing.pages.BasePage;

/**
 * /entity-portal/potential-recovery-claims — Recovery Claim Records module,
 * reached via RecoveryClaimsHubPage.openPotentialRecoveryClaimsListPage().
 * URL verified live 2026-08-30 (RecoveryClaimsNavigationTest); page content
 * itself not yet explored — only isLoaded() is backed by anything real so far.
 */
public class PotentialRecoveryClaimsListPage extends BasePage {

    public PotentialRecoveryClaimsListPage(Page page) {
        super(page);
    }

    private static final String EXPORT_BTN = "img:text-is('Export as')";
    private static final String ADVANCED_SEARCH_BTN = "button:text-is('Advanced Search')";
    private static final String CREATE_NEW_RECOVERY_CLAIM_BTN = "button:text-is('Create New Recovery Claim')";


    public boolean isLoaded() {
        return currentUrl().contains(Routes.POTENTIAL_RECOVERY_CLAIMS);
    }

    public CreateManualRecoveryClaimPage clickCreateNewRecoveryClaimBtn(){
        click(CREATE_NEW_RECOVERY_CLAIM_BTN);
        return new CreateManualRecoveryClaimPage(page);
    }



}
