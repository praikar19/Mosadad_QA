package com.mosadad.testing.pages;

import com.mosadad.testing.constants.Routes;
import com.microsoft.playwright.Page;

/**
 * /entity-portal/payment-history — Financial module, reached via
 * RecoveryClaimsHubPage.openPaymentHistory(). URL verified live 2026-08-30
 * (RecoveryClaimsNavigationTest); page content itself not yet explored —
 * only isLoaded() is backed by anything real so far.
 */
public class PaymentHistoryPage extends BasePage {

    public PaymentHistoryPage(Page page) {
        super(page);
    }

    public boolean isLoaded() {
        return currentUrl().contains(Routes.PAYMENT_HISTORY);
    }
}
