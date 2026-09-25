package com.mosadad.testing.pages.claims;

import com.mosadad.testing.constants.Routes;
import com.mosadad.testing.pages.BasePage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.FilePayload;
import io.qameta.allure.Step;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fast Track claim details. The first file input is the mandatory recovery
 * document; its file name must contain the claim number without "/"
 * (see FastTrackTestDataGenerator.claimNumberForFileName()). The Claim Summary
 * card is label/value text pairs (verified live on stage), e.g.
 * "Batch Code", "Claimant Claim Number", "Report Number", "Plate Number",
 * "Atfault Plate Number", "Claim Amount".
 */
public class FastTrackClaimDetailsPage extends BasePage {

    private static final String FILE_INPUTS = "input[type='file']";
    private static final int MANDATORY_DOCUMENT_INPUT_INDEX = 0;
    private static final String CLAIM_SUMMARY_CARD = ".card:has(h3:text-is('Claim Summary'))";
    private static final String ITEM_DOCUMENTS_API = "/FastTrack/ItemDocuments/";
    /** "+ Add Claim Number" / "+ Add Chassis Number" in the at-fault's Claim Summary (verified live). */
    private static final String AT_FAULT_ADD_LINK = "span.atfault-add-link";

    public FastTrackClaimDetailsPage(Page page) {
        super(page);
    }

    public boolean isLoaded() {
        return currentUrl().contains(Routes.FAST_TRACK_CLAIM_DETAILS);
    }

    /** The value printed under a Claim Summary label, or "" if the label is missing. */
    public String getSummaryValue(String label) {
        waitForSummaryData();
        return readSummaryValue(label);
    }

    private String readSummaryValue(String label) {
        Locator card = locator(CLAIM_SUMMARY_CARD);
        String text = (card.count() > 0 ? card.first() : locator("body")).innerText();
        Matcher m = Pattern.compile("(?m)^\\s*" + Pattern.quote(label) + "\\s*\\n+\\s*(.+?)\\s*$").matcher(text);
        return m.find() ? m.group(1) : "";
    }

    /**
     * The summary renders every label with "-" before the claim data arrives,
     * so wait until the batch code (always set for a Fast Track claim) is filled in.
     */
    private void waitForSummaryData() {
        page.getByText("Claim Summary").first().waitFor(new Locator.WaitForOptions().setTimeout(15000));
        long deadline = System.currentTimeMillis() + 15000;
        String batchCode = readSummaryValue("Batch Code");
        while ((batchCode.isEmpty() || batchCode.equals("-")) && System.currentTimeMillis() < deadline) {
            page.waitForTimeout(250);
            batchCode = readSummaryValue("Batch Code");
        }
        if (batchCode.isEmpty() || batchCode.equals("-")) {
            throw new IllegalStateException("Claim Summary data did not load within 15s on " + currentUrl());
        }
    }

    public String getBatchCode() {
        return getSummaryValue("Batch Code");
    }

    /**
     * At-fault side: "+ Add Claim Number" → "Add At Fault Claim and Chassis Number"
     * dialog → Submit, then the Save button that appears in the Claim Summary
     * (flow from the manual stage screenshots, 2026-09-24). Only the claim number is
     * required. Save's endpoint isn't known, so this waits for the first claims write
     * request it sends, then reloads to read the number back.
     */
    @Step("Add at-fault claim number {0} and save")
    public String addAtFaultClaimNumber(String atFaultClaimNumber) {
        waitForSummaryData();
        // Not getByText: the header note "Add Claim Number and Chassis Number to proceed…" matches first.
        locator(AT_FAULT_ADD_LINK).filter(new Locator.FilterOptions().setHasText("Add Claim Number")).click();
        Locator claimNumberInput = page.getByPlaceholder("Enter At-Fault Claim Number");
        claimNumberInput.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        claimNumberInput.fill(atFaultClaimNumber);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Submit").setExact(true)).click();

        Response response = page.waitForResponse(
                r -> r.url().contains("/api/claims/") && !"GET".equals(r.request().method())
                        && !r.url().contains("/List") && !r.url().contains("/Search"),
                new Page.WaitForResponseOptions().setTimeout(30000),
                () -> getLocatorByRole(AriaRole.BUTTON, "Save").first().click());
        String body = response.text();
        if (!response.ok() || body.contains("\"isSuccess\":false")) {
            throw new IllegalStateException("Saving the at-fault claim number failed — " + response.url()
                    + " HTTP " + response.status() + ": " + body.substring(0, Math.min(body.length(), 500)));
        }

        reloadAndWaitForNetworkIdle();
        return getSummaryValue("At Fault Claim Number");
    }

    /**
     * At-fault side: the Negotiate / Reject / Accept buttons at the bottom of the
     * details page (from the manual stage screenshots). The page updates Claim
     * Status in place; reloads once if it hasn't changed after 15s. Returns the
     * final Claim Status.
     */
    @Step("Accept the claim")
    public String acceptClaim(String expectedStatus) {
        waitForSummaryData();
        // Playwright scrolls the button into view before clicking.
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Accept").setExact(true)).click();
        String status = waitForClaimStatus(expectedStatus, 15000);
        if (!status.equals(expectedStatus)) {
            reloadAndWaitForNetworkIdle();
            status = waitForClaimStatus(expectedStatus, 15000);
        }
        return status;
    }

    private String waitForClaimStatus(String expectedStatus, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        String status = getSummaryValue("Claim Status");
        while (!status.equals(expectedStatus) && System.currentTimeMillis() < deadline) {
            page.waitForTimeout(500);
            status = readSummaryValue("Claim Status");
        }
        return status;
    }

    @Step("Upload mandatory recovery document: {0}")
    public FastTrackClaimDetailsPage uploadMandatoryDocument(String fileName, String mimeType, byte[] content) {
        // The checklist only shows its upload inputs once the claim data has loaded.
        waitForSummaryData();
        locator(FILE_INPUTS).nth(MANDATORY_DOCUMENT_INPUT_INDEX)
                .setInputFiles(new FilePayload(fileName, mimeType, content));
        return this;
    }

    /**
     * Save uploads the attached documents via POST claims/FastTrack/ItemDocuments/{claimId};
     * leaving the page before it answers cancels the upload, so wait for that response.
     */
    @Step("Click 'Save'")
    public FastTrackClaimDetailsPage clickSave() {
        Response response = page.waitForResponse(
                r -> r.url().contains(ITEM_DOCUMENTS_API) && "POST".equals(r.request().method()),
                new Page.WaitForResponseOptions().setTimeout(30000),
                () -> getLocatorByRole(AriaRole.BUTTON, "Save").click());
        String body = response.text();
        // Business failures come back as HTTP 200 with isSuccess=false (see ApiAssertions).
        if (!response.ok() || body.contains("\"isSuccess\":false")) {
            throw new IllegalStateException("Saving the recovery document failed — HTTP " + response.status()
                    + ": " + body.substring(0, Math.min(body.length(), 500)));
        }
        return this;
    }
}
