package com.mosadad.testing.tests.auth;

import com.mosadad.testing.base.BaseUiTest;
import com.mosadad.testing.pages.ForgotPasswordPage;
import io.qameta.allure.*;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Forgot Password link and form validation. Never clicks Submit: that sends a
 * real reset email from the shared QA environment.
 */
@Epic("Mosadad Recovery Claim")
@Feature("Authentication")
public class ForgotPasswordUiTest extends BaseUiTest {

    @Test(groups = {"auth", "ui"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("The login page's 'Forgot Password?' link opens /auth/forget-password with Submit disabled until an email is entered.")
    public void forgotPasswordLinkOpensForgotPasswordPage() {
        navigateToLogin();

        ForgotPasswordPage forgotPassword = loginPage.clickForgotPassword();

        assertThat(forgotPassword.isLoaded())
                .as("Forgot Password page should load at /auth/forget-password")
                .isTrue();
        assertThat(forgotPassword.getHeader()).isEqualTo("Forgot Password");
        assertThat(forgotPassword.isSubmitEnabled())
                .as("Submit should be disabled while the email is empty")
                .isFalse();
    }

    @Test(groups = {"auth", "ui"})
    @Severity(SeverityLevel.NORMAL)
    @Description("An email in the wrong format shows 'Invalid email format.' and keeps Submit disabled.")
    public void forgotPasswordRejectsInvalidEmailFormat() {
        navigateToLogin();
        ForgotPasswordPage forgotPassword = loginPage.clickForgotPassword();

        forgotPassword.enterEmail("not-an-email");

        assertThat(forgotPassword.getEmailError()).isEqualTo("Invalid email format.");
        assertThat(forgotPassword.isSubmitEnabled())
                .as("Submit should stay disabled for an invalid email")
                .isFalse();
    }

    @Test(groups = {"auth", "ui"})
    @Severity(SeverityLevel.NORMAL)
    @Description("A correctly formatted email enables Submit. The button is not clicked, so no reset email is sent.")
    public void forgotPasswordEnablesSubmitForValidEmail() {
        navigateToLogin();
        ForgotPasswordPage forgotPassword = loginPage.clickForgotPassword();

        forgotPassword.enterEmail("qa-automation@example.com");

        assertThat(forgotPassword.isSubmitEnabled())
                .as("Submit should be enabled once a valid email is entered")
                .isTrue();
    }
}
