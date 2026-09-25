package com.mosadad.testing.base;

import com.mosadad.testing.api.ApiClient;
import com.mosadad.testing.listeners.TestListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;

/**
 * Root base for all tests. Don't register AllureTestNg here or in suite XML —
 * it self-registers, and a duplicate breaks screenshot attachments.
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
