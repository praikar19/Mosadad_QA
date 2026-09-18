package com.mosadad.testing.utils;

import com.microsoft.playwright.Page;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Reusable Playwright actions that aren't tied to any one Page Object —
 * safe to call from a page object (via BasePage) or directly from a test
 * class that holds its own Page (e.g. WalletTesting's actorA/actorB pages),
 * unlike BasePage's instance methods which only exist on Page Object subclasses.
 */
public final class CommonMethods {

    private static final Logger log = LogManager.getLogger(CommonMethods.class);

    private CommonMethods() {}

    /** Reloads the page and waits for the SPA to reach its default load state before returning. */
    public static void refreshPage(Page page) {
        page.reload();
        page.waitForLoadState();
        log.debug("Refreshed page: {}", page.url());
    }
}
