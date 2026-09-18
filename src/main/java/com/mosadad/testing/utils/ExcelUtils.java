package com.mosadad.testing.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Apache POI-backed reader for .xlsx/.xls test data, e.g.
 * src/main/resources/Fast Track333.xlsx — the Fast Track bulk-claim-upload
 * template referenced in MOSADAD_DOMAIN.md §Fast Track (claim number,
 * accident details, policy, invoice, recovery claim amount per row).
 *
 * Reads a classpath resource, not a filesystem path, so it works the same
 * whether the sheet lives under src/main/resources or src/test/resources —
 * both end up on the test classpath. WorkbookFactory auto-detects .xls vs
 * .xlsx from the stream, so callers don't need to pick XSSF/HSSF themselves.
 */
public final class ExcelUtils {

    private static final Logger log = LogManager.getLogger(ExcelUtils.class);

    private ExcelUtils() {}

    /** Reads the first sheet, keyed by header row (row 0). Fully blank rows are skipped. */
    public static List<Map<String, String>> readRowsAsMaps(String resourceName) {
        return readRowsAsMaps(resourceName, 0);
    }

    public static List<Map<String, String>> readRowsAsMaps(String resourceName, String sheetName) {
        try (InputStream is = openResource(resourceName);
             Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet '" + sheetName + "' not found in " + resourceName);
            }
            return readSheetAsMaps(sheet);
        } catch (IOException e) {
            throw new UncheckedExcelException("Failed to read " + resourceName, e);
        }
    }

    private static List<Map<String, String>> readRowsAsMaps(String resourceName, int sheetIndex) {
        try (InputStream is = openResource(resourceName);
             Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(sheetIndex);
            return readSheetAsMaps(sheet);
        } catch (IOException e) {
            throw new UncheckedExcelException("Failed to read " + resourceName, e);
        }
    }

    private static List<Map<String, String>> readSheetAsMaps(Sheet sheet) {
        DataFormatter formatter = new DataFormatter();
        List<Map<String, String>> rows = new ArrayList<>();

        Row headerRow = sheet.getRow(sheet.getFirstRowNum());
        if (headerRow == null) {
            log.warn("Sheet '{}' has no header row", sheet.getSheetName());
            return rows;
        }
        List<String> headers = new ArrayList<>();
        for (Cell cell : headerRow) {
            headers.add(formatter.formatCellValue(cell).trim());
        }

        for (int r = sheet.getFirstRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row == null || isRowBlank(row, headers.size(), formatter)) {
                continue;
            }
            Map<String, String> rowData = new LinkedHashMap<>();
            for (int c = 0; c < headers.size(); c++) {
                String header = headers.get(c);
                if (header.isEmpty()) continue;
                Cell cell = row.getCell(c);
                rowData.put(header, cell == null ? "" : formatter.formatCellValue(cell).trim());
            }
            rows.add(rowData);
        }
        log.info("Read {} data row(s) from sheet '{}'", rows.size(), sheet.getSheetName());
        return rows;
    }

    /**
     * TestNG @DataProvider-friendly form — each element is a single-arg
     * Object[] wrapping the row's header->value map, e.g.:
     * <pre>{@code
     * @DataProvider(name = "fastTrackClaims")
     * public Object[][] fastTrackClaims() {
     *     return ExcelUtils.readRowsAsDataProvider("Fast Track333.xlsx");
     * }
     * }</pre>
     */
    public static Object[][] readRowsAsDataProvider(String resourceName) {
        List<Map<String, String>> rows = readRowsAsMaps(resourceName);
        Object[][] data = new Object[rows.size()][1];
        for (int i = 0; i < rows.size(); i++) {
            data[i][0] = rows.get(i);
        }
        return data;
    }

    private static boolean isRowBlank(Row row, int columnCount, DataFormatter formatter) {
        for (int c = 0; c < columnCount; c++) {
            Cell cell = row.getCell(c);
            if (cell != null && !formatter.formatCellValue(cell).trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static InputStream openResource(String resourceName) {
        InputStream is = ExcelUtils.class.getClassLoader().getResourceAsStream(resourceName);
        if (is == null) {
            throw new IllegalArgumentException(resourceName + " not found on classpath");
        }
        return is;
    }

    public static class UncheckedExcelException extends RuntimeException {
        public UncheckedExcelException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
