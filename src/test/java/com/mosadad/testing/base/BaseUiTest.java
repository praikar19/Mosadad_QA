package com.mosadad.testing.base;

import com.mosadad.testing.browser.AuthStateCache;
import com.mosadad.testing.browser.PlaywrightManager;
import com.mosadad.testing.config.ConfigManager;
import com.mosadad.testing.constants.Routes;
import com.mosadad.testing.pages.DashboardPage;
import com.mosadad.testing.pages.LoginPage;
import com.microsoft.playwright.Page;
import io.qameta.allure.Step;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import java.time.format.DateTimeFormatter;

/** Base for UI tests: fresh browser context per test method, with a trace saved on teardown. */
public abstract class BaseUiTest extends BaseTest {

    protected Page page;
    protected LoginPage loginPage;

    @BeforeMethod(alwaysRun = true)
    @Parameters("browser")
    public void launchBrowser(@Optional String browserParam) {
        String browser = browserParam != null ? browserParam : ConfigManager.getBrowser();
        page = PlaywrightManager.launch(browser);
        loginPage = new LoginPage(page);
    }

    @AfterMethod(alwaysRun = true)
    public void closeBrowser() {
        String traceName = "target/traces/" + getClass().getSimpleName() + "-"
                + DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS").format(java.time.LocalDateTime.now()) + ".zip";
        PlaywrightManager.close(traceName);
    }

    protected void navigateToLogin() {
        page.navigate(ConfigManager.getLoginUrl());
    }

    /** Opens a context with a cached logged-in session. Don't use it in tests of the login form itself. */
    @Step("Log in with cached session (platform={0}, user={1})")
    protected DashboardPage loginWithCachedSession(String platform, String userKey) {
        String storageState = AuthStateCache.ensureLoggedIn(platform, userKey);
        page = PlaywrightManager.reopenWithStorageState(storageState);
        loginPage = new LoginPage(page);
        page.navigate(ConfigManager.getBaseUrl(platform) + Routes.DASHBOARD);
        return new DashboardPage(page);
    }
}
