# Mosadad Testing Framework

## Why this framework exists

A combined UI (Playwright) + API (RestAssured) test automation framework for
the Mosadad Recovery Claim insurance platform, built to the same standard as
the sibling frameworks in this workspace (`ShopTestApp-Java-Mobile-Testing`,
`ShopTestApp-Playwright`) — the pattern used at large-scale Java QA shops
(Uber, Microsoft, Swiggy, Zomato, OLA, Flipkart).

See `MOSADAD_DOMAIN.md` for what the app under test actually does.

---

## Technology Stack

| Layer | Choice | Version |
|---|---|---|
| Language | Java | 17 (LTS) |
| Build | Maven | 3.x |
| Test Runner | TestNG | 7.10.2 |
| UI Automation | Playwright (Java) | 1.60.0 |
| API Testing | RestAssured | 5.5.0 |
| Reporting | Allure | 2.29.0 |
| JSON | Jackson | 2.18.2 |
| Assertions | AssertJ | 3.26.3 |
| Boilerplate | Lombok | 1.18.36 |
| Logging | Log4j2 | 2.24.3 |

These are the exact same choices and versions as
`ShopTestApp-Java-Mobile-Testing`, swapping Appium (mobile) for Playwright
(web) — see that project's `FRAMEWORK.md` for the full reasoning behind
TestNG-over-JUnit5, Maven-over-Gradle, RestAssured, Allure, AssertJ, and
Log4j2. Only the UI-automation and locator-strategy decisions differ enough
to repeat here.

---

## Decision — Playwright over Selenium

The user asked for "a Selenium framework having both UI (Playwright) and API
testing" — read as: build to Selenium-framework standards (POM, TestNG,
Maven, Page-Object base classes, ThreadLocal session management) but use
Playwright as the actual browser-automation engine, which is what the
sibling `ShopTestApp-Playwright` project already establishes as this
workspace's convention for web (as opposed to mobile/Appium) UI testing.

Playwright over raw Selenium WebDriver:

- **Auto-waiting is built in.** Every `Locator` action (click, fill, etc.)
  waits for the element to be actionable before acting — the exact problem
  Selenium needs explicit `WebDriverWait` boilerplate for.
- **Locators are lazy by construction.** `page.locator(selector)` doesn't
  resolve anything until an action is called on it, so there's no
  Page-Factory staleness problem to design around (the sibling Appium
  framework had to explicitly avoid `@FindBy` for this reason — Playwright
  gets it for free).
- **Built-in tracing** (`context.tracing()`) captures a full timeline —
  screenshots, DOM snapshots, and source links — for every test, which is
  invaluable when a QA/UAT environment's real DOM structure isn't fully
  mapped yet (see "What's stubbed" below).
- Same reasoning as `ShopTestApp-Playwright`'s existing choice, just
  restructured into the cleaner `com.mosadad.testing` package convention
  established in `ShopTestApp-Java-Mobile-Testing`.

---

## Decision — ThreadLocal PlaywrightManager

```java
private static final ThreadLocal<Page> PAGE = new ThreadLocal<>();
```

Same reasoning as the sibling framework's `DriverManager` for Appium:
parallel TestNG execution (`parallel="tests"`/`"methods"`) needs one
independent Playwright session per thread, or one test's navigation leaks
into another's assertions. `PlaywrightManager` holds `Playwright`,
`Browser`, `BrowserContext`, and `Page` all as separate `ThreadLocal`s and
tears all four down together in `close()`.

---

## Decision — Page Objects without Page Factory, using plain selector strings

Locators are stored as `private static final String` constants (CSS/text
selectors), resolved via `page.locator(selector)` inside `BasePage` helper
methods (`fill`, `click`, `getText`, `isVisible`, `waitVisible`). No
`@FindBy`-equivalent annotation layer — Playwright's `Locator` already does
lazy, auto-waited resolution, so a Page-Factory-style abstraction would add
nothing over calling `page.locator()` directly.

---

## Design Pattern — Two-Layer Base Class Hierarchy

```
BaseTest                  (apiClient)
  └── BaseUiTest           (extends BaseTest + Playwright session lifecycle)
        ├── LoginUiTest                    — REAL, verified
        ├── RecoveryClaimsNavigationTest   — REAL, verified
        ├── ClaimRegistrationTest          — stub (enabled=false)
        ├── QuotationTest                  — stub
        ├── InvoiceTest                    — stub
        ├── SettlementTest                 — stub
        └── DisputeTest                    — stub

  └── BaseApiTest           (extends BaseTest — API only, no browser)
        ├── AuthApiTest                    — REAL, verified (tenant/User/Login)
        ├── tests/api/tenant/*Test         — REAL, verified (10 classes)
        ├── tests/api/inthub/*Test         — REAL, verified (5 classes)
        ├── tests/api/quotation/*Test      — REAL, verified (3 classes)
        ├── tests/api/settlement/*Test     — REAL, verified (4 classes)
        ├── tests/api/invoice/*Test        — REAL, verified (4 classes)
        └── tests/api/claims/*Test         — REAL, verified (20 classes)
```

API-only tests run without a browser at all (`mvn test -P api`), same role
as `LoginTest` in the sibling mobile framework — the fast, no-browser PR
gate. **This is no longer a stub.** 403 test methods across the 6 backend
microservices, 330 of them real, live-verified calls against the QA
environment (the rest are disabled stubs for mutations that would write
real data — see "What Is Verified vs. Stubbed" below). Auth is a real,
reverse-engineered XOR+SHA-256 login (see `EncryptionUtil` javadoc) against
the actual backend, not a browser-driven session capture.

---

## What Is Verified vs. Stubbed (be honest with yourself before trusting a test)

**Verified live** against the QA environment on 2026-08-30, logged in as
the Claimant Insurer role (`Dubaiqa@gmail.com`):

- `LoginPage` — `#email`, `#password`, `button[type=submit]` ("Sign In"),
  `input[name=remember]`, "Forgot Password?" link. Real Angular form ids,
  not generated/obfuscated.
- `DashboardPage` — lands at `/entity-landing/dashboard`; sidebar
  (Home / Recovery Claims / Company Details), top bar
  (`.search-input`, `.bell-button`, `.profile-trigger`), "My Wallets"
  button, "In-Process Claims" / "Total Recoverable Value" / "Total Payable
  Value" cards.
- `RecoveryClaimsHubPage` — `/entity-landing/recovery-claims`, three module
  cards with real `a.nav-link` hrefs (see `Routes.java`).

**Not yet explored — TODO placeholders in the code**, marked with `TODO`
comments in every affected class:

- The actual "Create Claim" flow (Manual Entry / Police Data Entry / Fast
  Track) — `pages/claims/ClaimRegistrationPage.java`.
- Quotation, Total Loss, Salvage screens — `pages/claims/QuotationPage.java`.
- Invoice, Settlement, Dispute, Wallet-detail screens.

Do not assume a `TODO`-marked UI selector works. Verify against the real
app before enabling its test.

### API layer — verified live 2026-09-17 (all 6 backend microservices)

The real backend base URL, auth flow, and all 359 documented endpoints
(across claims/inthub/invoice/quotation/settlement/tenant) were captured
and confirmed live against the QA gateway — see `ApiClient`, `AuthApiTest`,
and `EncryptionUtil`'s javadocs for the full technical detail. In summary:

- **Base URL**: `https://recovery-api-management-qa.azure-api.net/api/<service>`
  — a shared Azure API Management gateway, NOT the Swagger UI hosts
  (`74.162.138.166`), which are direct-to-origin and used only for spec
  discovery.
- **Auth**: `POST /tenant/User/Login` with XOR(SHA-256(staticKey))-obfuscated
  email/password (reverse-engineered from the live Angular bundle, see
  `EncryptionUtil`) returns a real JWT. No API-gateway subscription key is
  required — only the bearer token.
- **Response envelope**: most endpoints wrap real data in
  `{statusCode, message, response, isSuccess, errors}` at **transport HTTP
  200**, even for business failures (wrong password, "not found", etc.).
  ASP.NET Core model-validation failures instead return a real HTTP 400
  with the RFC-9110 ProblemDetails shape. A missing/invalid bearer token
  returns a real HTTP 401. See `ApiAssertions`' javadoc for all three
  shapes and when each applies.
- **330 of 403 API test methods are real, live-verified calls**; the
  other 73 are disabled (`enabled=false`) stubs for endpoints that would
  write real, hard-to-revert data into the shared QA environment (creating
  entities/users/roles/claims, deleting records, financial checkout, or
  known **unscoped bulk-action endpoints** — see the next section).
  Enabling any stub requires explicit sign-off and, in several cases, a
  disposable fixture created first.

### ⚠️ Known unscoped bulk-action endpoints — do not call ad hoc

Several endpoints take **no id at all** and act on every pending item for
the caller/tenant, confirmed live to actually mutate real data even with an
empty request body (no validation gate). None of these are called by any
enabled test in this suite:

| Endpoint | Confirmed effect |
|---|---|
| `PUT quotation/Quotation/AcceptAll` | Accepted a real quotation with an empty body |
| `PUT invoice/Invoice/Accept` (no id) | Same — "Accepted Successfully" |
| `PUT invoice/ExtraExpenses/HandleStatus` (no id) | Same |
| `PUT invoice/ExtraExpenses/AcceptNegotiation` (no id) | Same |
| `POST claims/Config/Update` (no id) | "Config updated successfully" with `{}` |
| `POST claims/ClaimantNotification/TriggerAll` | Triggered every pending notification job platform-wide |
| `GET settlement/CreditNote/Notify/{a}/{b}` and `Settlement/Notify/{a}/{b}` | Reports success even for two fake ids — never validates either exists; used with fake ids only, in the `notifies` test group |

Each has an `enabled=false` stub in its test class documenting the finding;
see each class's javadoc for the id-scoped safe sibling where one exists.
**These were each executed exactly once, unintentionally, while mapping
the live API on 2026-09-17** (see the session's summary to the user for
what that means for QA data).

### Confirmed backend bugs found while building this suite

- `claims/Intimation/HandleInvoiceIntimationLetter` and
  `HandleLouIntimationLetter` throw an unhandled `NullReferenceException`
  (raw HTTP 500 with a full .NET stack trace, including internal server
  IPs — Developer Exception Page is enabled on this QA deployment) for a
  non-existent claim, instead of the graceful envelope failure their two
  sibling endpoints return. See `IntimationApiTest`.
- `claims/Dashboard/GetRegulatorDashboard`, `Admin/TasksOverdueReport`, and
  `TasksOverdueReport` (plus their `/Export` variants) return an envelope
  failure ("Error something went wrong" / a BsonType deserialization error)
  for the current QA login rather than real data. See `DashboardApiTest`.
- `claims/Dashboard/Regulator/PotentialClaims/Export` throws a raw Mongo
  aggregation error ("the limit must be positive"); so does
  `settlement/CreditNote/Bulk/History/{transactionId}` without a page
  filter — both look like a missing default page size.
- `claims/Dashboard/ClaimAgeingAnalysis` requires a `payable` flag but
  reports the misleading message "Claim Type is required" when it's
  missing — no such field exists on the request schema.
- `claims/Dashboard/ActionRerquiredCombind/Export` — the real, live path is
  misspelled ("Rerquired"/"Combind"); `ActionRequiredCombined/Export` is
  the correctly-spelled sibling and both work.

---

## Project Structure

```
src/
├── main/java/com/mosadad/testing/
│   ├── api/
│   │   ├── ApiClient.java          RestAssured wrapper — REAL, all 6 services, verified live
│   │   ├── ApiAssertions.java      The 3 real response shapes (envelope/ProblemDetails/401) + helpers
│   │   ├── EncryptionUtil.java     Reverse-engineered XOR+SHA-256 login field obfuscation
│   │   └── WalletApiClient.java    Third-party ATB Pay wallet portal client (separate system)
│   ├── browser/
│   │   └── PlaywrightManager.java  ThreadLocal<Playwright/Browser/Context/Page>
│   ├── config/
│   │   └── ConfigManager.java      Reads config.properties + credentials.properties; -D overrides
│   ├── constants/
│   │   ├── Routes.java             Verified front-end route paths
│   │   └── TestDataConstants.java  Roles, SLA hours, claim methods, etc.
│   ├── listeners/
│   │   └── TestListener.java       TestNG ITestListener — logs + screenshot on fail
│   ├── pages/
│   │   ├── BasePage.java           fill, click, getText, isVisible, waitVisible
│   │   ├── LoginPage.java          REAL — verified
│   │   ├── DashboardPage.java      REAL — verified
│   │   ├── RecoveryClaimsHubPage.java  REAL — verified
│   │   └── claims/                 STUB page objects — Stage 1-4, Dispute, Wallet
│   └── utils/
│       └── ScreenshotUtils.java    Captures + attaches to Allure on failure
│
└── test/
    ├── java/com/mosadad/testing/
    │   ├── base/
    │   │   ├── BaseTest.java       Initialises apiClient
    │   │   ├── BaseUiTest.java     Playwright session per test method
    │   │   └── BaseApiTest.java    Per-service request-spec shortcuts (claims()/tenant()/... , noAuth(), entityId())
    │   └── tests/
    │       ├── auth/LoginUiTest                     REAL — 2 tests
    │       ├── navigation/RecoveryClaimsNavigationTest  REAL — 8 tests
    │       ├── claims/*Test                          STUB — UI checklist, enabled=false
    │       └── api/
    │           ├── AuthApiTest                       REAL — 6 tests
    │           ├── tenant/    (10 classes, 66 tests)  REAL
    │           ├── inthub/    (5 classes, 25 tests)   REAL
    │           ├── quotation/ (3 classes, 22 tests)   REAL
    │           ├── settlement/(4 classes, 20 tests)   REAL
    │           ├── invoice/   (4 classes, 29 tests)   REAL
    │           └── claims/    (20 classes, 235 tests) REAL — Claim/FastTrack/Dashboard/PotentialClaim/etc.
    └── resources/
        ├── config.properties               Base URL/gateway URL, browser, SLA constants
        ├── credentials.properties          Gitignored — real QA login
        ├── credentials.properties.example  Template (committed)
        ├── testng.xml                      Full regression (default) — API suite now wired in
        ├── testng-ui.xml                   UI only, parallel
        ├── testng-api.xml                  API only — 403 tests, 330 live-verified
        └── testng-smoke.xml                Fastest gate — login only
```

---

## Running Tests

```bash
# Full regression (default) — real tests run, stub tests show as skipped
mvn test

# UI only, real + verified
mvn test -P ui

# Smoke — single login test, fastest gate
mvn test -P smoke

# API — 403 tests across all 6 backend microservices, 330 live-verified
mvn test -P api

# Override any config value at runtime (e.g. different browser, different QA login)
mvn test -P smoke -Dbrowser=firefox -Dqa.claimant.email=... -Dqa.claimant.password=...
```

See **Reports & Logs** below for how to turn a run into an HTML dashboard.

First run needs Playwright's browser binaries installed once:

```bash
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install --with-deps chromium"
```

---

## Reports & Logs

Every `mvn test` run produces two independent things: an **Allure HTML
dashboard** (pass/fail/skip counts, drill into any test, full error detail)
and **plain-text log files** (grep-able, no tooling required). Both come
from the same run — you don't choose one or the other.

### The Allure HTML dashboard

```
1. Run any test profile — this populates target/allure-results/ (raw JSON, not human-readable):
   mvn test -P api            # or -P ui / -P smoke / plain `mvn test`

2. Turn that into the HTML report:
   mvn io.qameta.allure:allure-maven:2.15.1:serve
```

That one command generates the report **and** opens it in your default
browser via a small local server, on a random free port. Leave the
terminal running — closing it (Ctrl+C) stops the server.

**Important — two things that look like they should work, don't:**

- **`mvn allure:report` / `mvn allure:serve` (the short form)** — Maven
  can't resolve the `allure` plugin prefix in this project, so these fail
  with `No plugin found for prefix 'allure'`. Always use the full
  coordinates shown above (`io.qameta.allure:allure-maven:2.15.1:...`).
- **Double-clicking `index.html` inside a generated report folder** — the
  report loads its data via JavaScript `fetch()` calls, which every
  browser blocks under `file://` (CORS). It **must** be served over HTTP.
  If you want a report you can reopen later without re-running Maven
  (e.g. after a CI run), generate it once and open it with the Allure CLI
  itself, which serves it for you:

  ```bash
  # Already downloaded locally by the allure-maven plugin the first time
  # any of its goals ran — reuse it directly, no reinstall needed:
  .allure/allure-2.29.0/bin/allure generate target/allure-results --clean -o target/allure-report
  .allure/allure-2.29.0/bin/allure open target/allure-report

  # Equivalent, if you prefer a standalone install (one-time):
  #   brew install allure
  #   allure generate target/allure-results --clean -o target/allure-report
  #   allure open target/allure-report
  ```

### What the dashboard shows

The **Overview** tab is the summary you're after:

| Widget | Meaning |
|---|---|
| **Total** | Every `@Test` method TestNG attempted or registered |
| **Passed** | Assertions held, no exception |
| **Failed** | An assertion failed |
| **Broken** | Something other than an assertion threw (setup error, network timeout, NPE) |
| **Skipped** | A `@BeforeMethod`/dependency failure caused TestNG to skip it |
| **Unknown** | TestNG never ran it at all — this is where this suite's `enabled=false` stub tests land (73 of them; see FRAMEWORK.md's "What Is Verified vs. Stubbed") |

Click through **Suites** (mirrors the `testng-*.xml` `<test>` blocks) or
**Behaviors** (grouped by `@Epic`/`@Feature`, e.g. "Claims API — Claim") to
drill into any individual test.

### Where to find a specific test's error detail

**Inside the Allure report** — click the failed test, then its
**Overview** tab: TestNG/Allure captures the full exception (type,
message, and complete stack trace) automatically for every failure, no
extra setup needed. If it's a UI test, `TestListener` also attaches a
screenshot at the moment of failure (see the **Attachments** section on
that same page).

**Without opening Allure at all** — every run also writes plain-text logs
to `target/logs/`:

| File | Contents |
|---|---|
| `target/logs/mosadad-testing.log` | Everything — every `START`/`PASS`/`FAIL`/`SKIP` line, request/response logging, DEBUG detail from `com.mosadad.*` |
| `target/logs/errors.log` | **Only** failures, with the full stack trace (not just the message) — the fastest way to `grep` for what broke without touching a browser |

```bash
# Fastest way to see what failed, no Allure needed:
cat target/logs/errors.log

# Or tail it live while a suite is running:
tail -f target/logs/mosadad-testing.log
```

Both files roll over automatically (10MB / daily for the main log, 5MB for
errors.log, keeping the last 5) — see `src/main/resources/log4j2.xml` if
you need to change retention or add a new logger.

---

## Next Steps (in priority order)

1. Walk the real "Create Claim" screen as the Claimant Insurer and fill in
   `ClaimRegistrationPage`'s TODO selectors; enable
   `claimantCanRegisterClaimViaManualEntry` first.
2. Get At-Fault Insurer test credentials so cross-role flows (claim
   submitted → at-fault notified → at-fault approves) can be tested
   end-to-end instead of stubbed.
3. Get sign-off to enable the 73 disabled API mutation stubs against
   disposable QA fixtures (a throwaway entity/claim/role per write-heavy
   test class) — see each `*ApiTest` class's `enabled=false` methods and
   "Known unscoped bulk-action endpoints" above before enabling anything
   that isn't id-scoped to a disposable fixture.
4. Get Regulator and Mosadad Admin test credentials for SLA-violation and
   dispute-history visibility tests, and governance/access-management tests.
5. Report the confirmed backend bugs (see "Confirmed backend bugs found
   while building this suite" above) to the Mosadad backend team.
