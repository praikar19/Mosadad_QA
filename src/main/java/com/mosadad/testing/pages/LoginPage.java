package com.mosadad.testing.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

public class LoginPage extends BasePage {

    private static final String EMAIL_INPUT = "#email";
    private static final String PASSWORD_INPUT = "#password";
    // Class, not text: the link text is translated when the page is in Arabic.
    private static final String FORGOT_PASSWORD_LINK = "a.forgot-password";
    private static final String SIGN_IN_BUTTON = "button[type='submit']";
    private static final String ERROR_TOAST = "#toast-container .toast-error";
    private static final String SIGN_IN_HEADER = ".login-header";
    private static final String LANGUAGES_BUTTON = ".languages-button";
    private static final String LANGUAGE_OPTION = ".languages-menu li";

    public LoginPage(Page page) {
        super(page);
    }

    /** Returns immediately — callers wait for the dashboard or the error toast. */
    // Only {0} (email) in the label — never put the password in a report.
    @Step("Log in as {0}")
    public DashboardPage login(String email, String password) {
        fill(EMAIL_INPUT, email);
        fill(PASSWORD_INPUT, password);
        click(SIGN_IN_BUTTON);
        return new DashboardPage(page);
    }

    @Step("Open Forgot Password")
    public ForgotPasswordPage clickForgotPassword() {
        click(FORGOT_PASSWORD_LINK);
        return new ForgotPasswordPage(page);
    }

    /**
     * optionText is the label shown in the menu: "English" or "العربية". Must
     * be a different language from the current one: returns once the page text
     * has changed, because translations load a moment after the click.
     */
    @Step("Switch language to {0}")
    public void switchLanguage(String optionText) {
        String headerBefore = getSignInHeader();
        click(LANGUAGES_BUTTON);
        locator(LANGUAGE_OPTION).filter(new Locator.FilterOptions().setHasText(optionText)).click();
        page.waitForFunction("([selector, before]) => document.querySelector(selector)?.innerText.trim() !== before",
                java.util.List.of(SIGN_IN_HEADER, headerBefore));
    }

    /** Reloads and waits for the app to boot; the page direction is empty until it has. */
    @Step("Reload login page")
    public void reload() {
        reloadAndWaitForNetworkIdle();
        waitVisible(SIGN_IN_HEADER);
    }

    public String getSignInHeader() {
        return getText(SIGN_IN_HEADER);
    }

    public String getLanguagesButtonText() {
        return getText(LANGUAGES_BUTTON);
    }

    /** "rtl" in Arabic, "ltr" in English. The app leaves the html lang attribute at "en" either way. */
    public String getPageDirection() {
        return (String) page.evaluate("document.documentElement.dir");
    }

    /** The app keeps the chosen language in localStorage, so it survives a reload. */
    public String getStoredLanguage() {
        return (String) page.evaluate("localStorage.getItem('lang')");
    }

    public boolean isOnLoginPage() {
        return currentUrl().contains("/auth/login");
    }

    public boolean isErrorToastVisible() {
        try {
            waitVisible(ERROR_TOAST);
        } catch (com.microsoft.playwright.PlaywrightException timeout) {
            log.debug("Error toast did not become visible in time: {}", timeout.getMessage());
            return false;
        }
        return true;
    }

    public String getErrorToastMessage() {
        return getText(ERROR_TOAST + " .toast-message");
    }
}
