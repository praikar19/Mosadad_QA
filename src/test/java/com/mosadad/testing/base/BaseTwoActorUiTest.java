package com.mosadad.testing.base;

import com.microsoft.playwright.Page;
import com.mosadad.testing.browser.ActorPages;
import com.mosadad.testing.browser.TwoActorPlaywrightManager;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Base for any test needing two concurrently-logged-in browser sessions —
 * a claimant insurer and an at-fault insurer, each in their own browser
 * engine (see TwoActorPlaywrightManager's Javadoc for why two engines, not
 * two contexts on one engine). Mirrors BaseUiTest's single-session
 * lifecycle: launch both sessions in @BeforeMethod, tear both down (with a
 * trace per actor) in @AfterMethod.
 *
 * Extends BaseTest directly, not BaseUiTest — BaseUiTest's own
 * @BeforeMethod would launch a third, unused single-actor session on top of
 * these two.
 *
 * Default engines are chrome (claimant) / safari (at-fault); override
 * claimantBrowser()/atFaultBrowser() for a different pairing (e.g. two
 * Chrome sessions).
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
