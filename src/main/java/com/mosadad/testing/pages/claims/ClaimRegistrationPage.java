package com.mosadad.testing.pages.claims;

import com.mosadad.testing.pages.BasePage;
import com.microsoft.playwright.Page;

/**
 * STUB — Stage 1: Claim Registration (see MOSADAD_DOMAIN.md §Stage 1).
 *
 * Not yet explored live. Likely reachable from RecoveryClaimsHubPage via
 * "Recovery Claims List" -> a "Create Claim" action, based on the presence
 * of that list screen (/entity-portal/my-claims) confirmed during scaffolding.
 * Selectors below are TODO placeholders — fill them in after walking the
 * real "Create Claim" flow for each of the 3 entry methods, then wire up
 * ClaimRegistrationTest (currently disabled).
 *
 * The Claimant Insurer enters, regardless of entry method:
 *   - Claim number
 *   - Policy details
 *   - Vehicle details
 *   - Accident details
 *   - Official Police Report reference number (mandatory — no valid report,
 *     no recovery)
 *   - Supporting documents
 *
 * Three entry methods:
 *   1. Manual Entry     — insurer types everything directly.
 *   2. Police Data Entry — accident details sourced from an official UAE
 *      police report (Dubai Police, Rafid, Saeed, or other authority).
 *   3. Fast Track        — bulk Excel upload for high-volume simple claims.
 *
 * On submit, the At-Fault insurer is auto-notified (verify via Wallet/
 * notifications bell once at-fault credentials are available).
 */
public class ClaimRegistrationPage extends BasePage {

    // TODO: verify these against the live "Create Claim" screen.
    private static final String CREATE_CLAIM_BUTTON = "TODO-create-claim-button";
    private static final String MANUAL_ENTRY_OPTION = "TODO-manual-entry-option";
    private static final String POLICE_DATA_ENTRY_OPTION = "TODO-police-data-entry-option";
    private static final String FAST_TRACK_OPTION = "TODO-fast-track-option";

    private static final String CLAIM_NUMBER_INPUT = "TODO-claim-number-input";
    private static final String POLICY_NUMBER_INPUT = "TODO-policy-number-input";
    private static final String VEHICLE_PLATE_INPUT = "TODO-vehicle-plate-input";
    private static final String ACCIDENT_DATE_INPUT = "TODO-accident-date-input";
    private static final String POLICE_REPORT_REFERENCE_INPUT = "TODO-police-report-reference-input";
    private static final String SUPPORTING_DOCUMENT_UPLOAD = "TODO-supporting-document-upload";
    private static final String SUBMIT_CLAIM_BUTTON = "TODO-submit-claim-button";

    public ClaimRegistrationPage(Page page) {
        super(page);
    }

    public void startManualEntry() {
        click(CREATE_CLAIM_BUTTON);
        click(MANUAL_ENTRY_OPTION);
    }

    public void startPoliceDataEntry() {
        click(CREATE_CLAIM_BUTTON);
        click(POLICE_DATA_ENTRY_OPTION);
    }

    public void startFastTrack() {
        click(CREATE_CLAIM_BUTTON);
        click(FAST_TRACK_OPTION);
    }

    public void fillClaimNumber(String claimNumber) {
        fill(CLAIM_NUMBER_INPUT, claimNumber);
    }

    public void fillPolicyNumber(String policyNumber) {
        fill(POLICY_NUMBER_INPUT, policyNumber);
    }

    public void fillVehiclePlate(String plate) {
        fill(VEHICLE_PLATE_INPUT, plate);
    }

    public void fillAccidentDate(String date) {
        fill(ACCIDENT_DATE_INPUT, date);
    }

    /** Mandatory per the domain rule: no valid police report reference, no recovery. */
    public void fillPoliceReportReference(String reference) {
        fill(POLICE_REPORT_REFERENCE_INPUT, reference);
    }

    public void uploadSupportingDocument(String filePath) {
        locator(SUPPORTING_DOCUMENT_UPLOAD).setInputFiles(java.nio.file.Paths.get(filePath));
    }

    public void submit() {
        click(SUBMIT_CLAIM_BUTTON);
    }
}
