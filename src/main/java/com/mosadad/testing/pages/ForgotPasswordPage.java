package com.mosadad.testing.pages;

import com.mosadad.testing.constants.Routes;
import com.microsoft.playwright.Page;

/**
 * Forgot Password screen (/auth/forget-password), reached from the login page.
 * Deliberately has no submit method: submitting sends a real reset email.
 */
public class ForgotPasswordPage extends BasePage {

    private static final String HEADER = ".forget-header";
    private static final String EMAIL_INPUT = "#email";
    private static final String SUBMIT_BUTTON = "button[type='submit']";
    private static final String EMAIL_ERROR = ".text-danger";

    public ForgotPasswordPage(Page page) {
        super(page);
    }

    public boolean isLoaded() {
        try {
            waitVisible(HEADER);
        } catch (com.microsoft.playwright.PlaywrightException timeout) {
            log.debug("Forgot Password header did not become visible in time: {}", timeout.getMessage());
            return false;
        }
        return currentUrl().contains(Routes.FORGOT_PASSWORD);
    }

    public String getHeader() {
        return getText(HEADER);
    }

    /** Fills the email and leaves the field, which is when the form shows its validation message. */
    public void enterEmail(String email) {
        fill(EMAIL_INPUT, email);
        locator(EMAIL_INPUT).blur();
    }

    public boolean isSubmitEnabled() {
        return locator(SUBMIT_BUTTON).isEnabled();
    }

    public String getEmailError() {
        return getText(EMAIL_ERROR);
    }
}
