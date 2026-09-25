package com.mosadad.testing.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Builds Fast Track upload rows with fresh random data on every call, since
 * the app rejects duplicate claims. Columns are the ones the stage Union
 * account's "Download Template" returns (verified live 2026-09-24) — the
 * required set comes from that entity's Smart Loader mapping, so another
 * account may need different headers. Don't use the old Fast Track333.xlsx
 * template — its columns don't match.
 */
public final class FastTrackTestDataGenerator {

    public static final String COL_CLAIM_NUMBER = "Claim No.";
    public static final String COL_ACCIDENT_NO = "Accident no";
    public static final String COL_ACCIDENT_DATE = "Accident date";
    public static final String COL_PLATE = "Plate claimant";
    public static final String COL_ATFAULT_PLATE_NUMBER = "Atfault_plate_number";
    public static final String COL_RECOVERY_CLAIM_AMOUNT = "Recovery_claim_amount";

    /** Header of the column the at-fault insurer fills in the sheet from "Download Sheet". */
    public static final String COL_AT_FAULT_CLAIM_NUMBER = "At Fault Claim Number";

    private static final List<String> COLUMNS = List.of(
            COL_CLAIM_NUMBER, COL_ACCIDENT_NO, COL_ACCIDENT_DATE, COL_PLATE,
            COL_ATFAULT_PLATE_NUMBER, COL_RECOVERY_CLAIM_AMOUNT
    );

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final List<String> EMIRATES = List.of("Sharjah", "Dubai", "Abu Dhabi", "Ajman", "Ras Al Khaimah");

    private FastTrackTestDataGenerator() {}

    /** One freshly-unique row per amount — used to stay under the auto-accept threshold. */
    public static List<Map<String, String>> generateRowsWithAmounts(int... recoveryAmountsAed) {
        List<Map<String, String>> rows = new ArrayList<>();
        for (int amount : recoveryAmountsAed) {
            rows.add(generateRow(amount));
        }
        return rows;
    }

    private static Map<String, String> generateRow(int recoveryAmountAed) {
        Map<String, String> row = new LinkedHashMap<>();
        row.put(COL_CLAIM_NUMBER, "C/SH/" + randomDigits(6) + "/" + randomDigits(4));
        row.put(COL_ACCIDENT_NO, randomDigits(12));
        row.put(COL_ACCIDENT_DATE, randomPastDate().format(DATE_FMT));
        row.put(COL_PLATE, randomPlate());
        row.put(COL_ATFAULT_PLATE_NUMBER, randomPlate());
        row.put(COL_RECOVERY_CLAIM_AMOUNT, formatAed(recoveryAmountAed));
        return row;
    }

    /** A fresh 12-digit at-fault claim number, the shape entered by hand on stage (e.g. 225588996633). */
    public static String newAtFaultClaimNumber() {
        return randomDigits(12);
    }

    /** Required core of each claim's document file name: the claim number without "/". */
    public static String claimNumberForFileName(String claimNumber) {
        return claimNumber.replace("/", "");
    }

    static String randomDigits(int length) {
        StringBuilder sb = new StringBuilder(length);
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            sb.append(rnd.nextInt(10));
        }
        return sb.toString();
    }

    private static String randomPlate() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        char letter = (char) ('A' + rnd.nextInt(26));
        int number = rnd.nextInt(10000, 99999);
        String emirate = EMIRATES.get(rnd.nextInt(EMIRATES.size()));
        return letter + " " + number + " " + emirate;
    }

    private static LocalDate randomPastDate() {
        return LocalDate.now().minusDays(ThreadLocalRandom.current().nextInt(1, 60));
    }

    /** The template's own example format, e.g. "5,000 AED". */
    private static String formatAed(int amount) {
        return String.format("%,d", amount) + " AED";
    }

    public static byte[] writeToXlsxBytes(List<Map<String, String>> rows) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Sheet1");

            Row headerRow = sheet.createRow(0);
            for (int c = 0; c < COLUMNS.size(); c++) {
                headerRow.createCell(c).setCellValue(COLUMNS.get(c));
            }

            for (int r = 0; r < rows.size(); r++) {
                Row dataRow = sheet.createRow(r + 1);
                Map<String, String> rowData = rows.get(r);
                for (int c = 0; c < COLUMNS.size(); c++) {
                    dataRow.createCell(c).setCellValue(rowData.getOrDefault(COLUMNS.get(c), ""));
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write Fast Track workbook", e);
        }
    }

    /** The at-fault's filled sheet plus the claim numbers written into it, in row order. */
    public record AtFaultSheet(byte[] xlsx, List<String> atFaultClaimNumbers) {}

    /**
     * Fills a fresh unique number into the {@value #COL_AT_FAULT_CLAIM_NUMBER}
     * column of every data row of the sheet from the at-fault's "Download Sheet".
     * The header match ignores case and surrounding spaces.
     */
    public static AtFaultSheet fillAtFaultClaimNumbers(byte[] downloadedSheet) {
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(downloadedSheet))) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            Row headerRow = sheet.getRow(sheet.getFirstRowNum());

            int column = -1;
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                String header = formatter.formatCellValue(cell).trim();
                headers.add(header);
                if (header.equalsIgnoreCase(COL_AT_FAULT_CLAIM_NUMBER)) {
                    column = cell.getColumnIndex();
                }
            }
            if (column < 0) {
                throw new IllegalStateException("Downloaded at-fault sheet has no '" + COL_AT_FAULT_CLAIM_NUMBER
                        + "' column — headers were: " + headers);
            }

            List<String> atFaultClaimNumbers = new ArrayList<>();
            for (int r = sheet.getFirstRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                String atFaultClaimNumber = newAtFaultClaimNumber();
                Cell cell = row.getCell(column);
                (cell == null ? row.createCell(column) : cell).setCellValue(atFaultClaimNumber);
                atFaultClaimNumbers.add(atFaultClaimNumber);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new AtFaultSheet(out.toByteArray(), atFaultClaimNumbers);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to fill the at-fault Fast Track sheet", e);
        }
    }
}
