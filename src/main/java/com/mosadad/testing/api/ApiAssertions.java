package com.mosadad.testing.api;

import io.restassured.response.Response;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Shared assertions for the {@code ApiBaseResponse} envelope every
 * Mosadad backend controller wraps its JSON in:
 * {@code {statusCode, message, response, isSuccess, errors}}
 * (see e.g. {@code LoginResponseDtoApiBaseResponse} in the tenant Swagger
 * spec — the same shape repeats across all 6 services).
 *
 * <p><b>Important, confirmed live 2026-09-17:</b> the transport-level HTTP
 * status and the envelope's inner {@code statusCode} are NOT the same
 * thing and diverge in exactly one direction: business-logic outcomes for
 * an otherwise-valid, authenticated request (bad login credentials,
 * validation failures, "not found" business entities) come back as
 * <i>HTTP 200</i> with the real outcome inside the JSON body — e.g.
 * {@code POST /tenant/User/Login} with wrong credentials returns HTTP 200,
 * body {@code {"statusCode":400,"message":"Wrong Username or
 * Password.","isSuccess":false}}. Gateway/middleware-level failures
 * (missing/invalid bearer token, unroutable path) DO return real HTTP
 * status codes (confirmed: no token → real HTTP 401). Always assert the
 * layer you actually mean to test.
 */
public final class ApiAssertions {

    private ApiAssertions() {}

    private static String jsonSafeString(Response res, String path, String defaultValue) {
        String body = res.asString();
        if (body == null || body.isBlank()) {
            return defaultValue;
        }
        try {
            Object value = res.jsonPath().get(path);
            return value == null ? defaultValue : String.valueOf(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static String responseSummary(Response res) {
        return "HTTP=" + res.statusCode() + "; isSuccess=" + jsonSafeString(res, "isSuccess", "n/a") + "; body=" + res.asString();
    }

    /** Transport 200 AND envelope isSuccess:true — the common "happy path" shape. */
    public static void assertStatusCode(Response res, int expectedStatus, String description) {
        assertThat(res.statusCode())
            .as("Expected %s to be %s but actual was %s. %s", description, expectedStatus, res.statusCode(), responseSummary(res))
            .isEqualTo(expectedStatus);
    }

    public static void assertEnvelopeSuccess(Response res) {
        assertThat(res.statusCode())
            .as("Expected HTTP status 200 but actual was %s. %s", res.statusCode(), responseSummary(res))
            .isEqualTo(200);
        assertThat(res.jsonPath().getBoolean("isSuccess"))
            .as("Expected envelope isSuccess=true but actual was %s. %s", res.jsonPath().getBoolean("isSuccess"), responseSummary(res))
            .isTrue();
    }

    /** Transport 200 but envelope isSuccess:false with a specific inner business statusCode (e.g. 400, 404). */
    public static void assertEnvelopeFailure(Response res, int expectedInnerStatusCode) {
        assertThat(res.statusCode())
            .as("Expected HTTP status 200 but actual was %s. Envelope failures are still transport 200. %s", res.statusCode(), responseSummary(res))
            .isEqualTo(200);
        assertThat(res.jsonPath().getBoolean("isSuccess"))
            .as("Expected envelope isSuccess=false but actual was %s. %s", res.jsonPath().getBoolean("isSuccess"), responseSummary(res))
            .isFalse();
        assertThat(res.jsonPath().getInt("statusCode"))
            .as("Expected envelope statusCode=%s but actual was %s. %s", expectedInnerStatusCode, res.jsonPath().getInt("statusCode"), responseSummary(res))
            .isEqualTo(expectedInnerStatusCode);
    }

    /** Real HTTP 401 at the gateway/middleware level — used for "no/invalid bearer token" tests. */
    public static void assertHttpUnauthorized(Response res) {
        assertThat(res.statusCode())
            .as("Expected HTTP 401 unauthorized but actual was %s. %s", res.statusCode(), responseSummary(res))
            .isEqualTo(401);
    }

    /**
     * A THIRD response shape, confirmed live 2026-09-17: when a request body
     * fails ASP.NET Core's own [ApiController] model-binding validation
     * (missing a [Required] field) before the controller action ever runs,
     * the API returns a real HTTP 400 with an RFC-9110 ProblemDetails body
     * — {@code {errors:{Field:[...]}, type, title, status, traceId}} — NOT
     * the {@code ApiBaseResponse} envelope. This is what a deliberately
     * empty/incomplete body on a "Create" endpoint returns, which makes it
     * a safe, live, no-side-effects way to exercise those endpoints without
     * actually writing data — used throughout this suite as the live
     * counterpart to the disabled "real create" stub test.
     */
    public static void assertValidationProblem(Response res, String... expectedRequiredFields) {
        assertThat(res.statusCode())
            .as("Expected HTTP 400 validation problem but actual was %s. %s", res.statusCode(), responseSummary(res))
            .isEqualTo(400);
        assertThat(res.jsonPath().getString("title")).as("Expected validation title to be present. %s", responseSummary(res)).isNotBlank();
        for (String field : expectedRequiredFields) {
            assertThat(res.jsonPath().getMap("errors"))
                .as("Expected validation errors to contain required field '%s'. Actual errors map: %s. Response body: %s", field, res.jsonPath().getMap("errors"), res.asString())
                .containsKey(field);
        }
    }

    public static void assertJsonEquals(Response res, String jsonPath, Object expectedValue, String description) {
        Object actualValue = res.jsonPath().get(jsonPath);
        assertThat(actualValue)
            .as("Expected %s to be '%s' but actual was '%s'. Response body: %s", description, expectedValue, actualValue, res.asString())
            .isEqualTo(expectedValue);
    }

    public static void assertJsonBoolean(Response res, String jsonPath, boolean expectedValue, String description) {
        boolean actualValue = Boolean.TRUE.equals(res.jsonPath().getBoolean(jsonPath));
        assertThat(actualValue)
            .as("Expected %s to be %s but actual was %s. Response body: %s", description, expectedValue, actualValue, res.asString())
            .isEqualTo(expectedValue);
    }

    public static void assertJsonNotBlank(Response res, String jsonPath, String description) {
        String actualValue = res.jsonPath().getString(jsonPath);
        assertThat(actualValue)
            .as("Expected %s to be non-blank but actual was '%s'. Response body: %s", description, actualValue, res.asString())
            .isNotBlank();
    }

    public static void assertJsonNotNull(Response res, String jsonPath, String description) {
        Object actualValue = res.jsonPath().get(jsonPath);
        assertThat(actualValue)
            .as("Expected %s to be present but actual was null. Response body: %s", description, res.asString())
            .isNotNull();
    }

    public static void assertJsonListNotEmpty(Response res, String jsonPath, String description) {
        java.util.List<?> actualValue = res.jsonPath().getList(jsonPath);
        assertThat(actualValue)
            .as("Expected %s to contain at least one item but actual was %s. Response body: %s", description, actualValue, res.asString())
            .isNotEmpty();
    }

    public static void assertJsonListEmpty(Response res, String jsonPath, String description) {
        java.util.List<?> actualValue = res.jsonPath().getList(jsonPath);
        assertThat(actualValue)
            .as("Expected %s to be empty but actual was %s. Response body: %s", description, actualValue, res.asString())
            .isEmpty();
    }

    public static void assertJsonMapContainsKey(Response res, String jsonPath, String expectedKey, String description) {
        java.util.Map<String, Object> actualValue = res.jsonPath().getMap(jsonPath);
        assertThat(actualValue)
            .as("Expected %s to contain key '%s' but actual keys were %s. Response body: %s", description, expectedKey, actualValue == null ? null : actualValue.keySet(), res.asString())
            .containsKey(expectedKey);
    }

    public static void assertJsonMapContainsKeys(Response res, String jsonPath, String[] expectedKeys, String description) {
        java.util.Map<String, Object> actualValue = res.jsonPath().getMap(jsonPath);
        assertThat(actualValue)
            .as("Expected %s to contain keys %s but actual keys were %s. Response body: %s", description, java.util.Arrays.toString(expectedKeys), actualValue == null ? null : actualValue.keySet(), res.asString())
            .containsKeys(expectedKeys);
    }

    public static void assertJsonContainsMessage(Response res, String expectedText, boolean ignoreCase, String description) {
        String actualValue = res.jsonPath().getString("message");
        if (ignoreCase) {
            assertThat(actualValue)
                .as("Expected %s to contain '%s' ignoring case but actual was '%s'. Response body: %s", description, expectedText, actualValue, res.asString())
                .containsIgnoringCase(expectedText);
        } else {
            assertThat(actualValue)
                .as("Expected %s to contain '%s' but actual was '%s'. Response body: %s", description, expectedText, actualValue, res.asString())
                .contains(expectedText);
        }
    }

    public static void assertHeaderContains(Response res, String headerName, String expectedFragment, String description) {
        String actualValue = res.header(headerName);
        assertThat(actualValue)
            .as("Expected %s header to contain '%s' but actual was '%s'. Response body: %s", description, expectedFragment, actualValue, res.asString())
            .contains(expectedFragment);
    }
}
