package com.mosadad.testing.pages.claims;

import com.mosadad.testing.constants.Routes;
import com.mosadad.testing.pages.BasePage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.FilePayload;
import io.qameta.allure.Step;

/**
 * At-fault "Update And Re-upload Batch" screen (⋮ → Upload Excel). Not yet
 * verified live: the heading, file input and button names come from the manual
 * test steps. Re-check them on the first run.
 */
public class FastTrackReuploadBatchPage extends BasePage {

    private static final String FILE_INPUT = "input[type='file']";

    public FastTrackReuploadBatchPage(Page page) {
        super(page);
    }

    public boolean isLoaded() {
        try {
            page.getByText("Update And Re-upload Batch").first()
                    .waitFor(new Locator.WaitForOptions().setTimeout(15000));
            return true;
        } catch (com.microsoft.playwright.PlaywrightException e) {
            log.debug("Update And Re-upload Batch screen did not show in time: {}", e.getMessage());
            return false;
        }
    }

    @Step("Upload the filled at-fault sheet: {0}")
    public FastTrackReuploadBatchPage uploadSheet(String fileName, String mimeType, byte[] content) {
        locator(FILE_INPUT).first().setInputFiles(new FilePayload(fileName, mimeType, content));
        return this;
    }

    @Step("Click 'Validate And Re-upload'")
    public FastTrackClaimsListPage validateAndReupload() {
        getLocatorByRole(AriaRole.BUTTON, "Validate And Re-upload").click();
        page.waitForURL(url -> url.contains(Routes.FAST_TRACK_CLAIMS_LIST),
                new Page.WaitForURLOptions().setTimeout(20000));
        return new FastTrackClaimsListPage(page);
    }
}
