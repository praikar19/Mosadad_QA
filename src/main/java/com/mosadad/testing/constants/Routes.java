package com.mosadad.testing.constants;

/** Front-end route paths, relative to base.url. */
public final class Routes {

    private Routes() {}

    public static final String LOGIN = "/auth/login";
    public static final String FORGOT_PASSWORD = "/auth/forget-password";

    public static final String DASHBOARD = "/entity-landing/dashboard";
    public static final String RECOVERY_CLAIMS_HUB = "/entity-landing/recovery-claims";

    public static final String CLAIMS_REPORT = "/entity-portal/financial-statement-report";
    public static final String SLA_VIOLATION = "/entity-portal/sla-violation";

    public static final String POTENTIAL_RECOVERY_CLAIMS = "/entity-portal/potential-recovery-claims";
    public static final String RECOVERY_CLAIMS_LIST = "/entity-portal/my-claims";
    public static final String FAST_TRACK = "/entity-portal/fasttrack-requests";
    public static final String FAST_TRACK_CREATE_BATCH = "/entity-portal/fasttrack-requests/create-batch";
    /** Prefix only — followed by a dynamic batch id. */
    public static final String FAST_TRACK_CLAIMS_LIST = "/entity-portal/fasttrack-request-claims";
    /** Prefix only — followed by a dynamic claim id. */
    public static final String FAST_TRACK_CLAIM_DETAILS = "/entity-portal/fasttrack-claim-details";


    public static final String BULK_SETTLEMENT = "/entity-portal/bulk-settlement";
    public static final String BULK_SETTLEMENT_PAYMENT = "/entity-portal/bulk-settlement-payment";
    public static final String DUE_AMOUNT = "/entity-portal/due-amount";
    public static final String CREDIT_NOTE_BULK = "/entity-portal/credit-note-bulk";
    /** Where the ATB checkout returns to: payment-status forwards to payment-success or payment-failure. */
    public static final String PAYMENT_SUCCESS = "/entity-portal/payment-success";
    public static final String PAYMENT_FAILURE = "/entity-portal/payment-failure";
    public static final String PAYMENT_HISTORY = "/entity-portal/payment-history";
}
