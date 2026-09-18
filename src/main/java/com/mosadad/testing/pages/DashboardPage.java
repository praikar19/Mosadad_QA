package com.mosadad.testing.pages;

import com.mosadad.testing.constants.Routes;
import com.mosadad.testing.pages.claims.OpenRecoveryClaimsPage;
import com.mosadad.testing.pages.claims.WalletPage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * /entity-landing/dashboard — the post-login landing page for an entity user
 * (verified live 2026-08-30, logged in as Claimant Insurer).
 *
 * Left sidebar is the "Entity Modules" nav, shared across every
 * /entity-landing/* and /entity-portal/* screen: Home, Recovery Claims,
 * Company Details. Top bar has global search, notifications bell, and the
 * profile menu.
 */
public class DashboardPage extends BasePage {

    // NOTE: link text itself was verified live; the exact wrapping container
    // (class/tag of the sidebar <nav>) was not captured during exploration.
    // If "Home" ever collides with text elsewhere on the page, scope these
    // further once the sidebar's real container selector is confirmed.
    private static final String SIDEBAR_HOME_LINK = "a:has-text('Home')";
    private static final String SIDEBAR_RECOVERY_CLAIMS_LINK = "a:has-text('Recovery Claims')";
    private static final String SIDEBAR_COMPANY_DETAILS_LINK = "a:has-text('Company Details')";

    private static final String TOP_SEARCH_INPUT = "input.search-input";
    private static final String TOP_NOTIFICATIONS_BELL = "button.bell-button";
    private static final String TOP_PROFILE_MENU_TRIGGER = "button.profile-trigger";

    private static final String IN_PROCESS_CLAIMS_HEADING = "text=In-Process Claims";
    private static final String TOTAL_RECOVERABLE_VALUE_CARD = "text=Total Recoverable Value";
    private static final String TOTAL_PAYABLE_VALUE_CARD = "text=Total Payable Value";
    private static final String MY_WALLETS_BUTTON = "button:has-text('My Wallets')";

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

    public void openCompanyDetails() {
        click(SIDEBAR_COMPANY_DETAILS_LINK);
    }

    public void openNotifications() {
        click(TOP_NOTIFICATIONS_BELL);
    }

    public void search(String query) {
        fill(TOP_SEARCH_INPUT, query);
        page.keyboard().press("Enter");
    }

    private static final String SEARCH_RESULT_ROW = ".search-result-row";

    /**
     * Typing into the top search box (no Enter needed) opens a
     * ".search-results-dropdown" with one ".search-result-row" per match,
     * each showing the claim's serial number, insurer name, and status
     * (confirmed live 2026-09-04). Pressing Enter alone does NOT navigate
     * anywhere — the row itself has to be clicked. Filters by exact query
     * text (the serial number) in case a search ever returns more than one
     * row.
     */
    public OpenRecoveryClaimsPage searchClaim(String query) {
        fill(TOP_SEARCH_INPUT, query);
        page.locator(SEARCH_RESULT_ROW).filter(new Locator.FilterOptions().setHasText(query)).first().click();
        page.waitForURL(url -> url.contains(Routes.OPEN_RECOVERY_CLAIM),
                new Page.WaitForURLOptions().setTimeout(15000));
        return new OpenRecoveryClaimsPage(page);
    }

    public void openProfileMenu() {
        click(TOP_PROFILE_MENU_TRIGGER);
    }

    /**
     * "My Wallets" button — redirects the same tab cross-domain to the ATB
     * Pay wallet portal (rak.atbpay.me), a third-party system, not a
     * Mosadad screen, via what looks like a multi-hop SSO handoff (the
     * intermediate hop's URL also contains "atbpay", so waiting on that
     * alone can return before the final page has settled). Also waits for
     * network-idle so WalletPage isn't handed a page mid-redirect.
     */
    public WalletPage openWallet() {
        click(MY_WALLETS_BUTTON);
        page.waitForURL(url -> url.contains("atbpay"), new Page.WaitForURLOptions().setTimeout(15000));
        try {
            page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE,
                    new Page.WaitForLoadStateOptions().setTimeout(10000));
        } catch (com.microsoft.playwright.PlaywrightException e) {
            log.debug("Wallet portal did not reach network-idle in time: {}", e.getMessage());
        }
        return new WalletPage(page);
    }
}
