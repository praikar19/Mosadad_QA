package com.mosadad.testing.base;

import com.mosadad.testing.api.ApiClient;
import com.mosadad.testing.listeners.TestListener;
import io.qameta.allure.testng.AllureTestNg;
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
 */
@Listeners({ TestListener.class, AllureTestNg.class })
public abstract class BaseTest {

    protected final Logger log = LogManager.getLogger(getClass());
    protected ApiClient apiClient;

    @BeforeClass(alwaysRun = true)
    public void initClients() {
        apiClient = new ApiClient();
        log.info("Clients initialised for: {}", getClass().getSimpleName());
    }
}
