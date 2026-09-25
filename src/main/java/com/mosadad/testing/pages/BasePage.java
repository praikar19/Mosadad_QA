package com.mosadad.testing.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class BasePage {

    protected final Page page;
    protected final Logger log = LogManager.getLogger(getClass());

    protected BasePage(Page page) {
        this.page = page;
    }

    protected Locator locator(String selector) {
        return page.locator(selector);
    }

    protected Locator getLocatorByRole(AriaRole role, String text){
        return page.getByRole(role, new Page.GetByRoleOptions().setName(text));
    }

    protected Locator getLocatorByRole(String text){
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(text));
    }

    @Step("Fill \"{1}\" into {0}")
    protected void fill(String selector, String text) {
        page.locator(selector).fill(text);
    }

    @Step("Click {0}")
    protected void click(String selector) {
        page.locator(selector).click();
    }

    @Step("Read text from {0}")
    protected String getText(String selector) {
        return page.locator(selector).innerText();
    }

    protected boolean isVisible(String selector) {
        return page.locator(selector).isVisible();
    }

    protected void waitVisible(String selector) {
        page.locator(selector).waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    protected String currentUrl() {
        return page.url();
    }

    /**
     * Reload after the other actor's action — the app doesn't push updates.
     * Network idle is best-effort; a timeout here is expected on some pages.
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
