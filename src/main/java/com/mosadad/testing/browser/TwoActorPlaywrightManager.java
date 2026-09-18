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

/**
 * Reusable two-actor browser session, for any test where a claimant insurer
 * and an at-fault insurer need to be logged in at the same time (claim
 * submitted by one, seen/acted on by the other — see MOSADAD_DOMAIN.md).
 *
 * Same ThreadLocal pattern as {@link PlaywrightManager}, but holds two
 * independent sessions instead of one: PlaywrightManager is right for every
 * single-actor test in this suite, this is for the two-actor case.
 *
 * Deliberately two different browser engines (real Chrome for the claimant,
 * Playwright's WebKit — the engine Safari is built on — for the at-fault
 * insurer) rather than two contexts on the same engine, so the two sessions
 * are as independent as two different users on two different machines would
 * be. Playwright cannot drive the real Safari.app (that needs Apple's own
 * safaridriver, a different tool); WebKit is the standard stand-in.
 *
 * Usage:
 *   ActorPages pages = TwoActorPlaywrightManager.openTwoBrowsers();   // @BeforeMethod
 *   ... pages.getClaimantPage() / pages.getAtFaultPage() ...
 *   TwoActorPlaywrightManager.closeTwoBrowsers(tracePathPrefix);      // @AfterMethod
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

    /**
     * Resolves a browser name to the right BrowserType (+ channel where
     * needed) — same mapping PlaywrightManager uses for the single-actor
     * case. "safari" and "webkit" both mean Playwright's WebKit engine:
     * there is no such thing as a "safari" channel on Chromium, since
     * Safari isn't Chromium-based — Chromium.launch().setChannel("safari")
     * fails at launch with "Unsupported chromium channel". Only "chrome"
     * and "edge" are real Chromium channels.
     */
    private static Browser launchBrowser(Playwright playwright, String browserName) {
        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                .setHeadless(false);

        //ConfigManager.isHeadless()

        return switch (browserName == null ? "chromium" : browserName.toLowerCase()) {
            case "firefox" -> playwright.firefox().launch(options);
            case "webkit", "safari" -> playwright.webkit().launch(options);
            case "edge" -> playwright.chromium().launch(options.setChannel("msedge"));
            case "chrome" -> playwright.chromium().launch(options.setChannel("chrome"));
            default -> playwright.chromium().launch(options);
        };
    }

    /** Stops tracing (saving to "<tracePathPrefix>-claimant.zip" / "-atFault.zip"), closes both sessions, clears the ThreadLocals. */
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
