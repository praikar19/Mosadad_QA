package com.mosadad.testing.base;

import com.mosadad.testing.browser.AuthStateCache;
import com.mosadad.testing.browser.PlaywrightManager;
import com.mosadad.testing.config.ConfigManager;
import com.mosadad.testing.constants.Routes;
import com.mosadad.testing.pages.DashboardPage;
import com.mosadad.testing.pages.LoginPage;
import com.microsoft.playwright.Page;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import java.time.format.DateTimeFormatter;

/**
 * Base for UI tests. Launches a fresh Playwright browser/context/page per
 * test method (deliberate — reusing a context across methods leaks cookies
 * and navigation state between tests) and tears it down afterwards, saving
 * a Playwright trace for debugging failures.
 *
 * Browser is chosen via the "browser" TestNG parameter (see testng*.xml),
 * falling back to config.properties' `browser` key (default: chromium).
 */
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

    /**
     * For tests that need to *be* logged in but aren't testing the login
     * form itself: skips driving LoginPage's real form and instead reopens
     * this test's context with a cached, already-authenticated storage
     * state (see AuthStateCache) — a real login still happens, just once
     * per platform/userKey for the whole suite run, not once per @Test
     * method. Still a fresh, isolated context per test method, same as
     * launchBrowser() gives every test — just pre-seeded instead of blank.
     *
     * Call from a subclass @BeforeMethod in place of
     * navigateToLogin()/loginPage.login(...). Don't use this in a test
     * that exercises the real login form — see LoginUiTest, which keeps
     * using navigateToLogin() + loginPage.login(...) directly.
     */
    protected DashboardPage loginWithCachedSession(String platform, String userKey) {
        String storageState = AuthStateCache.ensureLoggedIn(platform, userKey);
        page = PlaywrightManager.reopenWithStorageState(storageState);
        loginPage = new LoginPage(page);
        page.navigate(ConfigManager.getBaseUrl(platform) + Routes.DASHBOARD);
        return new DashboardPage(page);
    }
}
