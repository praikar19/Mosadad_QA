package com.mosadad.testing.tests.auth;

import com.mosadad.testing.base.BaseUiTest;
import com.mosadad.testing.config.ConfigManager;
import com.mosadad.testing.pages.DashboardPage;
import io.qameta.allure.*;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verified live against the QA environment (2026-08-30) as the Claimant
 * Insurer role. This is the reference example for the rest of the
 * framework — every locator and assertion here was confirmed against the
 * real app, not guessed.
 */
@Epic("Mosadad Recovery Claim")
@Feature("Authentication")
public class LoginUiTest extends BaseUiTest {

    @Test(groups = {"auth", "ui", "smoke"})
    @Severity(SeverityLevel.BLOCKER)
    @Description("Valid Claimant Insurer credentials must land on the entity dashboard with the Entity Modules sidebar visible.")
    public void loginWithValidCredentialsLandsOnDashboard() {
        navigateToLogin();

        DashboardPage dashboard = loginPage.login(
                ConfigManager.getEmail("qa", "dubai"),
                ConfigManager.getPassword("qa", "dubai")
        );

        assertThat(dashboard.isLoaded())
                .as("Dashboard should load at /entity-landing/dashboard with the In-Process Claims section visible")
                .isTrue();
        assertThat(dashboard.isEntityModulesSidebarVisible())
                .as("Home / Recovery Claims / Company Details sidebar links should be visible after login")
                .isTrue();
    }

    @Test(groups = {"auth", "ui"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("Invalid credentials must show the 'Wrong Username or Password.' toast and not navigate away from /auth/login.")
    public void loginWithInvalidCredentialsStaysOnLoginPage() {
        navigateToLogin();

        loginPage.login("nobody@nowhere.com", "wrong-password");

        assertThat(loginPage.isErrorToastVisible())
                .as("ngx-toastr error toast should appear on invalid login")
                .isTrue();
        assertThat(loginPage.getErrorToastMessage())
                .isEqualTo("Wrong Username or Password.");
        assertThat(loginPage.isOnLoginPage())
                .as("Invalid login must not navigate away from /auth/login")
                .isTrue();
    }
}
