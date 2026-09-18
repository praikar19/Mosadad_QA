package com.mosadad.testing.listeners;

import com.mosadad.testing.browser.PlaywrightManager;
import com.mosadad.testing.utils.ScreenshotUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * TestNG listener registered globally in each testng*.xml suite.
 *
 * Responsibilities:
 *  - Log test start / pass / fail / skip to console and log file
 *    (target/logs/mosadad-testing.log — everything; target/logs/errors.log
 *    — failures only, full stack trace).
 *  - Capture a screenshot on failure and attach it to the Allure report.
 *    This happens here (not in @AfterMethod) so it runs even when the
 *    @AfterMethod itself throws an exception.
 *
 * Note: the Allure HTML report (see `mvn allure:report` / `allure:serve`)
 * already shows the full stack trace per failed test on its own — this
 * listener's job is to make sure the SAME detail is also available as
 * plain text in target/logs/errors.log for anyone not looking at Allure
 * (grep-able in CI logs, etc.).
 */
public class TestListener implements ITestListener {

    private static final Logger log = LogManager.getLogger(TestListener.class);

    @Override
    public void onTestStart(ITestResult result) {
        log.info("START  [{}] {}", result.getTestClass().getRealClass().getSimpleName(),
                result.getMethod().getMethodName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        log.info("PASS   [{}] {} ({}ms)",
                result.getTestClass().getRealClass().getSimpleName(),
                result.getMethod().getMethodName(),
                result.getEndMillis() - result.getStartMillis());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String className = result.getTestClass().getRealClass().getSimpleName();
        String methodName = result.getMethod().getMethodName();
        long durationMs = result.getEndMillis() - result.getStartMillis();

        // Full stack trace (not just getMessage()) goes to target/logs/errors.log via
        // the ErrorAppender — this is what "where do I see what actually broke" points to.
        log.error("FAIL   [{}] {} ({}ms)", className, methodName, durationMs, result.getThrowable());

        if (PlaywrightManager.hasPage()) {
            ScreenshotUtils.captureAndAttach(
                    PlaywrightManager.getPage(),
                    result.getMethod().getMethodName()
            );
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        log.warn("SKIP   [{}] {}",
                result.getTestClass().getRealClass().getSimpleName(),
                result.getMethod().getMethodName());
    }
}
