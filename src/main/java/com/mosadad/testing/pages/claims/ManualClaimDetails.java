package com.mosadad.testing.pages.claims;

/**
 * Plain data holder for every value CreateManualClaimPage accepts — one
 * field per fill()/select() method on that page. Field grouping and order
 * mirror the page's own sections (Accident Details / Claimant Details /
 * At-fault Details / Attachments) for easy cross-reference.
 *
 * The 4 date fields (accidentDate, reportDate, claimantPolicyExpiryDate,
 * atFaultPolicyExpiryDate) are booleans, not date strings — the page can
 * currently only pick "today" via the Material Datepicker (see
 * CreateManualClaimPage's Javadoc: arbitrary past dates are TODO, not yet
 * implemented). true means "set this field to today"; false/unset means
 * "leave it alone".
 *
 * All other fields are nullable — a null String means "don't touch this
 * field", not "clear it". See
 * CreateManualClaimPage.enterAllRecoveryClaimsDetails(ManualClaimDetails),
 * which only calls a field's setter when it's non-null (or true, for the
 * date booleans).
 *
 * No all-args constructor on purpose — with 38 fields, positional
 * arguments would be unreadable and easy to mix up. Build one with the
 * no-arg constructor and the individual setters instead:
 * <pre>{@code
 * ManualClaimDetails details = new ManualClaimDetails();
 * details.setReportNumber("RPT-0001");
 * details.setAccidentType("Collision");
 * ...
 * }</pre>
 */
public class ManualClaimDetails {

    /* ── Accident Details ─────────────────────────────────────────────── */
    private String reportNumber;
    private boolean accidentDateToday;
    private String accidentTime;
    private String accidentType;
    private String emirate;
    private String city;
    private String area;
    private String intersection;
    private String street;
    private boolean reportDateToday;
    private String accidentSource;
    private String accidentDescription;

    /* ── Claimant Details ─────────────────────────────────────────────── */
    private String claimantClaimNumber;
    private String claimantPlateNumber;
    private String claimantPolicyNumber;
    private boolean claimantPolicyExpiryDateToday;
    private String claimantChassisNumber;
    private String claimantVehicleYear;
    private String claimantPlateSource;
    private String claimantPlateColor;
    private String claimantPolicyType;
    private String claimantVehicleMake;
    private String claimantVehicleModel;
    private String claimantDamagedParts;
    // Claimant's Insurance Company is disabled/prefilled by the app (verified live) — no field for it.

    /* ── At-fault Details ─────────────────────────────────────────────── */
    private String atFaultInsuranceCompany;
    private String atFaultPlateNumber;
    private String atFaultPolicyNumber;
    private boolean atFaultPolicyExpiryDateToday;
    private String atFaultChassisNumber;
    private String atFaultVehicleYear;
    private String atFaultPlateSource;
    private String atFaultPlateColor;
    private String atFaultPolicyType;
    private String atFaultVehicleMake;
    private String atFaultVehicleModel;
    private String atFaultDamagedParts;

    /* ── Attachments Uploaders ────────────────────────────────────────── */
    /** Required by the app ("Upload Claim Documents *"). */
    private String claimDocumentsFilePath;
    /** Optional ("Upload Other Documents", no asterisk). */
    private String otherDocumentsFilePath;

    public ManualClaimDetails() {}

    /* ── Accident Details getters/setters ─────────────────────────────── */

    public String getReportNumber() { return reportNumber; }
    public void setReportNumber(String reportNumber) { this.reportNumber = reportNumber; }

    public boolean isAccidentDateToday() { return accidentDateToday; }
    public void setAccidentDateToday(boolean accidentDateToday) { this.accidentDateToday = accidentDateToday; }

    public String getAccidentTime() { return accidentTime; }
    public void setAccidentTime(String accidentTime) { this.accidentTime = accidentTime; }

    public String getAccidentType() { return accidentType; }
    public void setAccidentType(String accidentType) { this.accidentType = accidentType; }

    public String getEmirate() { return emirate; }
    public void setEmirate(String emirate) { this.emirate = emirate; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getIntersection() { return intersection; }
    public void setIntersection(String intersection) { this.intersection = intersection; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public boolean isReportDateToday() { return reportDateToday; }
    public void setReportDateToday(boolean reportDateToday) { this.reportDateToday = reportDateToday; }

    public String getAccidentSource() { return accidentSource; }
    public void setAccidentSource(String accidentSource) { this.accidentSource = accidentSource; }

    public String getAccidentDescription() { return accidentDescription; }
    public void setAccidentDescription(String accidentDescription) { this.accidentDescription = accidentDescription; }

    /* ── Claimant Details getters/setters ─────────────────────────────── */

    public String getClaimantClaimNumber() { return claimantClaimNumber; }
    public void setClaimantClaimNumber(String claimantClaimNumber) { this.claimantClaimNumber = claimantClaimNumber; }

    public String getClaimantPlateSource() { return claimantPlateSource; }
    public void setClaimantPlateSource(String claimantPlateSource) { this.claimantPlateSource = claimantPlateSource; }

    public String getClaimantPlateColor() { return claimantPlateColor; }
    public void setClaimantPlateColor(String claimantPlateColor) { this.claimantPlateColor = claimantPlateColor; }

    public String getClaimantPlateNumber() { return claimantPlateNumber; }
    public void setClaimantPlateNumber(String claimantPlateNumber) { this.claimantPlateNumber = claimantPlateNumber; }

    public String getClaimantPolicyNumber() { return claimantPolicyNumber; }
    public void setClaimantPolicyNumber(String claimantPolicyNumber) { this.claimantPolicyNumber = claimantPolicyNumber; }

    public boolean isClaimantPolicyExpiryDateToday() { return claimantPolicyExpiryDateToday; }
    public void setClaimantPolicyExpiryDateToday(boolean claimantPolicyExpiryDateToday) { this.claimantPolicyExpiryDateToday = claimantPolicyExpiryDateToday; }

    public String getClaimantPolicyType() { return claimantPolicyType; }
    public void setClaimantPolicyType(String claimantPolicyType) { this.claimantPolicyType = claimantPolicyType; }

    public String getClaimantChassisNumber() { return claimantChassisNumber; }
    public void setClaimantChassisNumber(String claimantChassisNumber) { this.claimantChassisNumber = claimantChassisNumber; }

    public String getClaimantVehicleMake() { return claimantVehicleMake; }
    public void setClaimantVehicleMake(String claimantVehicleMake) { this.claimantVehicleMake = claimantVehicleMake; }

    public String getClaimantVehicleModel() { return claimantVehicleModel; }
    public void setClaimantVehicleModel(String claimantVehicleModel) { this.claimantVehicleModel = claimantVehicleModel; }

    public String getClaimantVehicleYear() { return claimantVehicleYear; }
    public void setClaimantVehicleYear(String claimantVehicleYear) { this.claimantVehicleYear = claimantVehicleYear; }

    public String getClaimantDamagedParts() { return claimantDamagedParts; }
    public void setClaimantDamagedParts(String claimantDamagedParts) { this.claimantDamagedParts = claimantDamagedParts; }

    /* ── At-fault Details getters/setters ─────────────────────────────── */

    public String getAtFaultInsuranceCompany() { return atFaultInsuranceCompany; }
    public void setAtFaultInsuranceCompany(String atFaultInsuranceCompany) { this.atFaultInsuranceCompany = atFaultInsuranceCompany; }

    public String getAtFaultPlateSource() { return atFaultPlateSource; }
    public void setAtFaultPlateSource(String atFaultPlateSource) { this.atFaultPlateSource = atFaultPlateSource; }

    public String getAtFaultPlateColor() { return atFaultPlateColor; }
    public void setAtFaultPlateColor(String atFaultPlateColor) { this.atFaultPlateColor = atFaultPlateColor; }

    public String getAtFaultPlateNumber() { return atFaultPlateNumber; }
    public void setAtFaultPlateNumber(String atFaultPlateNumber) { this.atFaultPlateNumber = atFaultPlateNumber; }

    public String getAtFaultChassisNumber() { return atFaultChassisNumber; }
    public void setAtFaultChassisNumber(String atFaultChassisNumber) { this.atFaultChassisNumber = atFaultChassisNumber; }

    public String getAtFaultPolicyNumber() { return atFaultPolicyNumber; }
    public void setAtFaultPolicyNumber(String atFaultPolicyNumber) { this.atFaultPolicyNumber = atFaultPolicyNumber; }

    public boolean isAtFaultPolicyExpiryDateToday() { return atFaultPolicyExpiryDateToday; }
    public void setAtFaultPolicyExpiryDateToday(boolean atFaultPolicyExpiryDateToday) { this.atFaultPolicyExpiryDateToday = atFaultPolicyExpiryDateToday; }

    public String getAtFaultPolicyType() { return atFaultPolicyType; }
    public void setAtFaultPolicyType(String atFaultPolicyType) { this.atFaultPolicyType = atFaultPolicyType; }

    public String getAtFaultVehicleMake() { return atFaultVehicleMake; }
    public void setAtFaultVehicleMake(String atFaultVehicleMake) { this.atFaultVehicleMake = atFaultVehicleMake; }

    public String getAtFaultVehicleModel() { return atFaultVehicleModel; }
    public void setAtFaultVehicleModel(String atFaultVehicleModel) { this.atFaultVehicleModel = atFaultVehicleModel; }

    public String getAtFaultVehicleYear() { return atFaultVehicleYear; }
    public void setAtFaultVehicleYear(String atFaultVehicleYear) { this.atFaultVehicleYear = atFaultVehicleYear; }

    public String getAtFaultDamagedParts() { return atFaultDamagedParts; }
    public void setAtFaultDamagedParts(String atFaultDamagedParts) { this.atFaultDamagedParts = atFaultDamagedParts; }

    /* ── Attachments getters/setters ──────────────────────────────────── */

    public String getClaimDocumentsFilePath() { return claimDocumentsFilePath; }
    public void setClaimDocumentsFilePath(String claimDocumentsFilePath) { this.claimDocumentsFilePath = claimDocumentsFilePath; }

    public String getOtherDocumentsFilePath() { return otherDocumentsFilePath; }
    public void setOtherDocumentsFilePath(String otherDocumentsFilePath) { this.otherDocumentsFilePath = otherDocumentsFilePath; }
}
