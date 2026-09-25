package com.mosadad.testing.pages;

import com.mosadad.testing.constants.Routes;
import com.microsoft.playwright.Page;

/** /entity-portal/my-claims — only isLoaded() is verified. */
public class RecoveryClaimsListPage extends BasePage {

    public RecoveryClaimsListPage(Page page) {
        super(page);
    }

    public boolean isLoaded() {
        return currentUrl().contains(Routes.RECOVERY_CLAIMS_LIST);
    }
}
