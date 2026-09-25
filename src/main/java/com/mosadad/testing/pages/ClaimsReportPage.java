package com.mosadad.testing.pages;

import com.mosadad.testing.constants.Routes;
import com.microsoft.playwright.Page;

/** /entity-portal/financial-statement-report — only isLoaded() is verified. */
public class ClaimsReportPage extends BasePage {

    public ClaimsReportPage(Page page) {
        super(page);
    }

    public boolean isLoaded() {
        return currentUrl().contains(Routes.CLAIMS_REPORT);
    }
}
