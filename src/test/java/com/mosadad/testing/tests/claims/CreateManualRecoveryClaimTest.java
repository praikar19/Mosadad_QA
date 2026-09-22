package com.mosadad.testing.tests.claims;

import com.mosadad.testing.base.BaseUiTest;
import com.mosadad.testing.pages.DashboardPage;
import com.mosadad.testing.pages.RecoveryClaimsHubPage;
import com.mosadad.testing.pages.claims.CreateManualClaimPage;
import com.mosadad.testing.pages.claims.CreateManualRecoveryClaimPage;
import com.mosadad.testing.pages.claims.PotentialRecoveryClaimsListPage;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * /entity-portal/manual-claim — Create Manual Recovery Claim, Step 1 (police
 * report provider + upload gate). QA environment, Claimant Insurer (see
 * CreateManualRecoveryClaimPage's class Javadoc for the two-screen flow and
 * its verified selectors).
 *
 * Covers only the upload-gate validation and the Submit -> confirmation
 * popup -> success toast hand-off so far (oversized file, unsupported
 * format, then a valid submission). The rest of the claim form
 * (Accident/Claimant/At-fault Details, Create — see CreateManualClaimPage)
 * is deliberately not driven yet; more steps are planned for a later stage.
 */
public class CreateManualRecoveryClaimTest extends BaseUiTest {

    // 1x1 transparent PNG — a genuinely valid "png" per the widget's own
    // supported-format list (pdf, png, jpg/jpeg), built in memory so no
    // binary fixture needs to be committed to the repo.
    private static final byte[] VALID_PNG = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=");

    private CreateManualRecoveryClaimPage createPage;

    // Cached-session login (see BaseUiTest.loginWithCachedSession) instead
    // of driving the real login form — login itself is already covered by
    // LoginUiTest, and this class only exercises the upload gate.
    @BeforeMethod(alwaysRun = true)
    public void loginAndOpenCreateManualClaimPage() {
        DashboardPage dashboard = loginWithCachedSession("qa", "dubai");
        RecoveryClaimsHubPage hub = dashboard.openRecoveryClaims();
        PotentialRecoveryClaimsListPage list = hub.openPotentialRecoveryClaimsListPage();
        assertThat(list.isLoaded()).as("Potential Recovery Claims List should load").isTrue();

        createPage = list.clickCreateNewRecoveryClaimBtn();
        assertThat(createPage.isLoaded()).as("Create Manual Claim screen should load").isTrue();
    }

    @Test(groups = {"ui", "manual claim"})
    @Severity(SeverityLevel.NORMAL)
    @Description("Create Manual Claim upload gate must reject a police report over the 10 MB limit with its size error.")
    public void oversizedFileIsRejectedOnUpload() {
        createPage.selectReportProvider("Rafid");
        createPage.uploadPoliceReport("oversized-police-report.png", "image/png", new byte[11 * 1024 * 1024]);

        assertThat(createPage.isFileUploadErrorVisible())
                .as("An oversized file should surface the upload's size-limit error")
                .isTrue();
        assertThat(createPage.getFileUploadErrorMessage())
                .isEqualTo("Error: File size exceeds the maximum limit of 10 MB.");
    }

    @Test(groups = {"ui", "manual claim"})
    @Severity(SeverityLevel.NORMAL)
    @Description("Create Manual Claim upload gate must reject a file whose format isn't pdf/png/jpg/jpeg.")
    public void unsupportedFileFormatIsRejectedOnUpload() {
        createPage.selectReportProvider("Rafid");
        createPage.uploadPoliceReport("police-report.txt", "text/plain", "not a police report".getBytes());

        assertThat(createPage.isFileUploadErrorVisible())
                .as("An unsupported file format should surface an upload validation error")
                .isTrue();
        // Exact wording not yet verified live for the format case (only the
        // size-limit message is, see getFileUploadErrorMessage()'s Javadoc) —
        // capture it for now instead of asserting a guessed string.
        log.info("Unsupported-format upload error text: {}", createPage.getFileUploadErrorMessage());
    }

    @Test(groups = {"ui", "manual claim"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("A valid provider + police report upload must reach the 'Processing Police Report' confirmation popup, and Continue must show a success toast.")
    public void validUploadReachesConfirmationPopupAndSuccessToast() {
        createPage.selectReportProvider("Rafid");
        createPage.uploadPoliceReport("police-report.png", "image/png", VALID_PNG);
        createPage.submitPoliceReportStep();

        assertThat(createPage.isProcessingPopupVisible())
                .as("Submitting a valid police report should show the 'Processing Police Report' confirmation popup")
                .isTrue();

        CreateManualClaimPage claimFormPage = createPage.continueProcessingPoliceReportPopup();

        assertThat(claimFormPage.isSuccessToastVisible())
                .as("Continuing past the confirmation popup should show a success toast")
                .isTrue();
        String successMessage = claimFormPage.getSuccessToastMessage();
        log.info("Manual claim upload success toast: {}", successMessage);
        assertThat(successMessage).isNotBlank();

        // Test intentionally ends here — the rest of the claim form
        // (Accident/Claimant/At-fault Details, Create) isn't driven yet;
        // more steps to be added in a later stage.
    }
}
