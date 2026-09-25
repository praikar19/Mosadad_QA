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
 * Caches a logged-in Playwright storageState per platform+user for the JVM,
 * so UI tests skip the login form (see BaseUiTest.loginWithCachedSession()).
 * In memory only.
 */
public final class AuthStateCache {

    private static final Logger log = LogManager.getLogger(AuthStateCache.class);
    private static final Map<String, String> CACHE = new ConcurrentHashMap<>();
    private static final Object LOCK = new Object();

    private AuthStateCache() {}

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

    public static void clear() {
        CACHE.clear();
    }
}
