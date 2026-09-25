package com.mosadad.testing.listeners;

import com.mosadad.testing.browser.PlaywrightManager;
import com.mosadad.testing.browser.TwoActorPlaywrightManager;
import com.mosadad.testing.utils.ScreenshotUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Logs test results and attaches failure screenshots to Allure. Registered
 * only via @Listeners on BaseTest — also declaring it in a suite XML
 * registers it twice and breaks screenshot attachment.
 */
public class TestListener implements ITestListener, IInvokedMethodListener {

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

        log.error("FAIL   [{}] {} ({}ms)", className, methodName, durationMs, result.getThrowable());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        log.warn("SKIP   [{}] {}",
                result.getTestClass().getRealClass().getSimpleName(),
                result.getMethod().getMethodName());
    }

    /**
     * Screenshots are taken here rather than in onTestFailure(): AllureTestNg
     * closes the current test in its own onTestFailure(), which runs first, so
     * attachments added later are orphaned. Two-actor tests (BaseTwoActorUiTest)
     * don't use PlaywrightManager, so each actor's page is captured separately.
     */
    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        if (!method.isTestMethod() || testResult.isSuccess()) {
            return;
        }
        String methodName = testResult.getMethod().getMethodName();
        if (PlaywrightManager.hasPage()) {
            ScreenshotUtils.captureAndAttach(PlaywrightManager.getPage(), methodName);
        }
        TwoActorPlaywrightManager.getOpenPages().forEach((actor, page) ->
                ScreenshotUtils.captureAndAttach(page, methodName + "_" + actor));
    }
}
