package com.mosadad.testing.browser;

import com.mosadad.testing.config.ConfigManager;
import com.microsoft.playwright.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Thread-safe Playwright session holder using ThreadLocal.
 *
 * Why ThreadLocal?
 *   When TestNG runs tests in parallel (parallel="methods" or "classes" in
 *   testng.xml), each thread needs its own Playwright/Browser/BrowserContext/
 *   Page — a static field would let one test's navigation bleed into another's
 *   assertions. This mirrors the pattern used for Appium's DriverManager in
 *   the sibling mobile framework (ShopTestApp-Java-Mobile-Testing), applied to
 *   Playwright's session objects instead of a single WebDriver.
 *
 * Usage lifecycle (managed by BaseUiTest):
 *   1. launch(browserName)  — called in @BeforeMethod
 *   2. getPage()            — used in page objects and tests
 *   3. close()              — stops tracing, tears down context/browser/playwright
 */
public final class PlaywrightManager {

    private static final Logger log = LogManager.getLogger(PlaywrightManager.class);

    private static final ThreadLocal<Playwright> PLAYWRIGHT = new ThreadLocal<>();
    private static final ThreadLocal<Browser> BROWSER = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> CONTEXT = new ThreadLocal<>();
    private static final ThreadLocal<Page> PAGE = new ThreadLocal<>();

    private PlaywrightManager() {}

    public static Page launch(String browserName) {
        Playwright playwright = Playwright.create();
        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                .setHeadless(false)
                .setSlowMo(ConfigManager.getSlowMoMs());
        //browsers = chromium.launch(new BrowserType.LaunchOptions().setHeadless(ConfigManager.isHeadless()));
        BrowserType browserType = switch (browserName == null ? "chromium" : browserName.toLowerCase()) {
            case "firefox" -> playwright.firefox();
            case "webkit" -> playwright.webkit();
            case "edge" -> {
                options.setChannel("msedge");
                yield playwright.chromium();
            }
            case "chrome" -> {
                options.setChannel("chrome");
                yield playwright.chromium();
            }
            default -> playwright.chromium();
        };

        Browser browser = browserType.launch(options);
        BrowserContext context = browser.newContext();
        context.setDefaultTimeout(ConfigManager.getExplicitWaitSeconds() * 1000);
        context.setDefaultNavigationTimeout(ConfigManager.getNavigationTimeoutSeconds() * 1000);
        context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true));

        Page page = context.newPage();

        PLAYWRIGHT.set(playwright);
        BROWSER.set(browser);
        CONTEXT.set(context);
        PAGE.set(page);

        log.debug("Playwright[{}] launched on thread: {}", browserName, Thread.currentThread().getName());
        return page;
    }

    public static Page getPage() {
        Page page = PAGE.get();
        if (page == null) {
            throw new IllegalStateException(
                "No Playwright Page for thread [" + Thread.currentThread().getName() +
                "]. Ensure @BeforeMethod called PlaywrightManager.launch() first.");
        }
        return page;
    }

    public static boolean hasPage() {
        return PAGE.get() != null;
    }

    /** Stops tracing (saving to targetPath) and closes context/browser/playwright, then clears the ThreadLocals. */
    public static void close(String tracePath) {
        try {
            BrowserContext context = CONTEXT.get();
            if (context != null && tracePath != null) {
                context.tracing().stop(new Tracing.StopOptions().setPath(java.nio.file.Paths.get(tracePath)));
            }
            if (context != null) context.close();

            Browser browser = BROWSER.get();
            if (browser != null) browser.close();

            Playwright playwright = PLAYWRIGHT.get();
            if (playwright != null) playwright.close();

            log.debug("Playwright session closed on thread: {}", Thread.currentThread().getName());
        } catch (Exception e) {
            log.warn("Exception while closing Playwright session: {}", e.getMessage());
        } finally {
            PAGE.remove();
            CONTEXT.remove();
            BROWSER.remove();
            PLAYWRIGHT.remove();
        }
    }
}
