package com.mosadad.testing.pages.claims;

import com.mosadad.testing.constants.Routes;
import com.mosadad.testing.pages.BasePage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

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

    public void selectReportProvider(String providerName) {
        locator(REPORT_PROVIDER_SELECT).selectOption(providerName);
    }

    public void uploadPoliceReport(String filePath) {
        locator(POLICE_REPORT_FILE_INPUT).setInputFiles(Paths.get(filePath));
    }

    /** Advances Step 1 -> Step 2 without a police report — the pure Manual Entry path. */
    public CreateManualRecoveryClaimPage skipPoliceReportUpload() {
        getLocatorByRole(AriaRole.BUTTON, "Skip").click();
        return this;
    }

    /** Advances Step 1 -> Step 2 after selectReportProvider()/uploadPoliceReport() — the Police Data Entry path. */
    public CreateManualRecoveryClaimPage submitPoliceReportStep() {
        getLocatorByRole(AriaRole.BUTTON, "Submit").click();
        return this;
    }

    public CreateManualClaimPage continueProcessingPoliceReportPopup() {
        getLocatorByRole(AriaRole.BUTTON, "Continue").click();
        return new CreateManualClaimPage(page);
    }

    public CreateManualRecoveryClaimPage cancelProcessingPoliceReportPopup() {
        getLocatorByRole(AriaRole.BUTTON, "Cancel").click();
        return this;
    }

    public CreateManualRecoveryClaimPage closeProcessingPoliceReportPopup() {
        getLocatorByRole(AriaRole.BUTTON, "×").click();
        return this;
    }

}
