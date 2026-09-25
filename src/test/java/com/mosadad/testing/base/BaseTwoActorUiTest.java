package com.mosadad.testing.base;

import com.microsoft.playwright.Page;
import com.mosadad.testing.browser.ActorPages;
import com.mosadad.testing.browser.TwoActorPlaywrightManager;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Base for tests needing a claimant and an at-fault session at once. Extends
 * BaseTest, not BaseUiTest, to avoid launching a third unused session.
 */
public abstract class BaseTwoActorUiTest extends BaseTest {

    protected Page claimantPage;
    protected Page atFaultPage;

    protected String claimantBrowser() { return "chrome"; }
    protected String atFaultBrowser()  { return "safari"; }

    @BeforeMethod(alwaysRun = true)
    public void launchTwoBrowsers() {
        ActorPages pages = TwoActorPlaywrightManager.openTwoBrowsers(claimantBrowser(), atFaultBrowser());
        claimantPage = pages.getClaimantPage();
        atFaultPage = pages.getAtFaultPage();
        log.info("Launched claimant ({}) + at-fault ({}) sessions", claimantBrowser(), atFaultBrowser());
    }

    @AfterMethod(alwaysRun = true)
    public void closeTwoBrowsers() {
        String tracePathPrefix = "target/traces/" + getClass().getSimpleName() + "-"
                + DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS").format(LocalDateTime.now());
        TwoActorPlaywrightManager.closeTwoBrowsers(tracePathPrefix);
    }
}
