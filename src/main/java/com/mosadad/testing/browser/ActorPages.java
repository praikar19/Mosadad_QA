package com.mosadad.testing.browser;

import com.microsoft.playwright.Page;

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
