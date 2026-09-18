package com.mosadad.testing.pages;

import com.microsoft.playwright.Page;

/**
 * /auth/login — locators verified live against the QA environment on
 * 2026-08-30 (Angular app; ids are stable form-control ids, not generated).
 */
public class LoginPage extends BasePage {

    private static final String EMAIL_INPUT = "#email";
    private static final String PASSWORD_INPUT = "#password";
    private static final String REMEMBER_EMAIL_CHECKBOX = "input[name='remember']";
    private static final String FORGOT_PASSWORD_LINK = "text=Forgot Password?";
    private static final String SIGN_IN_BUTTON = "button[type='submit']";
    // ngx-toastr error toast — verified live 2026-09-01 against "Wrong Username or Password."
    private static final String ERROR_TOAST = "#toast-container .toast-error";

    public LoginPage(Page page) {
        super(page);
    }

    /**
     * Submits the form and returns immediately — does NOT wait for the
     * outcome, deliberately. Success and failure need different waits
     * (dashboard content appearing vs. a toast that auto-dismisses in a
     * few seconds), and an internal one-size-fits-all wait here previously
     * ate into the toast's visible window before callers ever got to check
     * it. Callers wait for what they actually expect:
     *   - success: DashboardPage.isLoaded() already waits for its content.
     *   - failure: isErrorToastVisible() / getErrorToastMessage() wait for the toast.
     */
    public DashboardPage login(String email, String password) {
        fill(EMAIL_INPUT, email);
        fill(PASSWORD_INPUT, password);
        click(SIGN_IN_BUTTON);
        return new DashboardPage(page);
    }

    public void clickForgotPassword() {
        click(FORGOT_PASSWORD_LINK);
    }

    public boolean isOnLoginPage() {
        return currentUrl().contains("/auth/login");
    }

    /** ngx-toastr auto-dismisses after a few seconds — waits (bounded) for it to appear rather than a single instant check. */
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
