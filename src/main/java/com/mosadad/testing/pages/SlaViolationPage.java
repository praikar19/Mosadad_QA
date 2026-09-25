package com.mosadad.testing.pages;

import com.mosadad.testing.constants.Routes;
import com.microsoft.playwright.Page;

/** /entity-portal/sla-violation — only isLoaded() is verified. */
public class SlaViolationPage extends BasePage {

    public SlaViolationPage(Page page) {
        super(page);
    }

    public boolean isLoaded() {
        return currentUrl().contains(Routes.SLA_VIOLATION);
    }
}
