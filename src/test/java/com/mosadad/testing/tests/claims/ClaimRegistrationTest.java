package com.mosadad.testing.tests.claims;

import com.mosadad.testing.base.BaseUiTest;
import com.mosadad.testing.pages.claims.ClaimRegistrationPage;
import io.qameta.allure.*;
import org.testng.annotations.Test;

/**
 * STUB SUITE — Stage 1: Claim Registration (MOSADAD_DOMAIN.md §Stage 1).
 *
 * All methods are disabled (enabled = false) because ClaimRegistrationPage's
 * locators are TODO placeholders — this class exists as a checklist of the
 * scenarios the business spec implies, ready to enable one at a time as
 * real locators are filled in. Remove `enabled = false` and wire up
 * ClaimRegistrationPage once you've walked the real "Create Claim" screens.
 */
@Epic("Mosadad Recovery Claim")
@Feature("Stage 1 — Claim Registration")
public class ClaimRegistrationTest extends BaseUiTest {

    private ClaimRegistrationPage claimRegistrationPage;

    @Test(enabled = false, groups = {"stub", "claim-registration"})
    @Description("Manual Entry: insurer types claim number, policy/vehicle/accident details, police report reference, and supporting documents directly.")
    public void claimantCanRegisterClaimViaManualEntry() {
        claimRegistrationPage = new ClaimRegistrationPage(page);
        claimRegistrationPage.startManualEntry();
        // TODO: fill claim number, policy/vehicle/accident details, police report
        // reference, upload supporting docs, submit, assert claim created +
        // at-fault insurer auto-notified.
    }

    @Test(enabled = false, groups = {"stub", "claim-registration"})
    @Description("Police Data Entry: accident details sourced from an official UAE police report (Dubai Police / Rafid / Saeed / other authority).")
    public void claimantCanRegisterClaimViaPoliceDataEntry() {
        // TODO
    }

    @Test(enabled = false, groups = {"stub", "claim-registration"})
    @Description("Fast Track: bulk Excel upload for high-volume simple recoveries, with column validation and duplicate-claim detection.")
    public void claimantCanRegisterClaimsViaFastTrackBulkUpload() {
        // TODO
    }

    @Test(enabled = false, groups = {"stub", "claim-registration"})
    @Description("A claim without a valid Police Report reference number must not be allowed to proceed — recovery is based on the official Police Report.")
    public void claimRegistrationRequiresValidPoliceReportReference() {
        // TODO
    }

    @Test(enabled = false, groups = {"stub", "claim-registration"})
    @Description("On successful claim submission, the At-Fault insurer must be automatically notified.")
    public void atFaultInsurerIsAutoNotifiedOnClaimSubmission() {
        // TODO — requires At-Fault Insurer test credentials, not yet provisioned.
    }
}
