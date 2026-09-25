package com.mosadad.testing.pages;

import com.mosadad.testing.constants.Routes;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * /entity-portal/payment-success?transactionId=… — heading "Payment Was
 * Successful", the transaction ID, and "Return To Home Screen". From the app's
 * code (2026-09-24): that button is what reports the payment to Mosadad
 * (confirmPayment for each paid claim) before going to Recovery Claims, so the
 * claims only move to "Payment Successful" once it has been clicked.
 */
public class PaymentSuccessPage extends BasePage {

    public static final String SUCCESS_HEADING = "Payment Was Successful";
    private static final String RETURN_HOME_BTN = "button:text-is('Return To Home Screen')";

    public PaymentSuccessPage(Page page) {
        super(page);
    }

    public boolean isLoaded() {
        return currentUrl().contains(Routes.PAYMENT_SUCCESS);
    }

    public String getHeading() {
        Locator heading = locator("h1");
        heading.first().waitFor(new Locator.WaitForOptions().setTimeout(15000));
        return heading.first().innerText().trim();
    }

    /** From the page URL, where the app puts it; "" if missing. */
    public String getTransactionId() {
        Matcher m = Pattern.compile("[?&]transactionId=([^&#]+)").matcher(currentUrl());
        return m.find() ? URLDecoder.decode(m.group(1), StandardCharsets.UTF_8) : "";
    }

    public byte[] screenshot() {
        return page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
    }

    /** Reports the payment to Mosadad, then lands on Recovery Claims. */
    @Step("Click 'Return To Home Screen'")
    public void returnToHomeScreen() {
        click(RETURN_HOME_BTN);
        page.waitForURL(url -> url.contains(Routes.RECOVERY_CLAIMS_HUB), new Page.WaitForURLOptions().setTimeout(60000));
    }
}
