package com.mosadad.testing.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Fast Track claims created in the current test run, with the batch code
 * each belongs to, so later steps or tests in the same run can reuse them.
 * In memory only: nothing is written to disk, and FastTrackE2ETest clears it
 * in @AfterSuite, so every run starts empty and leaves nothing behind.
 */
public final class FastTrackRunClaims {

    /** One claim of this run; fields are filled in as the flow reaches them. */
    public static final class Entry {
        public final String claimantClaimNumber;
        public final String amountAed;
        public volatile String batchCode = "";
        public volatile String claimSerialNumber = "";
        public volatile String atFaultClaimNumber = "";
        public volatile String lastStatus = "";

        public Entry(String claimantClaimNumber, String amountAed) {
            this.claimantClaimNumber = claimantClaimNumber;
            this.amountAed = amountAed;
        }

        @Override
        public String toString() {
            return batchCode + " | " + claimantClaimNumber + " | serial " + claimSerialNumber
                    + " | at-fault " + atFaultClaimNumber + " | AED " + amountAed + " | " + lastStatus;
        }
    }

    private static final Map<String, Entry> CLAIMS = new LinkedHashMap<>();

    private FastTrackRunClaims() {}

    /** Registers a claim of this run (keyed by claimant claim number) and returns its entry. */
    public static synchronized Entry add(String claimantClaimNumber, String amountAed) {
        return CLAIMS.computeIfAbsent(claimantClaimNumber, k -> new Entry(claimantClaimNumber, amountAed));
    }

    /** The entry for a claimant claim number, or null if this run didn't create it. */
    public static synchronized Entry get(String claimantClaimNumber) {
        return CLAIMS.get(claimantClaimNumber);
    }

    public static synchronized List<Entry> all() {
        return new ArrayList<>(CLAIMS.values());
    }

    /** Distinct batch codes created in this run, in creation order. */
    public static synchronized Set<String> batchCodes() {
        Set<String> codes = new LinkedHashSet<>();
        CLAIMS.values().forEach(e -> { if (!e.batchCode.isEmpty()) codes.add(e.batchCode); });
        return codes;
    }

    public static synchronized void clear() {
        CLAIMS.clear();
    }
}
