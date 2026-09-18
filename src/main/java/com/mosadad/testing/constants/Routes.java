package com.mosadad.testing.constants;

/**
 * Front-end route paths (relative to base.url), verified live against the
 * QA environment on 2026-08-30 while logged in as the Claimant Insurer role.
 *
 * These are real, confirmed routes — safe to build tests against directly.
 * Routes for the deeper claim-lifecycle screens (claim detail, quotation,
 * invoice, settlement forms) were not explored and are not listed here; see
 * the TODOs in pages/claims/* for those.
 */
public final class Routes {

    private Routes() {}

    public static final String LOGIN = "/auth/login";
    public static final String FORGOT_PASSWORD = "/auth/forget-password";

    public static final String DASHBOARD = "/entity-landing/dashboard";
    public static final String RECOVERY_CLAIMS_HUB = "/entity-landing/recovery-claims";

    /** TODO: not yet verified live — inferred sibling of the two routes above, but the
     *  "Company Details" sidebar link's href was not captured during exploration. */
    public static final String COMPANY_DETAILS = "/entity-landing/company-details";

    /* ── Dashboard module ─────────────────────────────────────────────── */
    public static final String CLAIMS_REPORT = "/entity-portal/financial-statement-report";
    public static final String SLA_VIOLATION = "/entity-portal/sla-violation";

    /* ── Recovery Claim Records module ────────────────────────────────── */
    public static final String POTENTIAL_RECOVERY_CLAIMS = "/entity-portal/potential-recovery-claims";
    public static final String CREATE_MANUAL_RECOVERY_CLAIMS ="/entity-portal/manual-claim";
    public static final String CREATE_MANUAL_CLAIMS ="/entity-portal/manual-claim";
    public static final String RECOVERY_CLAIMS_LIST = "/entity-portal/my-claims";
    public static final String FAST_TRACK = "/entity-portal/fasttrack-requests";

    /** Prefix only — the real URL is this + a dynamic claim id, e.g. ".../recovery-claim/6a99629b8877afcbdf1c172e". Verified live 2026-09-03 after CreateManualClaimPage.createBtnClick(). */
    public static final String OPEN_RECOVERY_CLAIM = "/entity-portal/recovery-claim/";

    /* ── Financial module ─────────────────────────────────────────────── */
    public static final String BULK_SETTLEMENT = "/entity-portal/bulk-settlement";
    public static final String DUE_AMOUNT = "/entity-portal/due-amount";
    public static final String PAYMENT_HISTORY = "/entity-portal/payment-history";
}
