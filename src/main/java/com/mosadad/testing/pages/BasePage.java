package com.mosadad.testing.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Base class for all Page Objects.
 *
 * Design decision — locators as plain selector strings, resolved lazily via
 * page.locator(selector):
 *   The sibling Appium framework (ShopTestApp-Java-Mobile-Testing) avoids
 *   Page Factory because @FindBy resolves elements eagerly and goes stale
 *   across screen transitions. Playwright's Locator API already solves this
 *   the same way by design — a Locator is a lazy query, re-resolved and
 *   auto-waited on every action — so there is nothing extra to opt out of.
 *   Subclasses still store selectors as private static final String
 *   constants for the same reason: one place to update when the UI changes.
 */
public abstract class BasePage {

    protected final Page page;
    protected final Logger log = LogManager.getLogger(getClass());

    protected BasePage(Page page) {
        this.page = page;
    }

    protected Locator locator(String selector) {
        return page.locator(selector);
    }

    protected Locator getLocatorByText(String tagName, String text){
        return page.locator(tagName).filter(new Locator.FilterOptions().setHasText(text));
    }

    protected Locator getLocatorByText(String text){
        return page.getByText(text);
    }

    protected Locator getLocatorByLabel(String text){
        return page.getByLabel(text);
    }

    protected Locator getLocatorByRole(AriaRole role, String text){
        return page.getByRole(role, new Page.GetByRoleOptions().setName(text));
    }

    protected Locator getLocatorByRole(String text){
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(text));
    }

    /** By real HTML id attribute (e.g. "email" for #email) — NOT a data-testid; use getLocatorByTestId() for that. */
    protected Locator getLocatorById(String id){
        return page.locator("#" + id);
    }

    /** By data-testid attribute — only useful if the app actually sets one; this app hasn't shown any so far. */
    protected Locator getLocatorByTestId(String testId){
        return page.getByTestId(testId);
    }

    protected void fill(String selector, String text) {
        page.locator(selector).fill(text);
    }

    protected void click(String selector) {
        page.locator(selector).click();
    }

    protected String getText(String selector) {
        return page.locator(selector).innerText();
    }

    protected boolean isVisible(String selector) {
        return page.locator(selector).isVisible();
    }

    protected void waitVisible(String selector) {
        page.locator(selector).waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    /**
     * Explicitly scrolls the element into view. Most Playwright actions
     * (click, fill, ...) already auto-scroll the target into view as part
     * of their own actionability checks, so this is only needed when a
     * caller wants the scroll to happen as its own separate step — e.g.
     * right before a click on a button far down a long page, to rule out
     * scroll-position as the reason an action isn't finding the element
     * actionable.
     */
    protected void scrollIntoView(String selector) {
        page.locator(selector).scrollIntoViewIfNeeded();
    }

    protected String currentUrl() {
        return page.url();
    }

    /**
     * Reload and wait for the page to settle — needed after any action
     * taken by the OTHER actor's browser session (accept, quotation
     * approval, invoice acceptance, etc.), since this page's own Angular
     * app has no live-push mechanism for that and will keep showing stale
     * state until reloaded. Confirmed live 2026-09-04: without this, the
     * claimant's already-open claim page didn't reflect the at-fault
     * actor's Accept from a separate browser context. Network-idle is
     * best-effort, not required — some pages keep a long-poll/websocket
     * connection open that never goes idle, so a timeout here is expected
     * and not a failure.
     */
    protected void reloadAndWaitForNetworkIdle() {
        page.reload();
        try {
            page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE,
                    new Page.WaitForLoadStateOptions().setTimeout(15000));
        } catch (com.microsoft.playwright.PlaywrightException e) {
            log.debug("Page did not reach network-idle after reload in time: {}", e.getMessage());
        }
    }
}
