package com.mosadad.testing.browser;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.mosadad.testing.config.ConfigManager;
import com.mosadad.testing.pages.DashboardPage;
import com.mosadad.testing.pages.LoginPage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches a real, logged-in Playwright storage state (cookies + localStorage
 * — wherever the app's own session actually lives; storageState captures
 * both, so it doesn't matter which) per platform+user, JVM-wide. Same idea
 * as {@code ApiClient}'s cached access token: one real login for the whole
 * suite run instead of one per test method.
 *
 * A UI test that just needs to *be* logged in (not test the login form
 * itself) calls {@link #ensureLoggedIn(String, String)} and hands the
 * result to {@link PlaywrightManager#reopenWithStorageState(String)}
 * instead of driving LoginPage's real form on every @Test method — see
 * BaseUiTest.loginWithCachedSession(). LoginUiTest itself deliberately
 * doesn't use this: it's the one test that has to exercise the real login
 * form.
 *
 * In-memory only, never written to disk — the cache lives exactly as long
 * as this JVM run (one {@code mvn test}), so a stale/expired token from a
 * previous run can never leak into this one. The tradeoff is the one
 * bootstrap login below always happens at least once per key per run; that
 * cost is unavoidable (something has to prove the credentials are valid)
 * and it's exactly what PlaywrightManager would have paid anyway for a
 * single test.
 */
public final class AuthStateCache {

    private static final Logger log = LogManager.getLogger(AuthStateCache.class);
    private static final Map<String, String> CACHE = new ConcurrentHashMap<>();
    private static final Object LOCK = new Object();

    private AuthStateCache() {}

    /**
     * Returns a Playwright storageState JSON string for platform+userKey
     * (e.g. {@code ensureLoggedIn("qa", "dubai")} for the credentials at
     * {@code qa.claimant.email.dubai} / {@code qa.claimant.password.dubai}
     * in credentials.properties), logging in for real exactly once per key
     * per JVM run — double-checked locking, same pattern as
     * {@code ApiClient.ensureAuthenticated()}. Every later call for the
     * same key returns the cached value immediately, no browser involved.
     *
     * The bootstrap login runs in its own throwaway, always-headless
     * browser (nobody needs to watch a one-off login happen), independent
     * of whatever browser/headless setting the calling test itself uses.
     */
    public static String ensureLoggedIn(String platform, String userKey) {
        String key = platform + ":" + userKey;
        String cached = CACHE.get(key);
        if (cached != null) {
            return cached;
        }

        synchronized (LOCK) {
            cached = CACHE.get(key);
            if (cached != null) {
                return cached;
            }

            log.info("No cached session for [{}] yet — logging in for real once to seed it", key);
            try (Playwright playwright = Playwright.create()) {
                Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
                BrowserContext context = browser.newContext();
                Page page = context.newPage();

                page.navigate(ConfigManager.getLoginUrl(platform));
                DashboardPage dashboard = new LoginPage(page).login(
                        ConfigManager.getEmail(platform, userKey),
                        ConfigManager.getPassword(platform, userKey));

                if (!dashboard.isLoaded()) {
                    throw new IllegalStateException(
                        "Could not bootstrap a cached session for [" + key + "] — " +
                        "login did not reach the dashboard. Check credentials.properties.");
                }

                String state = context.storageState();
                browser.close();

                CACHE.put(key, state);
                log.info("Cached session for [{}] — subsequent tests reuse it without a real login", key);
                return state;
            }
        }
    }

    /** Drops every cached session, e.g. after a test that intentionally logs out or invalidates its token. */
    public static void clear() {
        CACHE.clear();
    }
}
