package com.mosadad.testing.pages.claims;

import com.mosadad.testing.constants.Routes;
import com.mosadad.testing.pages.BasePage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.FilePayload;
import com.microsoft.playwright.options.WaitForSelectorState;
import io.qameta.allure.Step;

public class FastTrackCreateBatchPage extends BasePage {

    private static final String FAULTY_ENTITY_SELECT = "mat-select";
    private static final String CLAIMS_FILE_INPUT = "input[type='file']";

    public FastTrackCreateBatchPage(Page page) {
        super(page);
    }

    public boolean isLoaded() {
        return currentUrl().contains(Routes.FAST_TRACK_CREATE_BATCH);
    }

    @Step("Select Faulty Entity '{0}'")
    public FastTrackCreateBatchPage selectFaultyEntity(String entityName) {
        locator(FAULTY_ENTITY_SELECT).click();
        page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName(entityName).setExact(true)).click();
        waitForOverlayToClose();
        return this;
    }

    @Step("Upload Fast Track claims file: {0}")
    public FastTrackCreateBatchPage uploadClaimsFile(String fileName, String mimeType, byte[] content) {
        locator(CLAIMS_FILE_INPUT).setInputFiles(new FilePayload(fileName, mimeType, content));
        return this;
    }

    /**
     * On a rejected sheet the page stays put and lists the problems above a
     * "Download error file" button, e.g. "Required column 'claim_number' is missing".
     */
    @Step("Click 'Validate And Create Batch'")
    public FastTrackClaimsListPage validateAndCreateBatch() {
        getLocatorByRole(AriaRole.BUTTON, "Validate And Create Batch").click();
        try {
            page.waitForURL(url -> url.contains(Routes.FAST_TRACK_CLAIMS_LIST),
                    new Page.WaitForURLOptions().setTimeout(20000));
        } catch (com.microsoft.playwright.PlaywrightException e) {
            String body = locator("body").innerText();
            int error = body.indexOf("Error:");
            String reason = error >= 0 ? body.substring(error, Math.min(body.length(), error + 800)) : "no error shown";
            throw new IllegalStateException("Fast Track batch was not created — " + reason, e);
        }
        return new FastTrackClaimsListPage(page);
    }

    private void waitForOverlayToClose() {
        page.keyboard().press("Escape");
        page.locator(".cdk-overlay-backdrop").waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.DETACHED)
                .setTimeout(5000));
    }
}
