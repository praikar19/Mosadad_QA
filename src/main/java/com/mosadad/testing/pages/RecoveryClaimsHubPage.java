package com.mosadad.testing.pages;

import com.mosadad.testing.constants.Routes;
import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

public class RecoveryClaimsHubPage extends BasePage {

    private static final String NAV_LINK = "a.nav-link";

    public RecoveryClaimsHubPage(Page page) {
        super(page);
    }

    public boolean isLoaded() {
        return currentUrl().contains(Routes.RECOVERY_CLAIMS_HUB);
    }

    @Step("Open '{0}' module")
    private void openModuleLink(String linkText) {
        page.locator(NAV_LINK, new Page.LocatorOptions().setHasText(linkText)).click();
        page.waitForURL(url -> !url.contains(Routes.RECOVERY_CLAIMS_HUB),
                new Page.WaitForURLOptions().setTimeout(10000));
    }

    public ClaimsReportPage openClaimsReport() {
        openModuleLink("Claims Report");
        return new ClaimsReportPage(page);
    }

    public SlaViolationPage openSlaViolation() {
        openModuleLink("SLA Violation");
        return new SlaViolationPage(page);
    }

    public void openPotentialRecoveryClaims() {
        openModuleLink("Potential Recovery Claims");
    }

    public RecoveryClaimsListPage openRecoveryClaimsList() {
        openModuleLink("Recovery Claims List");
        return new RecoveryClaimsListPage(page);
    }

    public FastTrackPage openFastTrack() {
        openModuleLink("Fast Track");
        return new FastTrackPage(page);
    }

    public BulkSettlementPage openBulkSettlement() {
        openModuleLink("Bulk Settlement");
        return new BulkSettlementPage(page);
    }

    public DueAmountPage openDueAmount() {
        openModuleLink("Due Amount");
        return new DueAmountPage(page);
    }

    public PaymentHistoryPage openPaymentHistory() {
        openModuleLink("Payment History");
        return new PaymentHistoryPage(page);
    }

}
