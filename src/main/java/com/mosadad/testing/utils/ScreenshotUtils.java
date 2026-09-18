package com.mosadad.testing.utils;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.ScreenshotType;
import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ScreenshotUtils {

    private static final Logger log = LogManager.getLogger(ScreenshotUtils.class);
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final Path SCREENSHOT_DIR = Paths.get("target/screenshots");

    private ScreenshotUtils() {}

    /**
     * Takes a screenshot, saves it to target/screenshots/, and attaches it
     * to the current Allure report step so it is visible inline in the report.
     */
    public static void captureAndAttach(Page page, String testName) {
        if (page == null) return;
        try {
            byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setType(ScreenshotType.PNG));

            Allure.addAttachment(
                    "Screenshot — " + testName,
                    "image/png",
                    new ByteArrayInputStream(screenshot),
                    "png"
            );

            Files.createDirectories(SCREENSHOT_DIR);
            String fileName = testName.replaceAll("[^a-zA-Z0-9_]", "_")
                    + "_" + LocalDateTime.now().format(TS_FMT) + ".png";
            Files.write(SCREENSHOT_DIR.resolve(fileName), screenshot);
            log.info("Screenshot saved: {}", fileName);

        } catch (IOException e) {
            log.warn("Could not save screenshot for '{}': {}", testName, e.getMessage());
        } catch (Exception e) {
            log.warn("Could not capture screenshot for '{}': {}", testName, e.getMessage());
        }
    }
}
