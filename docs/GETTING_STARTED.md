# Getting Started

This guide explains the simplest path for running this repository without overload.

## 1. Verify your environment

Run these commands first:

```bash
java -version
mvn -version
```

Expected result:

- Java 17 installed
- Maven 3.x available

## 2. Install Playwright browser dependencies

```bash
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install --with-deps chromium"
```

This is required before UI tests can run reliably.

## 3. Add test credentials

Create or update the credentials file:

```text
src/test/resources/credentials.properties
```

If the file is missing, copy the example file and fill in your real QA values.

You can also provide values directly in the command line:

```bash
mvn test -Dqa.claimant.email="your-email" -Dqa.claimant.password="your-password"
```

## 4. Start with the smoke suite

This is the safest and simplest onboarding suite:

```bash
mvn test -P smoke
```

This helps verify:

- project setup works
- credentials are valid
- environment is reachable
- core flow is still working

## 5. Move to targeted validation

### UI only

```bash
mvn test -P ui
```

### API only

```bash
mvn test -P api
```

### Full regression

```bash
mvn test
```

Use the broad suite only when you are ready for the full QA validation flow.

## 6. Review the results

The project uses Allure reports.

Quick command:

```bash
mvn io.qameta.allure:allure-maven:2.15.1:serve
```

This opens a test report dashboard for pass/fail details and logs.

## 7. Common issues and fixes

### Java version mismatch

Check your JDK version:

```bash
java -version
```

Install Java 17 if needed.

### Playwright browser not found

Re-run the browser install command:

```bash
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install --with-deps chromium"
```

### Credentials missing

Validate the credentials file exists and contains valid values.

### Test environment not accessible

Check that the QA login URL is reachable and the environment is healthy before assuming test failures are code issues.

## 8. Safe usage guide

This repo contains live QA testing.

Use this order:

1. smoke
2. api or ui based on target
3. regression only when needed

Avoid running destructive or mutation-style tests unless you explicitly know the purpose and the data impact.

## 9. Recommended workflow

Use this simple cycle:

- Setup
- Smoke test
- Targeted validation
- Reporting
- Fix or investigate failures

This keeps the project approachable and helps prevent confusion from full-suite execution too early.

## 10. Documentation to read next

- [FRAMEWORK.md](../FRAMEWORK.md) — technical architecture and reasoning
- [MOSADAD_DOMAIN.md](../MOSADAD_DOMAIN.md) — business domain and claim flows
- [CLAUDE.md](../CLAUDE.md) — workflow guidance for contributors

This is the easiest way to get productive without diving into the full migration-level detail first.
