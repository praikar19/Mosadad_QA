package com.mosadad.testing.tests.claims;

import com.mosadad.testing.base.BaseUiTest;
import io.qameta.allure.*;
import org.testng.annotations.Test;

/**
 * STUB SUITE — Disputes (MOSADAD_DOMAIN.md §Disputes). All methods
 * disabled — see ClaimRegistrationTest header comment for why.
 */
@Epic("Mosadad Recovery Claim")
@Feature("Disputes")
public class DisputeTest extends BaseUiTest {

    @Test(enabled = false, groups = {"stub", "dispute"})
    @Description("Claimant can raise a structured dispute over liability, repair amount, or invoice instead of email back-and-forth.")
    public void claimantCanRaiseDispute() {
        // TODO
    }

    @Test(enabled = false, groups = {"stub", "dispute"})
    @Description("At-Fault insurer can respond to a raised dispute; all communication and timestamps are logged.")
    public void atFaultInsurerCanRespondToDispute() {
        // TODO
    }

    @Test(enabled = false, groups = {"stub", "dispute"})
    @Description("Undisputed amounts on a claim can still move forward even while a dispute is open.")
    public void undisputedAmountsCanStillProgressDuringOpenDispute() {
        // TODO
    }

    @Test(enabled = false, groups = {"stub", "dispute"})
    @Description("Regulator can see the full dispute history for an unresolved dispute.")
    public void regulatorCanViewFullDisputeHistory() {
        // TODO — requires Regulator test credentials, not yet provisioned.
    }
}
