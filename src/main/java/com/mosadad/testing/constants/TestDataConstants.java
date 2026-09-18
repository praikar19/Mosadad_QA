package com.mosadad.testing.constants;

/**
 * Static, non-secret test data. Credentials live in ConfigManager (backed by
 * the gitignored credentials.properties), never here.
 */
public final class TestDataConstants {

    private TestDataConstants() {}

    /* ── User roles (see MOSADAD_DOMAIN.md §Actors) ───────────────────────
     * The QA account currently available (Dubaiqa@gmail.com) is a
     * Claimant Insurer. At-Fault Insurer / Regulator / Mosadad Admin
     * accounts are not yet provisioned for this framework — obtain
     * credentials for those roles before writing cross-role tests
     * (e.g. claimant submits -> at-fault insurer approves).
     */
    public static final String ROLE_CLAIMANT_INSURER = "Claimant Insurer";
    public static final String ROLE_AT_FAULT_INSURER = "At-Fault Insurer";
    public static final String ROLE_REGULATOR = "Regulator";
    public static final String ROLE_MOSADAD_ADMIN = "Mosadad Admin";

    public static final int TOTAL_UAE_INSURERS = 38;

    /* ── Claim creation methods (Stage 1) ─────────────────────────────── */
    public static final String CLAIM_METHOD_MANUAL = "Manual Entry";
    public static final String CLAIM_METHOD_POLICE_DATA = "Police Data Entry";
    public static final String CLAIM_METHOD_FAST_TRACK = "Fast Track";

    /* ── Quotation types (Stage 2) ────────────────────────────────────── */
    public static final String QUOTATION_NORMAL_REPAIR = "Normal Repair";
    public static final String QUOTATION_TOTAL_LOSS = "Total Loss";

    /* ── SLA timers ────────────────────────────────────────────────────── */
    public static final int NORMAL_REPAIR_SLA_HOURS = 72;
    public static final int TOTAL_LOSS_SLA_HOURS = 168; // 7 days
    public static final int SALVAGE_VAT_PERCENT = 5;

    /* ── Claim statuses (as documented in the business spec) ─────────────
     * TODO: confirm exact strings/enums the app uses once claim detail
     * screens are explored — these are the spec's plain-English labels.
     */
    public static final String STATUS_CLOSED = "Closed";
}
