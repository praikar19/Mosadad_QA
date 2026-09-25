package com.mosadad.testing.pages;

import com.mosadad.testing.constants.Routes;
import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ATB Pay checkout ("ATB PGA Checkout" on rak.atbpay.me), reached from Credit
 * Note Bulk Details → Proceed To Payment. Shows the paying account and balance,
 * an "Invoice Details" block per payee plus Mosadad fees, "I agree to
 * Terms&amp;Conditions", Confirm and Cancel Process. After Confirm, ATB returns to
 * Mosadad's payment-status page, which forwards to payment-success (with the
 * transactionId) or payment-failure (from the app's code, 2026-09-24).
 * Confirm moves real money from the at-fault's wallet.
 */
public class AtbCheckoutPage extends BasePage {

    public static final String HOST = "atbpay.me";
    private static final String TERMS_CHECKBOX = "input[type='checkbox']";
    private static final String CONFIRM_BTN = "button:text-is('Confirm')";

    public AtbCheckoutPage(Page page) {
        super(page);
    }

    public boolean isLoaded() {
        return currentUrl().contains(HOST);
    }

    /** The "Total Amount" at the bottom of Invoice Details, e.g. "5136.50" (claims plus fees). */
    public String getTotalAmount() {
        Matcher m = Pattern.compile("Total Amount\\s*\\n?\\s*\\S?\\s*([\\d,.]+)").matcher(locator("body").innerText());
        return m.find() ? m.group(1) : "";
    }

    /** Full page text, for checking the payee's amount line and recording the fees. */
    public String getInvoiceText() {
        return locator("body").innerText();
    }

    @Step("Tick 'I agree to Terms & Conditions'")
    public AtbCheckoutPage agreeToTerms() {
        locator(TERMS_CHECKBOX).first().check();
        return this;
    }

    /**
     * WARNING: pays for real from the at-fault's ATB wallet. Waits for Mosadad's
     * payment-success page; fails if the app lands on payment-failure instead.
     */
    @Step("Click 'Confirm' — real payment")
    public PaymentSuccessPage confirmPayment() {
        click(CONFIRM_BTN);
        try {
            page.waitForURL(url -> url.contains(Routes.PAYMENT_SUCCESS) || url.contains(Routes.PAYMENT_FAILURE),
                    new Page.WaitForURLOptions().setTimeout(120000));
        } catch (com.microsoft.playwright.PlaywrightException e) {
            throw new IllegalStateException("No payment result within 120s after Confirm; now on " + currentUrl(), e);
        }
        if (currentUrl().contains(Routes.PAYMENT_FAILURE)) {
            throw new IllegalStateException("Payment Unsuccessful: the app returned to payment-failure");
        }
        return new PaymentSuccessPage(page);
    }
}
