package com.mosadad.testing.browser;

import com.mosadad.testing.config.ConfigManager;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Two concurrent sessions (claimant + at-fault insurer), deliberately on
 * different engines (Chrome and WebKit) so they stay fully independent.
 */
public final class TwoActorPlaywrightManager {

    private static final Logger log = LogManager.getLogger(TwoActorPlaywrightManager.class);

    private static final ThreadLocal<Playwright> PLAYWRIGHT = new ThreadLocal<>();
    private static final ThreadLocal<Browser> CLAIMANT_BROWSER = new ThreadLocal<>();
    private static final ThreadLocal<Browser> AT_FAULT_BROWSER = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> CLAIMANT_CONTEXT = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> AT_FAULT_CONTEXT = new ThreadLocal<>();

    private TwoActorPlaywrightManager() {}

    public static ActorPages openTwoBrowsers(String browser1, String browser2) {
        Playwright playwright = Playwright.create();

        Browser claimantBrowser = launchBrowser(playwright, browser1);
        Browser atFaultBrowser = launchBrowser(playwright, browser2);

        BrowserContext claimantContext = claimantBrowser.newContext();
        BrowserContext atFaultContext = atFaultBrowser.newContext();
        claimantContext.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true).setSources(true));
        atFaultContext.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true).setSources(true));

        Page claimantPage = claimantContext.newPage();
        Page atFaultPage = atFaultContext.newPage();

        PLAYWRIGHT.set(playwright);
        CLAIMANT_BROWSER.set(claimantBrowser);
        AT_FAULT_BROWSER.set(atFaultBrowser);
        CLAIMANT_CONTEXT.set(claimantContext);
        AT_FAULT_CONTEXT.set(atFaultContext);

        log.debug("Launched claimant (Chrome) + at-fault (WebKit) sessions on thread: {}", Thread.currentThread().getName());
        return new ActorPages(claimantPage, atFaultPage);
    }

    /** Each actor's open pages on this thread, labelled "claimant" / "at-fault"; empty if no session is open. */
    public static Map<String, Page> getOpenPages() {
        Map<String, Page> pages = new LinkedHashMap<>();
        addLastPage(pages, "claimant", CLAIMANT_CONTEXT.get());
        addLastPage(pages, "at-fault", AT_FAULT_CONTEXT.get());
        return pages;
    }

    private static void addLastPage(Map<String, Page> pages, String actor, BrowserContext context) {
        if (context == null) return;
        List<Page> open = context.pages();
        if (!open.isEmpty()) {
            pages.put(actor, open.get(open.size() - 1));
        }
    }

    /** "safari"/"webkit" mean the WebKit engine — Chromium has no "safari" channel. */
    private static Browser launchBrowser(Playwright playwright, String browserName) {
        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                .setHeadless(ConfigManager.isHeadless());

        return switch (browserName == null ? "chromium" : browserName.toLowerCase()) {
            case "firefox" -> playwright.firefox().launch(options);
            case "webkit", "safari" -> playwright.webkit().launch(options);
            case "edge" -> playwright.chromium().launch(options.setChannel("msedge"));
            case "chrome" -> playwright.chromium().launch(options.setChannel("chrome"));
            default -> playwright.chromium().launch(options);
        };
    }

    public static void closeTwoBrowsers(String tracePathPrefix) {
        try {
            BrowserContext claimantContext = CLAIMANT_CONTEXT.get();
            if (claimantContext != null) {
                if (tracePathPrefix != null) {
                    claimantContext.tracing().stop(new Tracing.StopOptions().setPath(Paths.get(tracePathPrefix + "-claimant.zip")));
                }
                claimantContext.close();
            }

            BrowserContext atFaultContext = AT_FAULT_CONTEXT.get();
            if (atFaultContext != null) {
                if (tracePathPrefix != null) {
                    atFaultContext.tracing().stop(new Tracing.StopOptions().setPath(Paths.get(tracePathPrefix + "-atFault.zip")));
                }
                atFaultContext.close();
            }

            Browser claimantBrowser = CLAIMANT_BROWSER.get();
            if (claimantBrowser != null) claimantBrowser.close();

            Browser atFaultBrowser = AT_FAULT_BROWSER.get();
            if (atFaultBrowser != null) atFaultBrowser.close();

            Playwright playwright = PLAYWRIGHT.get();
            if (playwright != null) playwright.close();

            log.debug("Closed two-actor session on thread: {}", Thread.currentThread().getName());
        } catch (Exception e) {
            log.warn("Exception while closing two-actor session: {}", e.getMessage());
        } finally {
            CLAIMANT_CONTEXT.remove();
            AT_FAULT_CONTEXT.remove();
            CLAIMANT_BROWSER.remove();
            AT_FAULT_BROWSER.remove();
            PLAYWRIGHT.remove();
        }
    }
}
