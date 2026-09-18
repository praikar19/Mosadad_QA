package com.mosadad.testing.browser;

import com.microsoft.playwright.Page;

/**
 * The two Playwright pages returned by
 * {@link TwoActorPlaywrightManager#openTwoBrowsers()} — claimant insurer on
 * Chrome, at-fault insurer on WebKit (Safari's engine).
 */
public class ActorPages {

    private final Page claimantPage;
    private final Page atFaultPage;

    public ActorPages(Page claimantPage, Page atFaultPage) {
        this.claimantPage = claimantPage;
        this.atFaultPage = atFaultPage;
    }

    public Page getClaimantPage() { return claimantPage; }
    public Page getAtFaultPage() { return atFaultPage; }
}
