package com.mosadad.testing.listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IExecutionListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Builds a single-file, emailable Allure report at the end of every TestNG
 * run: reports/Allure_<suite>_<timestamp>.html. Single-file mode inlines all
 * data and screenshots, so the HTML opens straight from an email attachment
 * (a normal Allure report is a folder that needs a web server).
 *
 * Each run clears target/allure-results first so the report covers that run
 * only — otherwise results from every run since the last `mvn clean` pile up.
 *
 * Runs inside the test JVM rather than as a Maven goal so it also fires when
 * tests fail (Surefire fails the build before any later goal would run).
 * Never fails the run itself: any problem is logged and the report skipped.
 *
 * Registered via META-INF/services/org.testng.ITestNGListener so it applies
 * to every suite XML. Opt out with -Dallure.report.skip=true.
 */
public class AllureReportListener implements IExecutionListener, ISuiteListener {

    private static final Logger log = LogManager.getLogger(AllureReportListener.class);

    private static final Path RESULTS_DIR = Paths.get("target", "allure-results");
    private static final Path WORK_DIR = Paths.get("target", "allure-single-file");
    private static final Path REPORTS_DIR = Paths.get("reports");
    private static final Path ALLURE_HOME = Paths.get(".allure", "allure-2.29.0", "bin");
    private static final long GENERATE_TIMEOUT_MINUTES = 5;

    private String suiteName;

    @Override
    public void onExecutionStart() {
        if (isSkipped()) {
            return;
        }
        deleteRecursively(RESULTS_DIR);
    }

    @Override
    public void onStart(ISuite suite) {
        if (suiteName == null) {
            suiteName = suite.getName();
        }
    }

    @Override
    public void onExecutionFinish() {
        if (isSkipped()) {
            log.info("Allure report skipped (allure.report.skip=true)");
            return;
        }
        try {
            generateReport();
        } catch (Exception e) {
            log.warn("Could not generate emailable Allure report: {}", e.getMessage());
        }
    }

    private void generateReport() throws IOException, InterruptedException {
        if (!Files.isDirectory(RESULTS_DIR)) {
            log.warn("No Allure results at {} — skipping report", RESULTS_DIR);
            return;
        }
        // ASCII only: suite names use an em dash, which cmd.exe can mangle.
        String reportTitle = suiteName != null ? suiteName.replace('—', '-') : "Mosadad";
        List<String> command = allureCommand();
        command.addAll(List.of("generate", RESULTS_DIR.toString(),
                "--single-file", "--clean",
                "--report-name", reportTitle,
                "-o", WORK_DIR.toString()));

        Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .start();
        if (!process.waitFor(GENERATE_TIMEOUT_MINUTES, TimeUnit.MINUTES)) {
            process.destroyForcibly();
            log.warn("Allure report generation timed out after {} min", GENERATE_TIMEOUT_MINUTES);
            return;
        }
        Path generated = WORK_DIR.resolve("index.html");
        if (process.exitValue() != 0 || !Files.exists(generated)) {
            log.warn("Allure generate exited with code {} — no report written", process.exitValue());
            return;
        }

        Files.createDirectories(REPORTS_DIR);
        String fileName = "Allure_" + sanitize(reportTitle) + "_"
                + DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss").format(LocalDateTime.now()) + ".html";
        Path report = REPORTS_DIR.resolve(fileName);
        Files.copy(generated, report, StandardCopyOption.REPLACE_EXISTING);
        log.info("Emailable Allure report: {}", report.toAbsolutePath());
    }

    /**
     * Prefers the Allure CLI committed under .allure/, falls back to `allure`
     * on PATH. The bundled path stays relative (Surefire runs from the project
     * root): cmd /c mangles a quoted absolute path containing spaces.
     */
    private static List<String> allureCommand() {
        boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
        Path bundled = ALLURE_HOME.resolve(windows ? "allure.bat" : "allure");
        String executable = Files.exists(bundled) ? bundled.toString() : "allure";
        List<String> command = new ArrayList<>();
        if (windows) {
            command.add("cmd");
            command.add("/c");
        }
        command.add(executable);
        return command;
    }

    private static String sanitize(String name) {
        return name.replaceAll("[^A-Za-z0-9]+", "-").replaceAll("^-|-$", "");
    }

    private static boolean isSkipped() {
        return Boolean.getBoolean("allure.report.skip");
    }

    private static void deleteRecursively(Path dir) {
        if (!Files.exists(dir)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
        } catch (IOException e) {
            log.warn("Could not clear {}: {}", dir, e.getMessage());
        }
    }
}
