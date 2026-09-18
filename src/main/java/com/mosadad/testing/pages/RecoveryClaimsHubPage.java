package com.mosadad.testing.pages;

import com.mosadad.testing.constants.Routes;
import com.microsoft.playwright.Page;
import com.mosadad.testing.pages.claims.PotentialRecoveryClaimsListPage;

/**
 * /entity-landing/recovery-claims — verified live 2026-08-30. Three module
 * cards, each containing real nav-link anchors (hrefs captured via DOM
 * inspection, not guessed):
 *
 *   Dashboard              -> Claims Report ({@link Routes#CLAIMS_REPORT}),
 *                              SLA Violation ({@link Routes#SLA_VIOLATION})
 *   Recovery Claim Records -> Potential Recovery Claims, Recovery Claims List,
 *                              Fast Track
 *   Financial               -> Bulk Settlement, Due Amount, Payment History
 */
public class RecoveryClaimsHubPage extends BasePage {

    private static final String NAV_LINK = "a.nav-link";

    public RecoveryClaimsHubPage(Page page) {
        super(page);
    }

    public boolean isLoaded() {
        return currentUrl().contains(Routes.RECOVERY_CLAIMS_HUB);
    }

    /** Clicks a module link by its exact visible text, e.g. "Recovery Claims List", and waits for the SPA route change. */
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

    public PotentialRecoveryClaimsListPage openPotentialRecoveryClaimsListPage() {
        openModuleLink("Potential Recovery Claims");
        return new PotentialRecoveryClaimsListPage(page);
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
