package com.mosadad.testing.tests.auth;

import com.mosadad.testing.base.BaseUiTest;
import io.qameta.allure.*;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Login page language switcher (English / Arabic). Each test gets a fresh
 * browser context, so a language chosen here never leaks into other tests.
 */
@Epic("Mosadad Recovery Claim")
@Feature("Authentication")
public class LoginLanguageUiTest extends BaseUiTest {

    private static final String ENGLISH = "English";
    private static final String ARABIC = "العربية";

    @Test(groups = {"auth", "ui"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("Choosing Arabic translates the login page and switches it to right-to-left layout.")
    public void switchingToArabicTranslatesLoginPageToRtl() {
        navigateToLogin();

        loginPage.switchLanguage(ARABIC);

        assertThat(loginPage.getPageDirection()).as("Page direction in Arabic").isEqualTo("rtl");
        assertThat(loginPage.getSignInHeader()).isEqualTo("تسجيل الدخول");
        assertThat(loginPage.getLanguagesButtonText()).isEqualTo("اللغات");
    }

    @Test(groups = {"auth", "ui"})
    @Severity(SeverityLevel.NORMAL)
    @Description("The chosen language is remembered after a page reload.")
    public void arabicSelectionPersistsAfterReload() {
        navigateToLogin();
        loginPage.switchLanguage(ARABIC);

        loginPage.reload();

        assertThat(loginPage.getStoredLanguage()).as("Language saved in localStorage").isEqualTo("ar");
        assertThat(loginPage.getPageDirection()).as("Page direction after reload").isEqualTo("rtl");
        assertThat(loginPage.getSignInHeader()).isEqualTo("تسجيل الدخول");
    }

    @Test(groups = {"auth", "ui"})
    @Severity(SeverityLevel.NORMAL)
    @Description("Switching from Arabic back to English restores English text and left-to-right layout.")
    public void switchingBackToEnglishRestoresLtr() {
        navigateToLogin();
        loginPage.switchLanguage(ARABIC);

        loginPage.switchLanguage(ENGLISH);

        assertThat(loginPage.getPageDirection()).as("Page direction in English").isEqualTo("ltr");
        assertThat(loginPage.getSignInHeader()).isEqualTo("Sign in");
        assertThat(loginPage.getStoredLanguage()).isEqualTo("en");
    }
}
