package com.mosadad.testing.pages;

import com.microsoft.playwright.Page;

public class DashboardPage extends BasePage {

    private static final String SIDEBAR_HOME_LINK = "a:has-text('Home')";
    private static final String SIDEBAR_RECOVERY_CLAIMS_LINK = "a:has-text('Recovery Claims')";
    private static final String SIDEBAR_COMPANY_DETAILS_LINK = "a:has-text('Company Details')";

    private static final String TOP_SEARCH_INPUT = "input.search-input";

    private static final String IN_PROCESS_CLAIMS_HEADING = "text=In-Process Claims";

    public DashboardPage(Page page) {
        super(page);
    }

    public boolean isLoaded() {
        try {
            waitVisible(IN_PROCESS_CLAIMS_HEADING);
        } catch (com.microsoft.playwright.PlaywrightException timeout) {
            log.debug("Dashboard content did not become visible in time: {}", timeout.getMessage());
            return false;
        }
        return currentUrl().contains("/entity-landing/dashboard");
    }

    public boolean isEntityModulesSidebarVisible() {
        return isVisible(SIDEBAR_HOME_LINK)
                && isVisible(SIDEBAR_RECOVERY_CLAIMS_LINK)
                && isVisible(SIDEBAR_COMPANY_DETAILS_LINK);
    }

    public RecoveryClaimsHubPage openRecoveryClaims() {
        click(SIDEBAR_RECOVERY_CLAIMS_LINK);
        return new RecoveryClaimsHubPage(page);
    }

    public void search(String query) {
        fill(TOP_SEARCH_INPUT, query);
        page.keyboard().press("Enter");
    }

}
