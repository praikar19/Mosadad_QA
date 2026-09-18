package com.mosadad.testing.tests.utils;

import com.mosadad.testing.utils.ExcelUtils;
import io.qameta.allure.*;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verified against the real file: src/main/resources/Fast Track333.xlsx
 * (the Fast Track bulk-upload template — see MOSADAD_DOMAIN.md §Fast Track).
 * No browser/live app involved — pure data-reading, safe to run anywhere.
 */
@Epic("Mosadad Recovery Claim")
@Feature("Test Data — Excel")
public class ExcelUtilsTest {

    private static final String FAST_TRACK_FILE = "Fast Track333.xlsx";

    @Test(groups = {"utils"})
    @Description("Reads only populated data rows, keyed by the sheet's header row; blank template rows are skipped.")
    public void readsFastTrackDataRowsKeyedByHeader() {
        List<Map<String, String>> rows = ExcelUtils.readRowsAsMaps(FAST_TRACK_FILE);

        // The template has header row 1 + 3 populated claims + several
        // intentionally-blank rows for future manual entry — only the 3
        // populated rows should come back.
        assertThat(rows).hasSize(3);

        Map<String, String> firstClaim = rows.get(0);
        assertThat(firstClaim.get("Claim No.")).isEqualTo("Claim424403");
        assertThat(firstClaim.get("Policy")).isEqualTo("Policy12/43");
        assertThat(firstClaim.get("Invoice_number")).isEqualTo("Invoice140");
        assertThat(firstClaim.get("Plate claiman")).isEqualTo("1400");
        assertThat(firstClaim.get("Amount")).isEqualTo("5850");
        assertThat(firstClaim.get("Recovery_claim_amount")).isEqualTo("5500");
    }

    @Test(groups = {"utils"})
    @Description("Every returned row must carry a non-blank Claim No. — the primary key of the Fast Track template.")
    public void everyRowHasAClaimNumber() {
        List<Map<String, String>> rows = ExcelUtils.readRowsAsMaps(FAST_TRACK_FILE);

        assertThat(rows).allSatisfy(row ->
                assertThat(row.get("Claim No.")).as("Claim No. on row %s", row).isNotBlank());
    }

    @Test(groups = {"utils"})
    @Description("readRowsAsDataProvider() wraps each row map for direct use as a TestNG @DataProvider return value.")
    public void dataProviderFormMatchesMapForm() {
        List<Map<String, String>> asMaps = ExcelUtils.readRowsAsMaps(FAST_TRACK_FILE);
        Object[][] asDataProvider = ExcelUtils.readRowsAsDataProvider(FAST_TRACK_FILE);

        assertThat(asDataProvider).hasNumberOfRows(asMaps.size());
        assertThat(asDataProvider[0][0]).isEqualTo(asMaps.get(0));
    }
}
