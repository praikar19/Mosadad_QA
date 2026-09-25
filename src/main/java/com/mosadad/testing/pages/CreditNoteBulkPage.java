package com.mosadad.testing.pages;

import com.mosadad.testing.constants.Routes;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import io.qameta.allure.Step;

import java.util.List;

/**
 * /entity-portal/credit-note-bulk ("Credit Note Bulk Details") — every credit
 * note the at-fault owes one entity (Serial Number, At Fault Claim Number,
 * Approved Claim Amount, Accident Report Date). See SettlementTable for the table
 * and panel. From the app's code (2026-09-24): a claim whose checkout was started
 * is disabled for 24h (Payment Pending); "Proceed To Payment" goes via /callback
 * ("Redirecting...") to the ATB checkout, or to payment-failure if the payment
 * can't be started.
 */
public class CreditNoteBulkPage extends BasePage {

    private static final String PROCEED_TO_PAYMENT_BTN = "button:text-is('Proceed To Payment')";
    private static final String CHECKOUT_CONFIRM_BTN = "button:text-is('Confirm')";

    public CreditNoteBulkPage(Page page) {
        super(page);
    }

    public boolean isLoaded() {
        return currentUrl().contains(Routes.CREDIT_NOTE_BULK);
    }

    /** Ticks exactly the given claims; the page lists every credit note owed to the entity. */
    @Step("Select only claims {0}")
    public CreditNoteBulkPage selectOnly(List<String> claimSerialNumbers) {
        SettlementTable.selectOnly(page, claimSerialNumbers, "Credit Note Bulk Details");
        return this;
    }

    /** "count / total" as the side panel shows it once it matches, e.g. "2 / 7,200.00". */
    public String waitForSummary(int expectedCount, String expectedTotal) {
        return SettlementTable.waitForSummary(page, expectedCount, expectedTotal);
    }

    /**
     * Opens the ATB checkout. Starting it locks the selected claims for 24h, so it is
     * never retried: fails on payment-failure or if the checkout doesn't render.
     */
    @Step("Click 'Proceed To Payment'")
    public AtbCheckoutPage proceedToPayment() {
        locator(PROCEED_TO_PAYMENT_BTN).click();
        try {
            page.waitForURL(url -> url.contains(AtbCheckoutPage.HOST) || url.contains(Routes.PAYMENT_FAILURE),
                    new Page.WaitForURLOptions().setTimeout(45000));
        } catch (com.microsoft.playwright.PlaywrightException e) {
            throw new IllegalStateException("Proceed To Payment didn't reach the ATB checkout; now on " + currentUrl(), e);
        }
        if (currentUrl().contains(Routes.PAYMENT_FAILURE)) {
            throw new IllegalStateException("Proceed To Payment ended on payment-failure: the payment couldn't be started");
        }
        try {
            locator(CHECKOUT_CONFIRM_BTN).waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE).setTimeout(45000));
        } catch (com.microsoft.playwright.PlaywrightException e) {
            throw new IllegalStateException("The ATB checkout opened but never showed Confirm. "
                    + "The selected claims are now locked for 24h (Payment Pending).", e);
        }
        return new AtbCheckoutPage(page);
    }
}
