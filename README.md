# Mosadad Testing Framework

A Java + TestNG test automation framework for the Mosadad Recovery Claim platform, covering both UI and API validation.

## Start here

If you are new to this project, use this order:

1. Verify Java and Maven are installed
2. Install Playwright browser binaries
3. Add QA credentials
4. Run the smoke suite
5. Run API or UI tests as needed
6. Review the Allure report

## Quick commands

```bash
# Smoke test: fastest validation gate
mvn test -P smoke

# UI only
mvn test -P ui

# API only
mvn test -P api

# Full regression
mvn test
```

## Prerequisites

- Java 17
- Maven 3.x
- Playwright browser dependencies installed
- Valid QA credentials for the environment

## One-time setup

### 1. Check Java and Maven

```bash
java -version
mvn -version
```

### 2. Install Playwright browser binaries

```bash
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install --with-deps chromium"
```

### 3. Add credentials

Place your real QA credentials in:

- src/test/resources/credentials.properties

If the file is missing, use the example or pass values directly:

```bash
mvn test -Dqa.claimant.email="your-email" -Dqa.claimant.password="your-password"
```

## What to run and when

### Safe starting point

```bash
mvn test -P smoke
```

Use this to verify setup, authentication, and basic app health before running broader suites.

### UI validation

```bash
mvn test -P ui
```

Use this for browser-based tests and end-to-end validation.

### API validation

```bash
mvn test -P api
```

Use this for the backend contract and real service checks.

### Full suite

```bash
mvn test
```

Use this only when you want the complete regression pass.

## Important safety note

This repo validates a live QA environment with shared data.

Some tests are intentionally disabled because they would mutate or affect real records. Do not assume every endpoint is safe to run.

Use this rule:

- Safe read-only checks: enabled
- Destructive or mutating actions: disabled or explicitly documented
- New tests involving real data: verify impact before enabling

## Reporting

This project uses Allure.

To open the report locally:

```bash
mvn io.qameta.allure:allure-maven:2.15.1:serve
```

Or generate a static report:

```bash
.allure/allure-2.29.0/bin/allure generate target/allure-results --clean -o target/allure-report
```

## Recommended workflow

For most contributors, this is the cleanest path:

1. Setup Java and Maven
2. Install Playwright browsers
3. Add QA credentials
4. Run smoke tests
5. Run a targeted API/UI suite
6. Review Allure report
7. Only run regression when needed

## Documentation map

- [CLAUDE.md](CLAUDE.md) — repo guidance and contributor workflow
- [FRAMEWORK.md](FRAMEWORK.md) — combined reference: app domain (claim lifecycle, actors, SLAs), design/architecture (package map, base-class hierarchy, request flow), why each technology/pattern was chosen, and the full verified-vs-stubbed ledger

## Project layout

```text
src/
  main/
    java/
  test/
    java/
    resources/
```

The project is organized around:

- config
- API clients
- page objects
- test classes
- utilities
- listeners
- shared base classes

## Troubleshooting

If setup fails, check the following first:

- Java version is 17
- Maven is installed correctly
- Playwright browser binaries are present
- credentials.properties contains valid values
- QA environment is reachable

---

This repo is best used in a simple, low-risk flow: verify setup → run smoke → run the needed suite → review results.
