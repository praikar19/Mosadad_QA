package com.mosadad.testing.listeners;

import com.mosadad.testing.browser.PlaywrightManager;
import com.mosadad.testing.utils.ScreenshotUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * TestNG listener — registered exactly once, via @Listeners on BaseTest
 * (see its Javadoc for why this must NOT also be declared in a suite XML's
 * &lt;listeners&gt; block: that double-registration previously made failure
 * screenshots go missing from the Allure report).
 *
 * Responsibilities:
 *  - Log test start / pass / fail / skip to console and log file
 *    (target/logs/mosadad-testing.log — everything; target/logs/errors.log
 *    — failures only, full stack trace). Done in the ITestListener methods
 *    below; ordering here doesn't matter, so onTestFailure() is fine for it.
 *  - Capture a screenshot on failure and attach it to the Allure report.
 *    Done in afterInvocation() instead — see its Javadoc for why that, and
 *    not onTestFailure(), is the method that has to do this.
 *
 * Note: the Allure HTML report (see `mvn allure:report` / `allure:serve`)
 * already shows the full stack trace per failed test on its own — this
 * listener's job is to make sure the SAME detail is also available as
 * plain text in target/logs/errors.log for anyone not looking at Allure
 * (grep-able in CI logs, etc.).
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

        // Full stack trace (not just getMessage()) goes to target/logs/errors.log via
        // the ErrorAppender — this is what "where do I see what actually broke" points to.
        log.error("FAIL   [{}] {} ({}ms)", className, methodName, durationMs, result.getThrowable());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        log.warn("SKIP   [{}] {}",
                result.getTestClass().getRealClass().getSimpleName(),
                result.getMethod().getMethodName());
    }

    /**
     * Captures the failure screenshot here, not in onTestFailure(), because
     * AllureTestNg is ALSO an ITestListener and — being registered via
     * TestNG's ServiceLoader SPI (allure-testng ships its own
     * META-INF/services/org.testng.ITestNGListener) rather than through
     * @Listeners — TestNG dispatches it to onTestFailure() before our own
     * (confirmed live 2026-09-21: even after fixing our own duplicate
     * AllureTestNg registrations, a failure screenshot still landed as an
     * orphaned, unlinked Allure attachment). AllureTestNg's onTestFailure()
     * is what stops+writes the Allure "current test", so by the time our
     * onTestFailure() ran and called Allure.addAttachment(), there was no
     * live test case left to attach to.
     *
     * IInvokedMethodListener.afterInvocation() fires strictly before ANY
     * ITestListener.onTestXxx callback — it wraps the raw method invocation
     * itself, one layer inside TestNG's result-listener dispatch (see
     * TestNG's TestInvoker: afterInvocation() runs in a finally block right
     * after the method returns/throws, before runTestListeners() computes
     * and broadcasts the ITestResult to onTestSuccess/onTestFailure). The
     * Allure test case is still open at this point — confirmed live this
     * fixes it. ITestResult's status/throwable are already set by the time
     * afterInvocation() runs, so testResult.isSuccess() is reliable here.
     */
    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        if (!method.isTestMethod() || testResult.isSuccess()) {
            return;
        }
        if (PlaywrightManager.hasPage()) {
            ScreenshotUtils.captureAndAttach(
                    PlaywrightManager.getPage(),
                    testResult.getMethod().getMethodName()
            );
        }
    }
}
