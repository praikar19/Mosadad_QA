package com.mosadad.testing.base;

import com.mosadad.testing.api.ApiClient;
import com.mosadad.testing.listeners.TestListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;

/**
 * Root base for all tests — API and UI alike.
 *
 * Pure API tests extend this directly (no browser needed, fast CI gate).
 * UI tests extend BaseUiTest, which extends this and adds the Playwright
 * session lifecycle.
 *
 * Only TestListener is declared here — AllureTestNg is NOT, on purpose.
 * allure-testng ships its own META-INF/services/org.testng.ITestNGListener
 * (confirmed live in the 2.29.0 jar), so TestNG's ServiceLoader already
 * registers it automatically for every run; declaring it again here (or in
 * a suite XML's &lt;listeners&gt;) creates duplicate AllureTestNg instances
 * that race each other to start/stop/write the same Allure test case.
 * Confirmed live 2026-09-21: this caused TestListener's failure screenshots
 * to be written as orphaned attachments (no result.json referenced them) —
 * one AllureTestNg instance had already stopped+written the test case by
 * the time TestListener's own onTestFailure (also double-firing from the
 * same root cause) tried to attach the screenshot to it.
 */
@Listeners(TestListener.class)
public abstract class BaseTest {

    protected final Logger log = LogManager.getLogger(getClass());
    protected ApiClient apiClient;

    @BeforeClass(alwaysRun = true)
    public void initClients() {
        apiClient = new ApiClient();
        log.info("Clients initialised for: {}", getClass().getSimpleName());
    }
}
