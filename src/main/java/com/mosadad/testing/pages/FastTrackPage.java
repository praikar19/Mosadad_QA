package com.mosadad.testing.pages;

import com.mosadad.testing.constants.Routes;
import com.microsoft.playwright.Page;

/**
 * /entity-portal/fasttrack-requests — Recovery Claim Records module, reached
 * via RecoveryClaimsHubPage.openFastTrack(). URL verified live 2026-08-30
 * (RecoveryClaimsNavigationTest); page content itself not yet explored —
 * only isLoaded() is backed by anything real so far. Likely where the bulk
 * Excel upload (ExcelUtils / Fast Track333.xlsx) gets consumed once explored
 * — see MOSADAD_DOMAIN.md §Fast Track.
 */
public class FastTrackPage extends BasePage {

    public FastTrackPage(Page page) {
        super(page);
    }

    public boolean isLoaded() {
        return currentUrl().contains(Routes.FAST_TRACK);
    }
}
