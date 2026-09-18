package com.mosadad.testing.pages.claims;

import com.mosadad.testing.constants.Routes;
import com.mosadad.testing.pages.BasePage;
import com.mosadad.testing.pages.DashboardPage;
import com.microsoft.playwright.Page;

/**
 * ATB Pay Wallet Portal (rak.atbpay.me) — verified live 2026-09-02 as the
 * Claimant Insurer (Dubai stage account). Reached via
 * DashboardPage.openWallet() ("My Wallets" button), which redirects the
 * same browser tab cross-domain, same-tab (not a popup).
 *
 * IMPORTANT: this is a THIRD-PARTY system, not part of Mosadad's own front
 * end — see MOSADAD_DOMAIN.md §Wallet. It has its own SSO/auth: a Bearer
 * JWT stored in this page's own localStorage under "access-token" (not the
 * Mosadad session), used to call WalletApiClient directly.
 */
public class WalletPage extends BasePage {

    // Labels ("Balance", "Wallet Number") sit in their own leaf element with
    // the value as the very next sibling — Playwright's :text-is() + CSS
    // adjacent-sibling combinator finds it without depending on the
    // Angular-generated class names (text-light mb-1) that are reused
    // everywhere on this page and not unique to any one field.
    private static final String BALANCE_VALUE = "h6:text-is('Balance') + p";
    private static final String WALLET_NUMBER_VALUE = "h6:text-is('Wallet Number') + p";
    private static final String VIRTUAL_IBAN_VALUE = "span:text-is('Virtual IBAN number :') + span";
    private static final String VIRTUAL_ACCOUNT_NUMBER_VALUE = "span:text-is('Virtual account number :') + span";
    private static final String TRADE_LICENSE_NUMBER_VALUE = "span:text-is('Trade license number :') + span";
    private static final String OWNER_NAME_VALUE = "span:text-is('Owner Name :') + span";
    private static final String PAYER_NAME_VALUE = "span:text-is('Payer Name :') + span";

    // The "Wallet Number" <p> exists in the DOM as soon as the Angular shell
    // renders, but its text is interpolated in asynchronously (after the
    // portal's own data call resolves) — a bare element-visibility wait
    // isn't enough, it can pass while the text is still empty. Waits for
    // the actual text to be non-empty instead.
    private static final String WALLET_DATA_READY_JS =
            "() => { const h6 = Array.from(document.querySelectorAll('h6'))" +
            ".find(e => e.innerText.trim() === 'Wallet Number');" +
            " return !!(h6 && h6.nextElementSibling && h6.nextElementSibling.innerText.trim().length > 0); }";

    public WalletPage(Page page) {
        super(page);
    }

    /**
     * Retries once: the SSO handoff into this portal is multi-hop, so the
     * first attempt can land mid-redirect and have its execution context
     * torn out from under waitForFunction (thrown as a PlaywrightException,
     * same as a real timeout) — by the second attempt the redirect chain
     * has settled.
     */
    public boolean isLoaded() {
        for (int attempt = 1; attempt <= 2; attempt++) {
            if (!currentUrl().contains("atbpay.me")) {
                return false;
            }
            try {
                // 3-arg form required: waitForFunction(expression, arg, options) —
                // the 2-arg overload takes (expression, arg), so passing options as
                // the second argument silently mismatches to "arg" instead.
                page.waitForFunction(WALLET_DATA_READY_JS, null, new Page.WaitForFunctionOptions().setTimeout(15000));
                return true;
            } catch (com.microsoft.playwright.PlaywrightException e) {
                log.debug("Wallet data readiness check failed (attempt {}): {}", attempt, e.getMessage());
            }
        }
        return false;
    }

    /** Raw text as shown, e.g. "930,382.36 AED". */
    public String getBalanceText() {
        return getText(BALANCE_VALUE).trim();
    }

    /** Balance parsed to a number (comma/currency-suffix stripped) for comparing against the API's raw double. */
    public double getBalanceAmount() {
        return Double.parseDouble(getBalanceText().replaceAll("[^0-9.]", ""));
    }

    /** Raw text as shown, e.g. "#103516998". */
    public String getWalletNumberText() {
        return getText(WALLET_NUMBER_VALUE).trim();
    }

    /** Wallet number parsed to a number ("#" stripped) for comparing against the API's numeric walletNumber. */
    public long getWalletNumber() {
        return Long.parseLong(getWalletNumberText().replace("#", "").trim());
    }

    public String getVirtualIban()          { return getText(VIRTUAL_IBAN_VALUE).trim(); }
    public String getVirtualAccountNumber() { return getText(VIRTUAL_ACCOUNT_NUMBER_VALUE).trim(); }
    public String getTradeLicenseNumber()   { return getText(TRADE_LICENSE_NUMBER_VALUE).trim(); }
    public String getOwnerName()            { return getText(OWNER_NAME_VALUE).trim(); }
    public String getPayerName()            { return getText(PAYER_NAME_VALUE).trim(); }

    /**
     * The wallet portal's own Bearer JWT (separate from the Mosadad
     * session) — pass this straight to WalletApiClient to query the same
     * data shown on this page.
     */
    public String getAccessToken() {
        return (String) page.evaluate("() => localStorage.getItem('access-token')");
    }

    /**
     * Browser-Back navigation out of this third-party portal back to the
     * Mosadad dashboard.
     *
     * KNOWN NOT WORKING under Playwright as of 2026-09-02 — do not rely on
     * this yet. Manually (real Chrome, physical Back button) it takes
     * exactly 2 presses: press 1 lands on an intermediate same-origin SSO
     * return route (.../wallets/callback, still Mosadad's domain but not the
     * dashboard), press 2 reaches /entity-landing/dashboard — confirmed live
     * multiple times. That's why the loop below checks the actual dashboard
     * path rather than just "left the third-party domain" (which would stop
     * one press early). The loop shape is correct; what's broken is
     * page.goBack() itself in this context: it consistently returns null and
     * the URL only toggles between two variants of the wallet page's own
     * default-filter query string, never progressing to Mosadad — reproduced
     * 8+ times, with load-state waits, a 1s settle delay between presses,
     * and both Playwright's normal ephemeral BrowserContext and an explicit
     * launchPersistentContext() (real on-disk Chrome profile, to rule out
     * ephemeral-vs-persistent as the variable — it wasn't). Root cause is
     * still unknown; likely something about how this portal's Angular router
     * manages history that behaves differently under CDP-driven navigation
     * than a genuine browser-chrome Back button. Revisit before trusting
     * this method — see conversation history 2026-09-02 for the full
     * diagnostic trail before re-investigating from scratch.
     */
    public DashboardPage goBackToDashboard() {
        final int maxBackPresses = 5;
        for (int attempt = 1; attempt <= maxBackPresses; attempt++) {
            if (currentUrl().contains(Routes.DASHBOARD)) {
                break;
            }
            page.goBack(new Page.GoBackOptions().setTimeout(10000));
            try {
                page.waitForLoadState();
            } catch (com.microsoft.playwright.PlaywrightException e) {
                log.debug("Load state wait after Back press {} did not settle cleanly: {}", attempt, e.getMessage());
            }
            log.debug("goBackToDashboard() attempt {} — url now {}", attempt, currentUrl());
        }
        if (!currentUrl().contains(Routes.DASHBOARD)) {
            throw new IllegalStateException(
                    "Still not on the dashboard after " + maxBackPresses + " Back presses: " + currentUrl());
        }
        return new DashboardPage(page);
    }

    public DashboardPage goBackToDashboardJS(){
        int stepsBack = -3;

        page.evaluate("steps => window.history.go(steps)", stepsBack);
        try {
            page.waitForLoadState();
        } catch (com.microsoft.playwright.PlaywrightException e) {
            log.debug("Load state wait after Back press {} did not settle cleanly: {}", stepsBack, e.getMessage());
        }
        log.debug("goBackToDashboard() attempt {} — url now {}", stepsBack, currentUrl());
        if (!currentUrl().contains(Routes.DASHBOARD)) {
            throw new IllegalStateException(
                    "Still not on the dashboard after " + stepsBack + " Back presses: " + currentUrl());
        }
        return new DashboardPage(page);
    }
}
