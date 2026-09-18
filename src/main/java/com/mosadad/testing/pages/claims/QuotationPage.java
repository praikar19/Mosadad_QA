package com.mosadad.testing.pages.claims;

import com.mosadad.testing.pages.BasePage;
import com.microsoft.playwright.Page;

/**
 * STUB — Stage 2: Quotation (see MOSADAD_DOMAIN.md §Stage 2). Not yet
 * explored live; selectors are TODO placeholders.
 *
 * Two paths, mutually exclusive per claim:
 *
 * A. Normal Repair
 *    Claimant uploads workshop estimate, repair amount, supporting docs.
 *    System starts a 72-hour SLA timer; At-Fault insurer must Approve or
 *    Negotiate within 3 working days. Negotiation happens in-system until
 *    both sides agree, then the system generates an Approval Letter.
 *
 * B. Total Loss
 *    Declared when repair is uneconomical. Claimant submits sum insured,
 *    total loss amount, supporting docs. System starts a 168-hour (7-day)
 *    SLA timer. On approval, Salvage stage begins: claimant enters salvage
 *    buyer details + salvage amount; system auto-adds 5% VAT and updates
 *    the total claim amount. Approval then triggers the Invoice stage.
 */
public class QuotationPage extends BasePage {

    // TODO: verify against the live claim-detail / quotation screen.
    private static final String NORMAL_REPAIR_TAB = "TODO-normal-repair-tab";
    private static final String TOTAL_LOSS_TAB = "TODO-total-loss-tab";

    private static final String WORKSHOP_ESTIMATE_UPLOAD = "TODO-workshop-estimate-upload";
    private static final String REPAIR_AMOUNT_INPUT = "TODO-repair-amount-input";

    private static final String SUM_INSURED_INPUT = "TODO-sum-insured-input";
    private static final String TOTAL_LOSS_AMOUNT_INPUT = "TODO-total-loss-amount-input";

    private static final String SALVAGE_BUYER_INPUT = "TODO-salvage-buyer-input";
    private static final String SALVAGE_AMOUNT_INPUT = "TODO-salvage-amount-input";

    private static final String APPROVE_BUTTON = "TODO-approve-button";
    private static final String NEGOTIATE_BUTTON = "TODO-negotiate-button";
    private static final String SUBMIT_BUTTON = "TODO-submit-quotation-button";

    public QuotationPage(Page page) {
        super(page);
    }

    /* ── Normal Repair ─────────────────────────────────────────────────── */

    public void submitNormalRepair(String workshopEstimateFilePath, String repairAmount) {
        click(NORMAL_REPAIR_TAB);
        locator(WORKSHOP_ESTIMATE_UPLOAD).setInputFiles(java.nio.file.Paths.get(workshopEstimateFilePath));
        fill(REPAIR_AMOUNT_INPUT, repairAmount);
        click(SUBMIT_BUTTON);
    }

    /* ── Total Loss + Salvage ─────────────────────────────────────────── */

    public void declareTotalLoss(String sumInsured, String totalLossAmount) {
        click(TOTAL_LOSS_TAB);
        fill(SUM_INSURED_INPUT, sumInsured);
        fill(TOTAL_LOSS_AMOUNT_INPUT, totalLossAmount);
        click(SUBMIT_BUTTON);
    }

    /** Salvage amount submitted here gets 5% VAT auto-added by the system. */
    public void submitSalvageDetails(String buyerDetails, String salvageAmount) {
        fill(SALVAGE_BUYER_INPUT, buyerDetails);
        fill(SALVAGE_AMOUNT_INPUT, salvageAmount);
        click(SUBMIT_BUTTON);
    }

    /* ── At-Fault insurer response ────────────────────────────────────── */

    public void approve() {
        click(APPROVE_BUTTON);
    }

    public void negotiate() {
        click(NEGOTIATE_BUTTON);
    }
}
