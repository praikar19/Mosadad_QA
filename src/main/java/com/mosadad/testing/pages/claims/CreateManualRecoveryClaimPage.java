package com.mosadad.testing.pages.claims;

import com.mosadad.testing.constants.Routes;
import com.mosadad.testing.pages.BasePage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.FilePayload;
import io.qameta.allure.Step;

import java.nio.file.Paths;

/**
 * /entity-portal/manual-claim — Create Manual Recovery Claim (Stage 1,
 * Manual Entry — see MOSADAD_DOMAIN.md §Stage 1). Verified live 2026-09-03
 * as the Claimant Insurer (Dubai stage account), reached via
 * PotentialRecoveryClaimsListPage.clickCreateNewRecoveryClaimBtn().
 *
 * Two screens in one flow, both handled by this page object:
 *   1. Police report provider + optional police report upload, with
 *      Skip/Submit. (MOSADAD_DOMAIN.md's "Police Data Entry" method reuses
 *      this same gate; choosing a provider + uploading is that path,
 *      Skip is the pure Manual Entry path.)
 *   2. The claim form itself: Accident Details, Claimant Details,
 *      At-fault Details, Attachments Uploaders, then Save as Draft /
 *      Cancel / Reset Claim / Create.
 *
 * IMPORTANT — duplicate element ids: Claimant Details and At-fault Details
 * are visually identical vehicle/insurance sub-forms, and the app reuses
 * the exact same ids for both (plateNumber, policyNumber,
 * policyExpirationDate, chassisNumber, manufactureYear, insuranceCompanyId,
 * plateSource, plateColor, insuranceType, vehicleBrandId, vehicleModelId,
 * damagedPart all appear twice in the DOM — confirmed live). A bare
 * page.locator("#id") on any of those matches BOTH elements and throws a
 * Playwright strict-mode violation the moment an action is called on it.
 * Every such field is resolved through claimantSection()/atFaultSection()
 * below, never as a bare id. Only reportNumber, actualAccidentTime,
 * intersection, street, reportDate, accidentType, emirateCode, cityCode,
 * areaCode, accidentSource (all Accident Details), and
 * claimantClaimNumber (Claimant Details only) are unique on the page.
 *
 * Angular Material <mat-select> fields are not native <select> elements —
 * clicking one opens a <mat-option> list rendered into a global CDK overlay
 * appended to document.body (confirmed live via DOM inspection), not
 * nested under the mat-select itself. Options must therefore always be
 * located from the page root (see openMatSelect()), never scoped under a
 * section locator, even when the mat-select trigger itself is scoped.
 *
 * Attachments Uploaders' <input type="file"> ids are dynamic GUIDs
 * regenerated on every page load (e.g. "fileInput2b84a519f1ed4870ae08fe70
 * 7c734c8f") — confirmed live across reloads — so they are never
 * hardcoded; both file inputs are instead scoped by their uploader's own
 * stable visible label text ("Upload Claim Documents" / "Upload Other
 * Documents").
 *
 * All 4 date fields (actualAccidentTime, reportDate, and both
 * policyExpirationDate instances) are readonly Angular Material Datepicker
 * inputs, confirmed live — page.locator(id).fill() throws (TimeoutError:
 * "element is not editable"), it does not silently no-op. They only accept
 * input through the calendar picker UI: click the field's lone toggle
 * button (scoped via ".form-group:has(#id) button", confirmed exactly one
 * such button per field), which opens a <mat-calendar> in the same CDK
 * overlay as mat-select. Only "select today" is implemented and verified
 * live (click the picker's own "Today" link) — picking an arbitrary past
 * date requires clicking a day cell by its aria-label ("DD/MM/YYYY") and,
 * if that date isn't in the currently-displayed month, navigating via
 * .mat-calendar-previous-button/.mat-calendar-next-button first; that
 * month-navigation logic is NOT implemented here (TODO) — don't assume it
 * works, it hasn't been written or verified.
 */
public class CreateManualRecoveryClaimPage extends BasePage {

    public CreateManualRecoveryClaimPage(Page page) {
        super(page);
    }

    public boolean isLoaded() {
        return currentUrl().contains(Routes.CREATE_MANUAL_RECOVERY_CLAIMS);
    }

    /* ── Step 1 — police report provider gate ─────────────────────────── */

    /** Native &lt;select&gt;, options: "Select Provider" (placeholder), "Saaed", "Dubai", "Rafid". */
    private static final String REPORT_PROVIDER_SELECT = "select.report-provider-select";
    private static final String POLICE_REPORT_FILE_INPUT = "input[type='file']";

    @Step("Select report provider '{0}'")
    public void selectReportProvider(String providerName) {
        locator(REPORT_PROVIDER_SELECT).selectOption(providerName);
    }

    @Step("Upload police report file: {0}")
    public void uploadPoliceReport(String filePath) {
        locator(POLICE_REPORT_FILE_INPUT).setInputFiles(Paths.get(filePath));
    }

    /**
     * Same upload, but from in-memory bytes rather than a file already on
     * disk — lets negative tests (oversized file, unsupported format) build
     * their fixture inline instead of committing binary files to the repo.
     */
    // {2} (content) is deliberately left out of the step label — Allure
    // interpolates it via byte[].toString(), which is a meaningless object
    // reference (e.g. "[B@6f94fa3e"), not the byte count.
    @Step("Upload police report file: {0} ({1})")
    public void uploadPoliceReport(String fileName, String mimeType, byte[] content) {
        locator(POLICE_REPORT_FILE_INPUT).setInputFiles(new FilePayload(fileName, mimeType, content));
    }

    /**
     * Client-side upload validation message — confirmed live 2026-09-21 for
     * an oversized file ("Error: File size exceeds the maximum limit of 10
     * MB."). Matched by its "Error:" prefix rather than a guessed CSS class:
     * that prefix is the only part confirmed to also cover other upload
     * validation failures (e.g. an unsupported file format) — exact wording
     * for those hasn't been captured live yet.
     */
    private static final String UPLOAD_ERROR_PREFIX = "Error:";

    @Step("Check whether an upload validation error is visible")
    public boolean isFileUploadErrorVisible() {
        try {
            getLocatorByText(UPLOAD_ERROR_PREFIX).first().waitFor(new Locator.WaitForOptions().setTimeout(5000));
        } catch (com.microsoft.playwright.PlaywrightException timeout) {
            log.debug("Upload error message did not become visible in time: {}", timeout.getMessage());
            return false;
        }
        return true;
    }

    public String getFileUploadErrorMessage() {
        return getLocatorByText(UPLOAD_ERROR_PREFIX).first().innerText();
    }

    /** "Processing Police Report" confirmation dialog shown on Submit (screenshot-verified 2026-09-21). */
    private static final String PROCESSING_POPUP_HEADING = "text=Processing Police Report";

    @Step("Check whether the 'Processing Police Report' confirmation popup is visible")
    public boolean isProcessingPopupVisible() {
        try {
            locator(PROCESSING_POPUP_HEADING).waitFor(new Locator.WaitForOptions().setTimeout(10000));
        } catch (com.microsoft.playwright.PlaywrightException timeout) {
            log.debug("Processing Police Report popup did not become visible in time: {}", timeout.getMessage());
            return false;
        }
        return true;
    }

    /** Advances Step 1 -> Step 2 without a police report — the pure Manual Entry path. */
    @Step("Click 'Skip' (proceed without a police report)")
    public CreateManualRecoveryClaimPage skipPoliceReportUpload() {
        getLocatorByRole(AriaRole.BUTTON, "Skip").click();
        return this;
    }

    /** Advances Step 1 -> Step 2 after selectReportProvider()/uploadPoliceReport() — the Police Data Entry path. */
    @Step("Click 'Submit' (police report step)")
    public CreateManualRecoveryClaimPage submitPoliceReportStep() {
        getLocatorByRole(AriaRole.BUTTON, "Submit").click();
        return this;
    }

    @Step("Click 'Continue' on the Processing Police Report popup")
    public CreateManualClaimPage continueProcessingPoliceReportPopup() {
        getLocatorByRole(AriaRole.BUTTON, "Continue").click();
        return new CreateManualClaimPage(page);
    }

    @Step("Click 'Cancel' on the Processing Police Report popup")
    public CreateManualRecoveryClaimPage cancelProcessingPoliceReportPopup() {
        getLocatorByRole(AriaRole.BUTTON, "Cancel").click();
        return this;
    }

    @Step("Close the Processing Police Report popup")
    public CreateManualRecoveryClaimPage closeProcessingPoliceReportPopup() {
        getLocatorByRole(AriaRole.BUTTON, "×").click();
        return this;
    }

}
