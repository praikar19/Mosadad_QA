package com.mosadad.testing.pages;

import com.mosadad.testing.constants.Routes;
import com.microsoft.playwright.Page;

/** /entity-portal/payment-history — only isLoaded() is verified. */
public class PaymentHistoryPage extends BasePage {

    public PaymentHistoryPage(Page page) {
        super(page);
    }

    public boolean isLoaded() {
        return currentUrl().contains(Routes.PAYMENT_HISTORY);
    }
}
