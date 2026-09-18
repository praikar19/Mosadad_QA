package com.mosadad.testing.base;

import com.mosadad.testing.browser.PlaywrightManager;
import com.mosadad.testing.config.ConfigManager;
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
}
