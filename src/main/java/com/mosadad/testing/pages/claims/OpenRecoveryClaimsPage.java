package com.mosadad.testing.pages.claims;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.mosadad.testing.constants.Routes;
import com.mosadad.testing.pages.BasePage;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * /entity-portal/recovery-claim/{claimId} — the read-only claim detail
 * screen the app lands on after CreateManualClaimPage.createBtnClick()
 * succeeds. Verified live 2026-09-03 as the Claimant Insurer (Dubai stage
 * account). This is the "Claim Summary" tab specifically (the page also has
 * Claim Flow / Claim Attachments tabs, not covered here).
 *
 * No real HTML ids anywhere on this page (confirmed live) — every value is
 * a plain "label + value" pair rendered by Angular:
 *   {@code <p class="field-title">Label</p><p class="field-value">Value</p>}
 * always as immediate siblings, sometimes wrapped in a ".field" div
 * (Claim Details / Accident Details cards), sometimes not (Non-Faulty
 * Details / Faulty Details vehicle cards) — fieldValue() below handles both
 * uniformly by walking to the next ".field-value" sibling via XPath rather
 * than relying on the wrapper.
 *
 * "Damaged Parts" under Accident Details only ever shows the claimant
 * vehicle's damaged parts, never the at-fault vehicle's, even when both
 * are submitted in the same request — confirmed by design (not a bug):
 * this summary is the claimant's own view of their claim, and the
 * at-fault vehicle's damage isn't part of what it reports. See
 * getClaimantDamagedParts() Javadoc.
 *
 * NOT a bug, despite first appearing to be one: earlier exploratory runs
 * that reused the exact same claimant vehicle (plate/chassis/policy
 * number) across ~7 successive test submissions saw the "Non-Faulty
 * Details" card render that one vehicle's block 3x, then 6x, then 7x —
 * growing in lockstep with the repeat count. A controlled run with a
 * never-before-used vehicle showed exactly one block. So this card is
 * evidently showing one row per historical claim matched to that vehicle,
 * correctly reflecting that the same vehicle had been reused across
 * several claims — not a rendering bug. getClaimant*() below still reads
 * only the first occurrence regardless, both because it's the correct
 * value when duplicated and because it's exactly one row for a
 * genuinely-fresh vehicle.
 *
 * Two fields cannot be exactly asserted against ManualClaimDetails and
 * intentionally don't try to be:
 *   - VIN Number is masked here (e.g. "293463273812*****" for chassis
 *     "29346327381293899") — compare with startsWith() on a short prefix,
 *     not equals().
 *   - Policy Expiry Date: ManualClaimDetails only carries a boolean
 *     ("set to some valid future date" — see CreateManualClaimPage's
 *     pickClaimantPolicyExpiryDateToday() Javadoc, it does NOT pick
 *     today), not the concrete date it picked, so there's no expected
 *     string to compare against here. Only presence can be checked.
 *
 * Also note two label renames between the entry form and this summary,
 * same underlying field either way:
 *   - Form "Accident Source" -> Summary "Report Source"
 *   - Form "Accident Description" -> Summary "Report Description"
 */
public class OpenRecoveryClaimsPage extends BasePage {

    public OpenRecoveryClaimsPage(Page page){
        super(page);
    }

    public boolean isLoaded() {
        return currentUrl().contains(Routes.OPEN_RECOVERY_CLAIM);
    }

    /** Reload this claim page to pick up state changes made by the other actor's session — see BasePage.reloadAndWaitForNetworkIdle() Javadoc. */
    public void refresh() {
        reloadAndWaitForNetworkIdle();
    }

    private static final String CLAIM_DETAILS_BTN = "button:text-is('Claim Details')";
    private static final String CREATE_QUOTATION_BTN = "button:text-is('Create Quotation')";
    private static final String ACCEPT_BTN = "button:text-is('Accept')";
    private static final String REJECT_BTN = "button:text-is('Reject')";
    private static final String NEGOTIATE_BTN = "button:text-is('Negotiate')";
    private static final String AT_FAULT_CLAIM_NUMBER = "#atFaultClaimNumber";
    /** Quotation-type card titles — pass to selectQuotationType(). */
    public static final String REPAIR_QUOTATION = "Repair Quotation";
    public static final String OPEN_LPO = "Open LPO";
    public static final String TOTAL_LOSS = "Total Loss";
    /** "Selection" radio labels on the Repair Quotation form (real <input> is hidden; the <label> is what's clickable) — Workshop is selected by default. */
    private static final String WORKSHOP_RADIO_LABEL = "label[for='workshop']";
    private static final String AGENCY_RADIO_LABEL = "label[for='agency']";
    /** Filterable mat-select, not a text field — confirmed live 2026-09-04. Options come from a real workshop list (e.g. "Al Tharaa Auto Mech Rep W Shop"), picked the same way as CreateManualClaimPage's mat-selects. */
    private static final String WORKSHOP_NAME_SELECT = "#shopId";
    private static final String WORKSHOP_LOCATION = "input[formcontrolname='workshopLocation']";
    private static final String QUOTATION_NUMBER = "#quotationNumber";
    private static final String REPAIR_DURATION = "input[formcontrolname='duration']";
    private static final String REPAIR_COST = "#repairCost";
    private static final String UPLOAD_CLAIM_DOCUMENTS = "app-file-uploader:has-text('Upload Claim Documents') input[type='file']";
    private static final String UPLOAD_OTHER_DOCUMENTS = "app-file-uploader:has-text('Upload Supporting Documents') input[type='file']";
    private static final String SUBMIT_REQUEST = "button:text-is('Submit Request')";
    private static final String RESET_DATA = "button:text-is('Reset Data')";
    private static final String CREATE_INVOICE = "button:text-is('Create Invoice')";
    private static final String INVOICE_NUMBER = "#invoiceNumber";
    private static final String INVOICE_AMOUNT = "#invoiceAmount";
    /** Confirmed live 2026-09-04 — the invoice form's file-uploader section title, not "upload-text-section" (that class doesn't exist here; it's the earlier quotation form's inner label styling). Same app-file-uploader scoping pattern as UPLOAD_CLAIM_DOCUMENTS. */
    private static final String INVOICE_DOC_UPLOADER = "app-file-uploader:has-text('Credit Note And Discharge Documents')";
    private static final String UPLOAD_DOC_INVOICE = INVOICE_DOC_UPLOADER + " input[type='file']";
    private static final String RESPOND = "button:text-is('Respond')";
    /**
     * Confirmed live 2026-09-04 — clicking this on the at-fault claim page's
     * Settlement panel navigates to /entity-portal/bulk-settlement-payment,
     * a claim-picker page listing the claimant's OTHER still-unsettled
     * claims (batch settlement), not just this one. The same button text
     * appears again on that page (to actually create the credit note) and
     * on Claim Closure ("Proceed to Claim Closure") — each is scoped to its
     * own page so the shared text is not ambiguous within one page.
     */
    private static final String REQUEST_CREDIT_NOTE_BTN = "button:text-is('Request Credit Note')";
    private static final String PROCEED_TO_PAYMENT_BTN = "button:text-is('Proceed To Payment')";
    /** External ATB Pay checkout (rak.atbpay.me) reached via Proceed To Payment — confirmed live 2026-09-04 to render in the same tab/Page, not a popup. */
    private static final String CHECKOUT_TERMS_CHECKBOX = "input[type='checkbox']";
    private static final String CHECKOUT_CONFIRM_BTN = "button:text-is('Confirm')";
    private static final String PROCEED_TO_CLAIM_CLOSURE_BTN = "button:text-is('Proceed to Claim Closure')";
    private static final String FINALIZE_CLAIM_BTN = "button:text-is('Yes, I want to finalize claim')";
    private static final String CONFIRM_CLAIM_CLOSURE_BTN = "button:text-is('Confirm')";



    /* ── Card scoping ─────────────────────────────────────────────────── */

    /** Claim Details / Accident Details — matched by exact card-title text (avoids the Faulty/Non-Faulty substring collision below). */
    private Locator cardByExactTitle(String title) {
        return page.locator(".card").filter(new Locator.FilterOptions()
                .setHas(page.locator(".card-title").getByText(title, new Locator.GetByTextOptions().setExact(true))));
    }

    /**
     * Claimant vehicle card. Scoped by the "non-faulty-header" class, not by
     * card-title text — "Faulty Details" is a literal substring of
     * "Non-Faulty Details", so hasText("Faulty Details") would wrongly match
     * both cards.
     */
    private Locator nonFaultyCard() {
        return page.locator(".card:has(.non-faulty-header)");
    }

    /** At-fault vehicle card — see nonFaultyCard() Javadoc for why this isn't matched by title text. */
    private Locator faultyCard() {
        return page.locator(".card:has(.faulty-header)");
    }

    /**
     * Finds the "p.field-title" with exact text fieldTitle inside scope,
     * then returns the trimmed text of the next "p.field-value" sibling —
     * works whether or not the pair is wrapped in a ".field" div (see class
     * Javadoc). Uses the first match when a title repeats in scope (only
     * relevant to nonFaultyCard(), whose block is duplicated — see class
     * Javadoc; the duplicates are identical so first() is correct).
     */
    private String fieldValue(Locator scope, String fieldTitle) {
        return scope.locator("xpath=.//p[contains(@class,'field-title') and normalize-space(text())='" + fieldTitle + "']"
                        + "/following-sibling::p[contains(@class,'field-value')][1]")
                .first()
                .innerText()
                .trim();
    }

    /* ── Claim Details ────────────────────────────────────────────────── */

    /** Server-generated claim id (e.g. "RC-2026-043312") — no ManualClaimDetails equivalent, it isn't caller-supplied. */
    public String getClaimSerialNumber()  { return fieldValue(cardByExactTitle("Claim Details"), "Claim Serial Number"); }
    public String getClaimantClaimNumber() { return fieldValue(cardByExactTitle("Claim Details"), "Claimant Claim Number"); }
    /** Shows "---" for a manually-created claim — no ManualClaimDetails equivalent. */
    public String getAtFaultClaimNumberDisplayed() { return fieldValue(cardByExactTitle("Claim Details"), "At Fault Claim Number"); }

    /* ── Non-Faulty Details (claimant vehicle) ────────────────────────── */

    /** No ManualClaimDetails equivalent — Claimant's Insurance Company is app-prefilled/disabled on the entry form (see CreateManualClaimPage). */
    public String getClaimantInsuranceCompanyDisplayed() { return fieldValue(nonFaultyCard(), "Insurance Company"); }
    /** Masked — see class Javadoc. Compare with startsWith() against ManualClaimDetails.getClaimantChassisNumber(), not equals(). */
    public String getClaimantVinNumberMasked()            { return fieldValue(nonFaultyCard(), "VIN Number"); }
    /** No exact expected value available — see class Javadoc. Only presence/non-"---" can be checked. */
    public String getClaimantPolicyExpiryDateDisplayed()  { return fieldValue(nonFaultyCard(), "Policy Expiry Date"); }
    public String getClaimantPolicyNumberDisplayed()      { return fieldValue(nonFaultyCard(), "Policy Number"); }
    public String getClaimantPolicyTypeDisplayed()        { return fieldValue(nonFaultyCard(), "Policy Type"); }

    /* ── Faulty Details (at-fault vehicle) ────────────────────────────── */

    public String getAtFaultInsuranceCompanyDisplayed() { return fieldValue(faultyCard(), "Insurance Company"); }
    /** Masked — see class Javadoc. Compare with startsWith() against ManualClaimDetails.getAtFaultChassisNumber(), not equals(). */
    public String getAtFaultVinNumberMasked()           { return fieldValue(faultyCard(), "VIN Number"); }
    /** No exact expected value available — see class Javadoc. Only presence/non-"---" can be checked. */
    public String getAtFaultPolicyExpiryDateDisplayed() { return fieldValue(faultyCard(), "Policy Expiry Date"); }
    public String getAtFaultPolicyNumberDisplayed()     { return fieldValue(faultyCard(), "Policy Number"); }
    public String getAtFaultPolicyTypeDisplayed()       { return fieldValue(faultyCard(), "Policy Type"); }

    /* ── Accident Details ─────────────────────────────────────────────── */

    public String getReportNumber() { return fieldValue(cardByExactTitle("Accident Details"), "Report Number"); }
    /** Same field as CreateManualClaimPage's Report Date — the app just relabels it here. */
    public String getReportDate()   { return fieldValue(cardByExactTitle("Accident Details"), "Report Date"); }
    /** Same field as the entry form's "Accident Source" — see class Javadoc re: the label rename. */
    public String getAccidentSourceDisplayed()     { return fieldValue(cardByExactTitle("Accident Details"), "Report Source"); }
    /** Same field as the entry form's "Accident Description" — see class Javadoc re: the label rename. */
    public String getAccidentDescriptionDisplayed() { return fieldValue(cardByExactTitle("Accident Details"), "Report Description"); }
    public String getAccidentType() { return fieldValue(cardByExactTitle("Accident Details"), "Accident Type"); }
    /** Combined "DD/MM/YYYY HH:mm". The date half can't be derived from ManualClaimDetails — see validateAllDetailsOfOpenRecoveryClaimsPage() Javadoc. */
    public String getAccidentDateAndTime() { return fieldValue(cardByExactTitle("Accident Details"), "Accident Date & Time"); }
    public String getEmirate()      { return fieldValue(cardByExactTitle("Accident Details"), "Emirate"); }
    public String getCity()         { return fieldValue(cardByExactTitle("Accident Details"), "City"); }
    public String getArea()         { return fieldValue(cardByExactTitle("Accident Details"), "Area"); }
    public String getStreet()       { return fieldValue(cardByExactTitle("Accident Details"), "Street"); }
    public String getIntersection() { return fieldValue(cardByExactTitle("Accident Details"), "Intersection"); }

    /**
     * All "Damaged Parts" values shown under Accident Details, in DOM
     * order. This is by design, not a bug — see class Javadoc: this list
     * only ever contains the claimant vehicle's damaged parts, confirmed
     * live 2026-09-03 with a controlled, isolated run (fresh
     * never-before-used vehicle data, single claim, no claim history to
     * conflate) — with claimant damaged part "The Right Front Corner" and
     * at-fault damaged part "The Front Right Door" both submitted, this
     * returned exactly one entry, "The Right Front Corner". A caller
     * should only assert the claimant's damaged part is present in this
     * list — there is no at-fault equivalent to check.
     */
    public List<String> getClaimantDamagedParts() {
        return cardByExactTitle("Accident Details")
                .locator("xpath=.//p[contains(@class,'field-title') and normalize-space(text())='Damaged Parts']"
                        + "/following-sibling::p[contains(@class,'field-value')][1]")
                .allInnerTexts()
                .stream()
                .map(String::trim)
                .collect(Collectors.toList());
    }

    public void clickCreateQuotation(){
        click(CREATE_QUOTATION_BTN);
    }

    /**
     * Clicking "Create Quotation" only expands a "Select Quotation Type"
     * step with three cards (REPAIR_QUOTATION / OPEN_LPO / TOTAL_LOSS) —
     * confirmed live 2026-09-04. The actual quotation form (Workshop Name
     * etc.) only renders after one of these is picked, so this must be
     * called before any of the quotation-detail fill methods below.
     */
    public void selectQuotationType(String quotationType) {
        page.locator(".quotation-card").filter(new Locator.FilterOptions()
                .setHas(page.locator(".quotation-title").getByText(quotationType, new Locator.GetByTextOptions().setExact(true))))
                .click();
    }

    public void clickClaimDetails(){
        click(CLAIM_DETAILS_BTN);
    }

    /**
     * Accept is a cross-actor mutation — the other actor's browser session
     * refreshes right after this and expects the change (claim accepted /
     * quotation accepted / invoice accepted) to already be visible.
     * Confirmed live 2026-09-04: without waiting for network-idle here, the
     * claimant's very next refresh + clickCreateInvoice() sometimes ran
     * before the accept mutation had actually landed server-side, so Create
     * Invoice never rendered and the following fillInvoiceNumber() timed
     * out waiting for a field that was never there.
     */
    public void clickAcceptBtn(){
        click(ACCEPT_BTN);
        waitForNetworkIdleBestEffort();
    }

    public void clickRejectBtn(){
        click(REJECT_BTN);
    }

    public void clickNegotiateBtn(){
        click(NEGOTIATE_BTN);
    }

    public void fillAtFaultClaimNumber(String claimNumber){
        fill(AT_FAULT_CLAIM_NUMBER, claimNumber);
        waitForAtFaultClaimNumberValidation();
    }

    /**
     * Filling At Fault Claim Number kicks off an async server-side
     * validation call; clicking Accept before it settles is silently
     * blocked with a "Please wait for claim number validation to
     * complete." toast and no accept mutation fires at all — confirmed
     * live 2026-09-04 via manual browser testing, and was the root cause
     * of clickAcceptBtn() never actually accepting the claim in automated
     * runs. Blurring the field (validation is keyed off blur, not just
     * input) and waiting for network-idle lets that call settle before
     * Accept is clicked.
     */
    private void waitForAtFaultClaimNumberValidation() {
        page.locator(AT_FAULT_CLAIM_NUMBER).blur();
        waitForNetworkIdleBestEffort();
    }

    /** Best-effort settle wait after a click that fires an async server call — see reloadAndWaitForNetworkIdle() Javadoc for why this isn't guaranteed. */
    private void waitForNetworkIdleBestEffort() {
        try {
            page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE,
                    new Page.WaitForLoadStateOptions().setTimeout(10000));
        } catch (com.microsoft.playwright.PlaywrightException e) {
            log.debug("Page did not reach network-idle after mutation click: {}", e.getMessage());
        }
    }

    /**
     * Despite the name (kept for compatibility with existing callers), this
     * is a filterable mat-select, not a text field — see
     * WORKSHOP_NAME_SELECT Javadoc. workshopName must be the exact visible
     * text of a real option (e.g. "Al Tharaa Auto Mech Rep W Shop",
     * confirmed live 2026-09-04), not arbitrary free text.
     */
    public void fillWorkShopName(String workshopname){
        locator(WORKSHOP_NAME_SELECT).click();
        page.getByRole(com.microsoft.playwright.options.AriaRole.OPTION,
                new Page.GetByRoleOptions().setName(workshopname).setExact(true)).click();
        waitForOverlayToClose();
    }

    /** "Selection" radio — Workshop is selected by default (confirmed live 2026-09-04), so this is only needed to switch to Agency or back. */
    public void selectWorkshopOption() { click(WORKSHOP_RADIO_LABEL); }
    public void selectAgencyOption()   { click(AGENCY_RADIO_LABEL); }

    public void fillWorkShopLocation(String workshopLocation){
        fill(WORKSHOP_LOCATION, workshopLocation);
    }

    public String getQuotationNumber(){
        String quotationNumber = getText(QUOTATION_NUMBER);
        return quotationNumber;
    }

    public void fillRepairDurationDays(String durationOfDays){
        fill(REPAIR_DURATION, durationOfDays);
    }

    public void fillRepairCost(String costOFRepair){
        fill(REPAIR_COST, costOFRepair);
    }

    public void clickUploadClaimDocuments(String filePath){
        //click(UPLOAD_CLAIM_DOCUMENTS);
        locator(UPLOAD_CLAIM_DOCUMENTS).setInputFiles(Paths.get(filePath));
    }

    /**
     * Submit Request is a cross-actor mutation (quotation or invoice
     * submittal) — same reasoning as clickAcceptBtn()'s Javadoc: the other
     * actor's browser refreshes right after this and expects the submitted
     * item to already be visible. Confirmed live 2026-09-04: without this
     * wait, the at-fault side's refresh() + clickRespondBtn() sometimes ran
     * before the invoice submittal had landed server-side, so Respond
     * never appeared.
     */
    public void clickSubmitBtn(){
        click(SUBMIT_REQUEST);
        waitForNetworkIdleBestEffort();
    }

    /** Create Invoice sits further down the page than the initial viewport reaches — scroll it into view before clicking. */
    public void scrollToCreateInvoiceBtn() {
        scrollIntoView(CREATE_INVOICE);
    }

    /**
     * Confirmed live 2026-09-04: even with clickAcceptBtn()'s network-idle
     * wait on the at-fault side, the claimant's Create Invoice click
     * sometimes still lands before the invoice form has actually rendered
     * — fillInvoiceNumber() then times out waiting for a field that was
     * never there. Waiting for the field itself here, rather than trying to
     * time the click right, is the reliable fix regardless of what's slow
     * upstream.
     */
    public void clickCreateInvoice(){
        click(CREATE_INVOICE);
        waitVisible(INVOICE_NUMBER);
    }

    /**
     * Confirmed live 2026-09-04: Invoice Number gets an async server-side
     * validation the same way At Fault Claim Number does (see
     * fillAtFaultClaimNumber() Javadoc) — a diagnostic dump of ng-invalid
     * controls right before Submit showed #invoiceNumber carrying a
     * "server-error-border" class when the field wasn't blurred and given
     * time to settle first, blocking Submit Request indefinitely even
     * though the value itself was fine.
     */
    public void fillInvoiceNumber(String number){
        fill(INVOICE_NUMBER, number);
        page.locator(INVOICE_NUMBER).blur();
        waitForNetworkIdleBestEffort();
    }

    /**
     * Confirmed live 2026-09-04: without a blur, #invoiceAmount stayed
     * "ng-untouched ng-invalid" per the same diagnostic dump described on
     * fillInvoiceNumber() — Angular's validators only run once the control
     * is marked touched.
     */
    public void fillInvoiceAmount(String costOFRepair){
        fill(INVOICE_AMOUNT, costOFRepair);
        page.locator(INVOICE_AMOUNT).blur();
        waitForNetworkIdleBestEffort();
    }

    /**
     * Confirmed live 2026-09-04: setting the hidden input's files directly
     * via locator(UPLOAD_DOC_INVOICE).setInputFiles() never registers here —
     * input.files.length reads back 0 immediately after the call and the
     * filename never appears in the uploader's own text, even retried.
     * Driving it through a real file-chooser interaction (click, intercept
     * the resulting dialog) still didn't register — a network capture
     * showed the click opens the OS file dialog (confirming the label is
     * the real trigger — clicking the wrapping container instead doesn't
     * even open the dialog, confirmed live 2026-09-04) but the component
     * never shows the filename afterward. Explicitly re-dispatching input
     * and change on the file-chooser's own target element after
     * setFiles() covers the case where the component's (change) handler
     * needs a native-feeling event it isn't getting from Playwright's
     * default dispatch.
     */
    public void uploadDocumentsInsideInvoice(String filePath){
        com.microsoft.playwright.FileChooser fileChooser = page.waitForFileChooser(() ->
                page.locator(INVOICE_DOC_UPLOADER).getByText("Upload Credit Note Documents").click());
        fileChooser.setFiles(Paths.get(filePath));
        fileChooser.element().evaluate(
                "el => { el.dispatchEvent(new Event('input', {bubbles: true})); el.dispatchEvent(new Event('change', {bubbles: true})); }");
        String fileNamePrefix = Paths.get(filePath).getFileName().toString().substring(0, 10);
        page.locator(INVOICE_DOC_UPLOADER).getByText(fileNamePrefix)
                .waitFor(new Locator.WaitForOptions().setTimeout(10000));
    }

    /**
     * Confirmed live 2026-09-04: filling Invoice Amount, uploading the
     * document, and filling Invoice Number as three separate independent
     * steps is unreliable regardless of order — this form re-renders at
     * unpredictable points while settling (an async validator, the file
     * uploader's own dynamic-id churn, or both), and whichever field was
     * filled earliest is the one most likely to have been silently wiped
     * by the time Submit is clicked. Filling all three, then verifying all
     * three actually stuck, and re-filling the whole set from scratch if
     * not, is more reliable than trying to out-guess which single field
     * needs a wait or a different fill order.
     */
    public void fillInvoiceForm(String invoiceNumber, String amount, String filePath) {
        String fileNamePrefix = Paths.get(filePath).getFileName().toString().substring(0, 10);
        for (int attempt = 1; attempt <= 3; attempt++) {
            fillInvoiceAmount(amount);
            try {
                uploadDocumentsInsideInvoice(filePath);
            } catch (com.microsoft.playwright.PlaywrightException e) {
                log.debug("fillInvoiceForm attempt {}: document upload didn't settle: {}", attempt, e.getMessage());
            }
            fillInvoiceNumber(invoiceNumber);

            boolean amountOk = amount.equals(page.locator(INVOICE_AMOUNT).inputValue());
            boolean numberOk = invoiceNumber.equals(page.locator(INVOICE_NUMBER).inputValue());
            boolean fileOk = page.locator(INVOICE_DOC_UPLOADER).getByText(fileNamePrefix).count() > 0;
            if (amountOk && numberOk && fileOk) {
                return;
            }
            log.debug("fillInvoiceForm attempt {} didn't stick (amountOk={}, numberOk={}, fileOk={}), retrying.",
                    attempt, amountOk, numberOk, fileOk);
        }
        log.warn("Invoice form fields may not all have stuck after 3 full attempts — Submit Request will likely stay disabled.");
    }

    public void clickResetData(){
        click(RESET_DATA);
    }

    /**
     * Confirmed live 2026-09-04: a single refresh() right after the other
     * actor's mutation (Submit Request, Accept, etc.) isn't reliably enough
     * — network-idle only covers the browser's own in-flight requests, not
     * how long the backend takes to actually process and expose the change
     * to the other actor's session. Retrying refresh + a short wait for
     * Respond to appear, instead of a single refresh + immediate click,
     * absorbs that extra backend lag without guessing a fixed delay.
     */
    public void clickRespondBtn(){
        for (int attempt = 1; attempt <= 4; attempt++) {
            try {
                page.locator(RESPOND).waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE).setTimeout(7000));
                click(RESPOND);
                return;
            } catch (com.microsoft.playwright.PlaywrightException e) {
                log.debug("clickRespondBtn attempt {}: Respond not visible yet, refreshing and retrying: {}", attempt, e.getMessage());
                refresh();
            }
        }
        click(RESPOND);
    }

    /* ── Settlement / Payment / Claim Closure ─────────────────────────── */

    /**
     * At-fault side clicks this on the claim's Settlement panel to start
     * paying off the accepted invoice — confirmed live 2026-09-04, lands on
     * the Bulk Settlement Payment claim-picker page (see
     * REQUEST_CREDIT_NOTE_BTN Javadoc).
     */
    public void clickRequestCreditNote(){
        click(REQUEST_CREDIT_NOTE_BTN);
        page.waitForURL(url -> url.contains("bulk-settlement-payment"),
                new Page.WaitForURLOptions().setTimeout(15000));
    }

    /**
     * Checks this claim's row checkbox on the Bulk Settlement Payment (or
     * Credit Note Bulk Details) page — both list claims by Serial Number in
     * the first data column, confirmed live 2026-09-04. Selecting a single
     * claim's checkbox (rather than the header "select all") keeps the
     * settlement scoped to just this claim instead of batching in whatever
     * else happens to be unsettled for the same claimant company.
     */
    public void selectClaimForSettlement(String claimSerialNumber){
        page.locator("tr:has-text('" + claimSerialNumber + "') input[type='checkbox']").check();
    }

    /**
     * Right-hand "Total Selected Claims" panel on the Bulk Settlement
     * Payment / Credit Note Bulk Details pages. Parsed off the whole page's
     * text by label rather than a guessed container class, since the panel
     * isn't wrapped in anything with a stable, predictable class name.
     */
    public String getNumberOfClaimsSelected(){
        String pageText = page.locator("body").innerText();
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("Number Of Claims Selected\\s*\\n?\\s*(\\d+)").matcher(pageText);
        return m.find() ? m.group(1) : "";
    }

    /** Same "Total Selected Claims" panel's Total Amount — see getNumberOfClaimsSelected() Javadoc for why this is parsed by label. */
    public String getBulkSettlementTotalAmount(){
        String pageText = page.locator("body").innerText();
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("Total Amount\\s*\\n?\\s*([\\d,.]+)").matcher(pageText);
        return m.find() ? m.group(1) : "";
    }

    /**
     * On the Bulk Settlement Payment page, this creates the credit note
     * bulk record and navigates to /entity-portal/credit-note-bulk —
     * confirmed live 2026-09-04. The claim disappears from any FUTURE
     * Bulk Settlement Payment listing once this succeeds (it's now awaiting
     * payment, not awaiting a credit note). Confirmed live 2026-09-04 that
     * this navigation is flaky — the click fires and the "Credit Note
     * requested successfully." toast confirms the API call succeeded, but
     * the client-side route change to credit-note-bulk sometimes doesn't
     * happen before the next action runs. Waiting for the URL itself,
     * rather than just the click, is the reliable signal.
     */
    public void clickRequestCreditNoteToCreateBulk(){
        click(REQUEST_CREDIT_NOTE_BTN);
        page.waitForURL(url -> url.contains("credit-note-bulk"),
                new Page.WaitForURLOptions().setTimeout(15000));
    }

    /**
     * On the Credit Note Bulk Details page, navigates to a real external
     * payment gateway (RAK Bank's ATB Pay, rak.atbpay.me) in the SAME tab —
     * confirmed live 2026-09-04, Playwright's page/locator calls work on it
     * unchanged since it's a same-tab navigation, not a popup or iframe.
     * Confirmed live 2026-09-04: Playwright's coordinate-based mouse click
     * on this button (normal or forced) reliably resolves the element,
     * finds it visible/enabled/stable, and completes the click action
     * without error — but repeatedly (5/5 runs) never actually navigates,
     * with no popup and no new context page opened either. Manual clicks
     * in a real browser always worked. A direct DOM element.click() call
     * (bypassing coordinate/viewport-based mouse simulation entirely)
     * fires the same native click event a real click would, and is
     * retried since the app-side navigation itself still occasionally
     * doesn't fire on the first attempt.
     */
    public void clickProceedToPayment(){
        String creditNoteBulkUrl = page.url();
        try {
            page.locator(".toast-message").waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.DETACHED).setTimeout(15000));
        } catch (com.microsoft.playwright.PlaywrightException e) {
            log.debug("Toast didn't clear in time before Proceed To Payment: {}", e.getMessage());
        }
        for (int attempt = 1; attempt <= 3; attempt++) {
            if (attempt > 1) {
                // A prior attempt's checkout session never rendered (or the
                // gateway bounced to its own error callback) — navigate
                // straight back to this exact page for a fresh checkoutId
                // rather than relying on browser history, which gets
                // ambiguous once the gateway itself has done extra
                // navigations (e.g. its own callback redirect on failure).
                page.navigate(creditNoteBulkUrl);
                page.locator(PROCEED_TO_PAYMENT_BTN).waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE).setTimeout(15000));
            }
            page.locator(PROCEED_TO_PAYMENT_BTN).evaluate("el => el.click()");
            try {
                page.waitForURL(url -> url.contains("atbpay.me"),
                        new Page.WaitForURLOptions().setTimeout(15000));
            } catch (com.microsoft.playwright.PlaywrightException e) {
                log.debug("Proceed To Payment attempt {} didn't navigate, retrying: {}", attempt, e.getMessage());
                continue;
            }
            try {
                // Confirmed live 2026-09-04: the ATB checkout page sometimes
                // gets stuck showing only "English" (the language selector)
                // forever — its console logs a Subresource Integrity
                // failure loading Mastercard's SRC UI-kit script (a
                // stale/mismatched integrity hash on Mastercard's/ATB's own
                // CDN, not anything under this app's or this test's control
                // — no amount of waiting, clicking, or selector changes
                // fixes a browser correctly refusing to run a corrupted
                // script). Reloading in place was tried and made things
                // worse — it can trigger the gateway's own
                // error-handling redirect to a broken callback state
                // instead of cleanly retrying — so a failed render here
                // just abandons the session for attempt N+1 to get a fresh
                // one instead.
                page.locator(CHECKOUT_CONFIRM_BTN).waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE).setTimeout(20000));
                return;
            } catch (com.microsoft.playwright.PlaywrightException e) {
                log.debug("Checkout attempt {} landed on atbpay.me but never rendered, abandoning for a fresh session: {}", attempt, e.getMessage());
            }
        }
        throw new IllegalStateException("Proceed To Payment never reached a fully-rendered ATB checkout after 3 attempts.");
    }

    /**
     * ATB Checkout page's Account panel — "Account Number : 113550369" /
     * "Balance : 266097.27" as plain text, no real ids — confirmed live
     * 2026-09-04. Parsed off the whole page's text by label, same approach
     * as getNumberOfClaimsSelected() — a direct text=/regex/ locator here
     * timed out, meaning the label and value aren't a single matchable text
     * node the way they appeared visually.
     */
    public String getCheckoutAccountNumber(){
        return extractByLabel("Account Number\\s*:\\s*(\\S+)");
    }

    public String getCheckoutBalance(){
        return extractByLabel("Balance\\s*:\\s*([\\d,.]+)");
    }

    /** "Total Amount" row in the Invoice Details panel — confirmed live 2026-09-04 (includes settlement fees + tax, not just the invoice amount). */
    public String getCheckoutTotalAmount(){
        return extractByLabel("Total Amount\\s*\\n?\\s*([\\d,.]+)");
    }

    private String extractByLabel(String regex){
        String pageText = page.locator("body").innerText();
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(regex).matcher(pageText);
        return m.find() ? m.group(1) : "";
    }

    /** The only checkbox on the ATB checkout page — confirmed live 2026-09-04. */
    public void agreeToCheckoutTerms(){
        page.locator(CHECKOUT_TERMS_CHECKBOX).check();
    }

    /**
     * Confirmed live 2026-09-04: this executes a real settlement payment
     * against the entity's wallet via the ATB gateway — not a simulated or
     * sandboxed no-op. Only call this with explicit authorization for the
     * financial-transaction side effect it causes; no enabled test in this
     * suite calls it today. On success, redirects to
     * /entity-portal/payment-success?transactionId=... in the same tab.
     */
    public void clickCheckoutConfirm(){
        click(CHECKOUT_CONFIRM_BTN);
    }

    /** True once redirected back to Mosadad's payment-success page after a real Confirm — confirmed live 2026-09-04. */
    public boolean isPaymentSuccessful(){
        return currentUrl().contains("payment-success");
    }

    /**
     * Claimant-side button that appears on Claim Closure once Settlement is
     * done — confirmed live 2026-09-04. Reveals a "Do You Want To Finalize
     * This Claim?" panel, not an immediate action.
     */
    public void clickProceedToClaimClosure(){
        click(PROCEED_TO_CLAIM_CLOSURE_BTN);
    }

    /** Opens the "Confirm Claim Closure" modal (irreversible warning + optional remarks) — confirmed live 2026-09-04. */
    public void clickFinalizeClaim(){
        click(FINALIZE_CLAIM_BTN);
    }

    /** Confirms the "Confirm Claim Closure" modal — irreversible, closes the claim for good. Confirmed live 2026-09-04. */
    public void confirmClaimClosure(){
        click(CONFIRM_CLAIM_CLOSURE_BTN);
    }

    /** "Claim Finalized" success state shown after confirmClaimClosure() — confirmed live 2026-09-04. */
    public boolean isClaimFinalized(){
        return page.getByText("Claim Finalized").isVisible();
    }

    /* ── Mandatory check ──────────────────────────────────────────────── */

    /**
     * Verifies every value actually SET on submitted made it onto this
     * claim summary page unchanged — the round-trip check
     * CreateManualClaimPage.enterAllRecoveryClaimsDetails() exists to
     * support. Only fields the caller set are checked — a null field on
     * submitted means "wasn't set", not "should be blank" (same contract
     * as enterAllRecoveryClaimsDetails() itself) — so this never fails on
     * a field the caller never populated. See the class Javadoc for the
     * fields that can't be exactly compared (masked VIN, Policy Expiry
     * Date has no concrete expected value) and the two label renames
     * (Accident Source/Description).
     */
    public void validateAllDetailsOfOpenRecoveryClaimsPage(ManualClaimDetails submitted) {
        assertMatchesIfSet("Claimant Claim Number", getClaimantClaimNumber(), submitted.getClaimantClaimNumber());
        assertMatchesIfSet("Report Number", getReportNumber(), submitted.getReportNumber());
        assertMatchesIfSet("Accident Type", getAccidentType(), submitted.getAccidentType());
        assertMatchesIfSet("Emirate", getEmirate(), submitted.getEmirate());
        assertMatchesIfSet("City", getCity(), submitted.getCity());
        assertMatchesIfSet("Area", getArea(), submitted.getArea());
        assertMatchesIfSet("Street", getStreet(), submitted.getStreet());
        assertMatchesIfSet("Intersection", getIntersection(), submitted.getIntersection());
        assertMatchesIfSet("Accident Source (shown as \"Report Source\" on this page)", getAccidentSourceDisplayed(), submitted.getAccidentSource());
        assertMatchesIfSet("Accident Description (shown as \"Report Description\" on this page)", getAccidentDescriptionDisplayed(), submitted.getAccidentDescription());

        // Accident Date & Time is shown combined ("DD/MM/YYYY HH:mm"), but
        // an exact date can't be asserted: CreateManualClaimPage's
        // pickAccidentDateToday() picks a real PAST date (2 days back, not
        // literally today — see its Javadoc for why), and
        // ManualClaimDetails only carries the "was it set" boolean, not the
        // concrete date picked, same limitation as Policy Expiry Date (see
        // class Javadoc). Only the time half and presence can be checked.
        if (submitted.getAccidentTime() != null) {
            assertThat(getAccidentDateAndTime())
                    .as("Accident Date & Time should contain the submitted time")
                    .contains(submitted.getAccidentTime());
        }
        // Report Date has the same limitation as Accident Date above — see
        // pickReportDateToday() Javadoc — so only presence can be checked.
        if (submitted.isReportDateToday()) {
            assertThat(getReportDate()).as("Report Date should be populated").isNotBlank();
        }

        assertMatchesIfSet("Claimant Policy Number", getClaimantPolicyNumberDisplayed(), submitted.getClaimantPolicyNumber());
        assertMatchesIfSet("Claimant Policy Type", getClaimantPolicyTypeDisplayed().trim(), submitted.getClaimantPolicyType());
        if (submitted.getClaimantChassisNumber() != null) {
            assertThat(getClaimantVinNumberMasked())
                    .as("Claimant VIN Number (masked on this page — prefix only)")
                    .startsWith(submitted.getClaimantChassisNumber().substring(0, 8));
        }

        assertMatchesIfSet("At-fault Insurance Company", getAtFaultInsuranceCompanyDisplayed(), submitted.getAtFaultInsuranceCompany());
        assertMatchesIfSet("At-fault Policy Number", getAtFaultPolicyNumberDisplayed(), submitted.getAtFaultPolicyNumber());
        assertMatchesIfSet("At-fault Policy Type", getAtFaultPolicyTypeDisplayed().trim(), submitted.getAtFaultPolicyType());
        if (submitted.getAtFaultChassisNumber() != null) {
            assertThat(getAtFaultVinNumberMasked())
                    .as("At-fault VIN Number (masked on this page — prefix only)")
                    .startsWith(submitted.getAtFaultChassisNumber().substring(0, 8));
        }

        // Damaged Parts only ever shows the claimant side by design — see
        // getClaimantDamagedParts() Javadoc.
        if (submitted.getClaimantDamagedParts() != null) {
            assertThat(getClaimantDamagedParts())
                    .as("Damaged Parts should include the claimant's submitted value")
                    .contains(submitted.getClaimantDamagedParts());
        }
    }

    /**
     * Only asserts when expected is non-null — see
     * validateAllDetailsOfOpenRecoveryClaimsPage() Javadoc for why.
     */
    private void assertMatchesIfSet(String label, String actual, String expected) {
        if (expected != null) {
            assertThat(actual).as(label).isEqualTo(expected);
        }
    }

    /**
     * Same overlay-close race as CreateManualClaimPage's mat-selects (see
     * its Javadoc) — the Workshop Name mat-select on this page needs the
     * same wait before the next interaction.
     */
    private void waitForOverlayToClose() {
        page.keyboard().press("Escape");
        page.locator(".cdk-overlay-backdrop").waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.DETACHED)
                .setTimeout(5000));
    }
}
