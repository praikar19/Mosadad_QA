# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this project is

This is a Java/TestNG automation framework for the Mosadad Recovery Claim platform, combining:

- UI testing with Playwright
- API testing with RestAssured
- shared base classes and utilities for a consistent project structure

QA app URL:
`https://recoveryclaim-entity-qa.azurewebsites.net/auth/login`

## Start here

Use the simplest path first:

```bash
mvn test -P smoke
```

This is the quick validation gate for setup, auth, and basic app health.

## Commands

```bash
# Full regression (real tests run; stub tests show as skipped)
mvn test

# UI tests only
mvn test -P ui

# Fastest gate — single login smoke test
mvn test -P smoke

# API tests — live-verified backend checks across 6 services
mvn test -P api

# Open Allure report
mvn io.qameta.allure:allure-maven:2.15.1:serve

# One-time Playwright browser install
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install --with-deps chromium"
```

## Credentials

`src/test/resources/credentials.properties` holds the real QA login.

If it is missing, create it or pass credentials directly:

```bash
mvn test -Dqa.claimant.email="your-email" -Dqa.claimant.password="your-password"
```

## Use this project safely

This repo tests a shared QA environment. Not every endpoint or flow is safe to run.

Follow this order when working locally:

1. smoke
2. targeted API or UI verification
3. regression only when necessary

Never assume a mutation endpoint is safe just because it exists. Check the risk notes in `FRAMEWORK.md` before enabling a new flow.

## Architecture

The test suite follows a layered structure similar to the sibling Java framework pattern:

- config
- api
- pages
- tests
- listeners
- constants
- utils
- shared base classes

The key pattern is a two-layer base hierarchy:

- `BaseTest` for common test setup
- `BaseUiTest` for browser lifecycle and Playwright session management

## Important reading order

Read these in order:

1. `FRAMEWORK.md` — combined reference: what the app actually does (Part 1),
   how the suite is built (Part 2), and what is verified vs. what is still a
   TODO or stub (Part 3)
2. `README.md` — the quick start and execution flow

This avoids trusting selectors or API endpoints that have not been live-verified.

## Risk-level guidance

- Safe: read-only checks, smoke validation, most API reads
- Needs review: UI flows touching real data or external state
- Unsafe by default: endpoints that mutate, create, delete, bulk-accept, or trigger notifications across shared QA data

Only enable a risky flow after confirming the impact and the intended fixture.

## Final rule

Before adding or enabling a test, confirm:

- it is mapped to the correct service
- it is safe for the shared QA environment
- it is not a destructive or broad bulk-action endpoint
- the selector or endpoint is verified in `FRAMEWORK.md`

This is the fastest way to keep the suite trustworthy and easy to maintain.
