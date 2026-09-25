package com.mosadad.testing.api;

import io.restassured.response.Response;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Assertions for the backend's {statusCode, message, response, isSuccess, errors}
 * envelope. Business failures usually come back as HTTP 200 with the real
 * status inside the envelope, so check both.
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

    public static void assertHttpUnauthorized(Response res) {
        assertThat(res.statusCode())
            .as("Expected HTTP 401 unauthorized but actual was %s. %s", res.statusCode(), responseSummary(res))
            .isEqualTo(401);
    }

    /**
     * Model-binding validation failures return a real HTTP 400 ProblemDetails
     * body instead of the envelope — a safe way to hit create endpoints without
     * writing data.
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

    public static void assertJsonBoolean(Response res, String jsonPath, boolean expectedValue, String description) {
        boolean actualValue = Boolean.TRUE.equals(res.jsonPath().getBoolean(jsonPath));
        assertThat(actualValue)
            .as("Expected %s to be %s but actual was %s. Response body: %s", description, expectedValue, actualValue, res.asString())
            .isEqualTo(expectedValue);
    }

}
