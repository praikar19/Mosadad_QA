package com.mosadad.testing.pages.claims;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.mosadad.testing.constants.Routes;
import com.mosadad.testing.pages.BasePage;

import java.nio.file.Paths;

public class CreateManualClaimPage extends BasePage {

    public CreateManualClaimPage(Page page) {
        super(page);
    }

    public boolean isLoaded() {
        return currentUrl().contains(Routes.CREATE_MANUAL_CLAIMS);
    }

    /* ── Accident Details — every id here is unique on the page ─────────── */

    private static final String REPORT_NUMBER = "reportNumber";
    private static final String REPORT_NUMBER_SEARCH_BTN = "button.btn.btn-primary.d-flex";
    private static final String ACCIDENT_DATE = "actualAccidentTime";                 // text input, DD/MM/YYYY
    private static final String ACCIDENT_TIME = "input[type='time']";                 // no id; only one on the page
    private static final String ACCIDENT_TYPE = "accidentType";                       // mat-select
    private static final String EMIRATE = "emirateCode";                              // mat-select
    private static final String CITY = "cityCode";                                    // mat-select
    private static final String AREA = "areaCode";                                    // mat-select
    private static final String INTERSECTION = "intersection";
    private static final String STREET = "street";
    private static final String REPORT_DATE = "reportDate";                           // text input, DD/MM/YYYY
    private static final String ACCIDENT_SOURCE = "accidentSource";                   // mat-select
    private static final String ACCIDENT_DESCRIPTION = "textarea[placeholder='Accident Description']";

    /* ── Claimant Details / At-fault Details — shared field-name constants,
     * always resolved through claimantSection()/atFaultSection() below.
     */
    private static final String INSURANCE_COMPANY = "insuranceCompanyId";     // mat-select — Claimant's is disabled/prefilled
    private static final String PLATE_SOURCE = "plateSource";                 // mat-select
    private static final String PLATE_COLOR = "plateColor";                   // mat-select
    private static final String PLATE_NUMBER = "plateNumber";
    private static final String POLICY_NUMBER = "policyNumber";
    private static final String POLICY_EXPIRY_DATE = "policyExpirationDate";
    private static final String POLICY_TYPE = "insuranceType";                // mat-select
    private static final String CHASSIS_NUMBER = "chassisNumber";
    private static final String VEHICLE_MAKE = "vehicleBrandId";              // mat-select
    private static final String VEHICLE_MODEL = "vehicleModelId";             // mat-select
    private static final String VEHICLE_YEAR = "manufactureYear";
    private static final String DAMAGED_PARTS = "damagedPart";

    /** Claimant Details only — real id, not duplicated in At-fault Details. */
    private static final String CLAIMANT_CLAIM_NUMBER = "claimantClaimNumber";

    public void fillReportNumber(String value)        { getLocatorById(REPORT_NUMBER).fill(value); }
    public void clickReportNumberSearch()              { locator(REPORT_NUMBER_SEARCH_BTN).click(); }
    /**
     * Despite the name (kept for compatibility with existing callers), this
     * does NOT click "Today" — per live observation 2026-09-04, this field's
     * "Today" quick-link doesn't work: clicking it closes the calendar
     * without error but leaves the field empty (confirmed via DOM
     * inspection — flagged as an app bug to raise with the dev team, not an
     * automation issue). Picks a real past date instead — see
     * pickPastDate() Javadoc.
     */
    public void pickAccidentDateToday()                { pickPastDate(datepickerToggle(ACCIDENT_DATE), 2); }
    public void fillAccidentTime(String hhMm)          { locator(ACCIDENT_TIME).fill(hhMm); }
    public void selectAccidentType(String optionText)  { openMatSelect(ACCIDENT_TYPE, optionText); }
    public void selectEmirate(String optionText)       { openMatSelect(EMIRATE, optionText); }
    public void selectCity(String optionText)          { openMatSelect(CITY, optionText); }
    public void selectArea(String optionText)          { openMatSelect(AREA, optionText); }
    public void fillIntersection(String value)         { getLocatorById(INTERSECTION).fill(value); }
    public void fillStreet(String value)               { getLocatorById(STREET).fill(value); }
    /**
     * Despite the name (kept for compatibility with existing callers), this
     * does NOT click "Today" either — same app-bug finding as
     * pickAccidentDateToday() Javadoc, confirmed live 2026-09-04 for this
     * field too. Picks a real past date instead — see pickPastDate()
     * Javadoc.
     */
    public void pickReportDateToday()                  { pickPastDate(datepickerToggle(REPORT_DATE), 2); }
    public void selectAccidentSource(String optionText) { openMatSelect(ACCIDENT_SOURCE, optionText); }
    public void fillAccidentDescription(String value)  { locator(ACCIDENT_DESCRIPTION).fill(value); }

    private Locator claimantSection() {
        return locator("app-expandable-card").filter(new Locator.FilterOptions().setHasText("Claimant Details"));
    }

    private Locator atFaultSection() {
        return locator("app-expandable-card").filter(new Locator.FilterOptions().setHasText("At-fault Details"));
    }

    public void fillClaimantClaimNumber(String value)         { getLocatorById(CLAIMANT_CLAIM_NUMBER).fill(value); }
    public void fillClaimantPlateNumber(String value)         { claimantSection().locator("#" + PLATE_NUMBER).fill(value); }
    public void fillClaimantPolicyNumber(String value)        { claimantSection().locator("#" + POLICY_NUMBER).fill(value); }
    /**
     * Despite the name (kept for compatibility with existing callers), this
     * does NOT pick today — confirmed live 2026-09-03: the API rejects an
     * expiry date equal to the accident date with "Policy expired before
     * the accident date." Picks a real near-future date instead. See
     * pickFutureDate() Javadoc for the same-month limitation.
     */
    public void pickClaimantPolicyExpiryDateToday()           { pickFutureDate(claimantSection().locator(".form-group:has(#" + POLICY_EXPIRY_DATE + ") button"), 7); }
    public void fillClaimantChassisNumber(String value)       { claimantSection().locator("#" + CHASSIS_NUMBER).fill(value); }
    public void fillClaimantVehicleYear(String value)         { claimantSection().locator("#" + VEHICLE_YEAR).fill(value); }
    public void selectClaimantPlateSource(String optionText)  { openMatSelect(claimantSection(), PLATE_SOURCE, optionText); }
    public void selectClaimantPlateColor(String optionText)   { openMatSelect(claimantSection(), PLATE_COLOR, optionText); }
    public void selectClaimantPolicyType(String optionText)   { openMatSelect(claimantSection(), POLICY_TYPE, optionText); }
    public void selectClaimantVehicleMake(String optionText)  { openMatSelect(claimantSection(), VEHICLE_MAKE, optionText); }
    public void selectClaimantVehicleModel(String optionText) { openMatSelect(claimantSection(), VEHICLE_MODEL, optionText); }
    public void selectClaimantDamagedParts(String optionText) { openMatSelect(claimantSection(), DAMAGED_PARTS, optionText); }
    // Claimant's Insurance Company is disabled/prefilled with the logged-in insurer (verified live) — no setter.

    public void selectAtFaultInsuranceCompany(String optionText) { openMatSelect(atFaultSection(), INSURANCE_COMPANY, optionText); }
    public void fillAtFaultPlateNumber(String value)              { atFaultSection().locator("#" + PLATE_NUMBER).fill(value); }
    public void fillAtFaultPolicyNumber(String value)             { atFaultSection().locator("#" + POLICY_NUMBER).fill(value); }
    /**
     * Despite the name (kept for compatibility with existing callers), this
     * does NOT pick today — see pickClaimantPolicyExpiryDateToday() Javadoc.
     */
    public void pickAtFaultPolicyExpiryDateToday()                { pickFutureDate(atFaultSection().locator(".form-group:has(#" + POLICY_EXPIRY_DATE + ") button"), 7); }
    public void fillAtFaultChassisNumber(String value)            { atFaultSection().locator("#" + CHASSIS_NUMBER).fill(value); }
    public void fillAtFaultVehicleYear(String value)              { atFaultSection().locator("#" + VEHICLE_YEAR).fill(value); }
    public void selectAtFaultPlateSource(String optionText)       { openMatSelect(atFaultSection(), PLATE_SOURCE, optionText); }
    public void selectAtFaultPlateColor(String optionText)        { openMatSelect(atFaultSection(), PLATE_COLOR, optionText); }
    public void selectAtFaultPolicyType(String optionText)        { openMatSelect(atFaultSection(), POLICY_TYPE, optionText); }
    public void selectAtFaultVehicleMake(String optionText)       { openMatSelect(atFaultSection(), VEHICLE_MAKE, optionText); }
    public void selectAtFaultVehicleModel(String optionText)      { openMatSelect(atFaultSection(), VEHICLE_MODEL, optionText); }
    public void selectAtFaultDamagedParts(String optionText)      { openMatSelect(atFaultSection(), DAMAGED_PARTS, optionText); }

    /* ── Attachments Uploaders — see class Javadoc re: dynamic file input ids ─ */

    private static final String CLAIM_DOCUMENTS_UPLOADER =
            "app-file-uploader:has-text('Upload Claim Documents') input[type='file']";
    private static final String OTHER_DOCUMENTS_UPLOADER =
            "app-file-uploader:has-text('Upload Other Documents') input[type='file']";

    public void uploadClaimDocuments(String filePath) {
        locator(CLAIM_DOCUMENTS_UPLOADER).setInputFiles(Paths.get(filePath));
    }

    public void uploadOtherDocuments(String filePath) {
        locator(OTHER_DOCUMENTS_UPLOADER).setInputFiles(Paths.get(filePath));
    }

    /* ── Final actions ─────────────────────────────────────────────────── */

    public void saveAsDraftBtnClick() { getLocatorByRole(AriaRole.BUTTON, "Save as Draft").click(); }
    public void cancelBtnClick()      { getLocatorByRole(AriaRole.BUTTON, "Cancel").click(); }
    public void resetClaimBtnClick()  { getLocatorByRole(AriaRole.BUTTON, "Reset Claim").click(); }

    /**
     * On success the app navigates to /entity-portal/recovery-claim/{id}
     * (verified live 2026-09-03). On a server-side validation error (e.g. a
     * duplicate claim number, or an expiry date not after the accident
     * date — both confirmed live) it shows an error toast and stays on
     * this same page instead. On a client-side validation error (a
     * required field left invalid — Angular reactive forms block the
     * submit handler before any request is sent, confirmed live 2026-09-04)
     * no toast appears at all, since the app never got as far as calling
     * the API. If the navigation doesn't happen, this checks for a toast
     * first and, failing that, reports which required fields are still
     * marked invalid ("ng-invalid"), so a failure here says why instead of
     * just "timed out".
     */
    public OpenRecoveryClaimsPage createBtnClick() {
        getLocatorByRole(AriaRole.BUTTON, "Create").click();
        try {
            page.waitForURL(url -> url.contains(Routes.OPEN_RECOVERY_CLAIM),
                    new Page.WaitForURLOptions().setTimeout(15000));
        } catch (com.microsoft.playwright.PlaywrightException timeout) {
            String toastText = (String) page.evaluate(
                    "document.querySelector('.toast-container') ? document.querySelector('.toast-container').innerText : null");
            String reason;
            if (toastText != null && !toastText.isBlank()) {
                reason = " — app said: \"" + toastText.trim() + "\"";
            } else {
                Object invalidFieldIds = page.evaluate(
                        "Array.from(document.querySelectorAll('input.ng-invalid,mat-select.ng-invalid,textarea.ng-invalid'))"
                                + ".map(e => e.id || e.tagName)");
                reason = " — no error toast was shown either; still-invalid required fields: " + invalidFieldIds;
            }
            throw new RuntimeException("Create did not navigate to the claim summary page" + reason, timeout);
        }
        return new OpenRecoveryClaimsPage(page);
    }

    /**
     * Opens an unscoped (Accident Details) mat-select by id and picks the
     * option by its visible text. See class Javadoc — the option list is
     * always in the global CDK overlay, located from the page root.
     */
    private void openMatSelect(String matSelectId, String optionText) {
        getLocatorById(matSelectId).click();
        page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName(optionText).setExact(true)).click();
        waitForOverlayToClose();
    }

    /** Same as above, but the mat-select trigger itself is scoped (Claimant/At-fault Details). */
    private void openMatSelect(Locator scopedSection, String matSelectId, String optionText) {
        scopedSection.locator("#" + matSelectId).click();
        page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName(optionText).setExact(true)).click();
        waitForOverlayToClose();
    }

    /**
     * Angular Material's CDK overlay (mat-select panel, datepicker) animates
     * closed instead of detaching instantly — confirmed live: opening the
     * next mat-select/datepicker while the previous overlay/backdrop is
     * still mid-close throws "subtree intercepts pointer events", and if
     * nothing ever waits for it, the leftover backdrop can silently block
     * clicks anywhere on the page for the rest of the form (confirmed live
     * 2026-09-04 — a report-date pickToday() left its backdrop open, and
     * the claimant policy expiry datepicker several fields later timed out
     * clicking through it). Called by every overlay-opening interaction on
     * this page (mat-select, pickToday(), pickFutureDate()) right after
     * making its selection, so no caller can leave one behind.
     *
     * Damaged Part is a multi-select mat-select (options render with the
     * "mat-mdc-option-multiple" class, confirmed live) — picking one option
     * does NOT auto-close the panel the way a single-select does, so the
     * backdrop never detaches on its own. Escape closes either kind, so
     * pressing it first makes this wait work for both.
     */
    private void waitForOverlayToClose() {
        page.keyboard().press("Escape");
        page.locator(".cdk-overlay-backdrop").waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.DETACHED)
                .setTimeout(5000));
    }

    /** The one calendar-toggle button inside an unscoped date field's .form-group — confirmed exactly one per field live. */
    private Locator datepickerToggle(String dateFieldId) {
        return locator(".form-group:has(#" + dateFieldId + ") button");
    }

    /** Opens the calendar via the given toggle and clicks "Today" — the only verified date-picker interaction (see class Javadoc). */
    private void pickToday(Locator calendarToggle) {
        calendarToggle.click();
        page.locator(".cdk-overlay-container").getByText("Today").click();
        waitForOverlayToClose();
    }

    /**
     * Opens the calendar via the given toggle and clicks the day
     * daysFromToday days ahead, by its "DD/MM/YYYY" aria-label (confirmed
     * live 2026-09-03 — day cells: button.mat-calendar-body-cell[aria-label]).
     * No month-navigation is implemented (same TODO as the class Javadoc),
     * so this only works when the target date falls in the currently
     * displayed (current) month — keep daysFromToday small (verified with 7).
     */
    private void pickFutureDate(Locator calendarToggle, int daysFromToday) {
        calendarToggle.click();
        String targetDate = java.time.LocalDate.now().plusDays(daysFromToday)
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        page.locator(".cdk-overlay-container button.mat-calendar-body-cell[aria-label='" + targetDate + "']").click();
        waitForOverlayToClose();
    }

    /**
     * Opens the calendar via the given toggle and clicks the day
     * daysBeforeToday days behind, by its "DD/MM/YYYY" aria-label — same
     * day-cell mechanism as pickFutureDate(), just subtracting instead of
     * adding. Same same-month limitation as pickFutureDate() too: no
     * month-navigation, so keep daysBeforeToday small.
     *
     * Added to work around a live app finding, not an automation bug: per
     * live observation 2026-09-04, pickToday() — clicking the calendar's
     * own "Today" quick-link — does not work for the Accident Date and
     * Report Date fields specifically (the click completes with no error,
     * but the field's value stays empty), while the exact same pickToday()
     * code works correctly for other fields. Flagged to raise with the
     * app's dev team; pickAccidentDateToday()/pickReportDateToday() use
     * this method instead so claim-creation tests aren't blocked on it.
     */
    private void pickPastDate(Locator calendarToggle, int daysBeforeToday) {
        calendarToggle.click();
        String targetDate = java.time.LocalDate.now().minusDays(daysBeforeToday)
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        page.locator(".cdk-overlay-container button.mat-calendar-body-cell[aria-label='" + targetDate + "']").click();
        waitForOverlayToClose();
    }

    /**
     * Fills every section of the form from one ManualClaimDetails. Each
     * field is only touched when it's set (non-null String, or true for the
     * date-today booleans) — an unset field is left alone rather than
     * cleared, so a caller can build a partial ManualClaimDetails for tests
     * that only care about one section.
     */
    public void enterAllRecoveryClaimsDetails(ManualClaimDetails details) {
        if (details.getReportNumber() != null)        fillReportNumber(details.getReportNumber());
        if (details.isAccidentDateToday())             pickAccidentDateToday();
        if (details.getAccidentTime() != null)         fillAccidentTime(details.getAccidentTime());
        if (details.getAccidentType() != null)         selectAccidentType(details.getAccidentType());
        if (details.getEmirate() != null)              selectEmirate(details.getEmirate());
        if (details.getCity() != null)                 selectCity(details.getCity());
        if (details.getArea() != null)                 selectArea(details.getArea());
        if (details.getIntersection() != null)         fillIntersection(details.getIntersection());
        if (details.getStreet() != null)               fillStreet(details.getStreet());
        if (details.isReportDateToday())                pickReportDateToday();
        if (details.getAccidentSource() != null)       selectAccidentSource(details.getAccidentSource());
        if (details.getAccidentDescription() != null)  fillAccidentDescription(details.getAccidentDescription());

        if (details.getClaimantClaimNumber() != null)  fillClaimantClaimNumber(details.getClaimantClaimNumber());
        if (details.getClaimantPlateNumber() != null)  fillClaimantPlateNumber(details.getClaimantPlateNumber());
        if (details.getClaimantPolicyNumber() != null) fillClaimantPolicyNumber(details.getClaimantPolicyNumber());
        if (details.isClaimantPolicyExpiryDateToday())  pickClaimantPolicyExpiryDateToday();
        if (details.getClaimantChassisNumber() != null) fillClaimantChassisNumber(details.getClaimantChassisNumber());
        if (details.getClaimantVehicleYear() != null)  fillClaimantVehicleYear(details.getClaimantVehicleYear());
        if (details.getClaimantPlateSource() != null)  selectClaimantPlateSource(details.getClaimantPlateSource());
        if (details.getClaimantPlateColor() != null)   selectClaimantPlateColor(details.getClaimantPlateColor());
        if (details.getClaimantPolicyType() != null)   selectClaimantPolicyType(details.getClaimantPolicyType());
        if (details.getClaimantVehicleMake() != null)  selectClaimantVehicleMake(details.getClaimantVehicleMake());
        if (details.getClaimantVehicleModel() != null) selectClaimantVehicleModel(details.getClaimantVehicleModel());
        if (details.getClaimantDamagedParts() != null) selectClaimantDamagedParts(details.getClaimantDamagedParts());

        if (details.getAtFaultInsuranceCompany() != null) selectAtFaultInsuranceCompany(details.getAtFaultInsuranceCompany());
        if (details.getAtFaultPlateNumber() != null)   fillAtFaultPlateNumber(details.getAtFaultPlateNumber());
        if (details.getAtFaultPolicyNumber() != null)  fillAtFaultPolicyNumber(details.getAtFaultPolicyNumber());
        if (details.isAtFaultPolicyExpiryDateToday())   pickAtFaultPolicyExpiryDateToday();
        if (details.getAtFaultChassisNumber() != null) fillAtFaultChassisNumber(details.getAtFaultChassisNumber());
        if (details.getAtFaultVehicleYear() != null)   fillAtFaultVehicleYear(details.getAtFaultVehicleYear());
        if (details.getAtFaultPlateSource() != null)   selectAtFaultPlateSource(details.getAtFaultPlateSource());
        if (details.getAtFaultPlateColor() != null)    selectAtFaultPlateColor(details.getAtFaultPlateColor());
        if (details.getAtFaultPolicyType() != null)    selectAtFaultPolicyType(details.getAtFaultPolicyType());
        if (details.getAtFaultVehicleMake() != null)   selectAtFaultVehicleMake(details.getAtFaultVehicleMake());
        if (details.getAtFaultVehicleModel() != null)  selectAtFaultVehicleModel(details.getAtFaultVehicleModel());
        if (details.getAtFaultDamagedParts() != null)  selectAtFaultDamagedParts(details.getAtFaultDamagedParts());

        if (details.getClaimDocumentsFilePath() != null) uploadClaimDocuments(details.getClaimDocumentsFilePath());
        if (details.getOtherDocumentsFilePath() != null) uploadOtherDocuments(details.getOtherDocumentsFilePath());
    }

}
