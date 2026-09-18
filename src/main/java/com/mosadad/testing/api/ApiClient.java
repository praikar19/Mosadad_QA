package com.mosadad.testing.api;

import com.mosadad.testing.config.ConfigManager;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * RestAssured-backed HTTP client for Mosadad's real backend — six
 * microservices sitting behind one shared Azure API Management gateway.
 *
 * <p><b>Base URL — CONFIRMED LIVE 2026-09-17.</b> Captured directly from
 * the Angular app's own network traffic (not the Swagger UI hosts, which
 * are a separate direct-to-origin path used only for API discovery):
 * {@code https://recovery-api-management-qa.azure-api.net/api/<service>},
 * where {@code <service>} is one of claims / inthub / invoice / quotation /
 * settlement / tenant — see {@link Service}.
 *
 * <p><b>Auth — CONFIRMED LIVE.</b> {@code POST /tenant/User/Login} with an
 * XOR-obfuscated email/password (see {@link EncryptionUtil}) returns a JWT
 * Bearer token good for every other endpoint; a request with no token (or a
 * stale one) gets a real {@code 401}, confirmed against the live gateway
 * with no token at all (no Ocp-Apim-Subscription-Key or similar gateway
 * key required). The token is cached statically (JVM-wide, one real login
 * call for the whole suite) since every API test class otherwise pays for
 * its own {@code @BeforeClass} — see {@code BaseApiTest}.
 */
public class ApiClient {

    private static final Logger log = LogManager.getLogger(ApiClient.class);
    private static final String GATEWAY = ConfigManager.getApiGatewayUrl();

    /** One backend microservice behind the shared APIM gateway — the path segment matches its Swagger UI. */
    public enum Service {
        CLAIMS("claims"),
        INTHUB("inthub"),
        INVOICE("invoice"),
        QUOTATION("quotation"),
        SETTLEMENT("settlement"),
        TENANT("tenant");

        public final String segment;
        Service(String segment) { this.segment = segment; }

        public String baseUri() { return GATEWAY + "/" + segment; }
    }

    /* ── Shared (JVM-wide) authenticated session — one real login for the whole suite ── */
    private static volatile String cachedAccessToken;
    private static volatile String cachedRefreshToken;
    private static volatile String cachedEntityId;
    private static volatile String cachedUserId;
    private static volatile String cachedUserType;
    private static final Object LOGIN_LOCK = new Object();

    public ApiClient() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    /* ── Auth ─────────────────────────────────────────────────────────── */

    /**
     * POST /tenant/User/Login with the given plaintext credentials
     * (encrypted client-side exactly like the real Angular app does — see
     * {@link EncryptionUtil}). Does NOT throw on non-2xx; callers assert on
     * the returned {@link Response} (e.g. a 401-on-bad-credentials test).
     * On success, caches the token/entityId/user for {@link #ensureAuthenticated()}.
     */
    public Response login(String email, String password) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("email", EncryptionUtil.encryptLoginField(email));
        body.put("password", EncryptionUtil.encryptLoginField(password));

        Response res = given()
                .baseUri(Service.TENANT.baseUri())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(body)
                .post("/User/Login");

        log.info("POST /tenant/User/Login [{}] -> {}", email, res.statusCode());

        if (res.statusCode() == 200 && Boolean.TRUE.equals(res.jsonPath().getBoolean("isSuccess"))) {
            synchronized (LOGIN_LOCK) {
                cachedAccessToken = res.jsonPath().getString("response.token.accessToken");
                cachedRefreshToken = res.jsonPath().getString("response.token.refreshToken");
                cachedEntityId = res.jsonPath().getString("response.user.entityId");
                cachedUserId = res.jsonPath().getString("response.user.id");
                cachedUserType = res.jsonPath().getString("response.user.userType");
            }
        }
        return res;
    }

    /** POST /tenant/User/RefreshToken using the cached refresh token from the last successful login. */
    public Response refreshToken() {
        ensureAuthenticated();
        Map<String, String> body = Map.of("refreshToken", cachedRefreshToken == null ? "" : cachedRefreshToken);
        return given()
                .baseUri(Service.TENANT.baseUri())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(body)
                .post("/User/RefreshToken");
    }

    /** Logs in once per JVM using the default QA Claimant Insurer account (credentials.properties: qa.claimant.*.dubai). Cheap no-op on subsequent calls. */
    public void ensureAuthenticated() {
        if (cachedAccessToken == null) {
            synchronized (LOGIN_LOCK) {
                if (cachedAccessToken == null) {
                    Response res = login(ConfigManager.getEmail("qa", "dubai"), ConfigManager.getPassword("qa", "dubai"));
                    if (res.statusCode() != 200 || cachedAccessToken == null) {
                        throw new IllegalStateException(
                            "Default QA login failed — cannot bootstrap authenticated API session. " +
                            "Status=" + res.statusCode() + " body=" + res.asString());
                    }
                }
            }
        }
    }

    /** Drops the cached session so the next {@link #ensureAuthenticated()} performs a fresh real login. */
    public void clearSession() {
        synchronized (LOGIN_LOCK) {
            cachedAccessToken = null;
            cachedRefreshToken = null;
            cachedEntityId = null;
            cachedUserId = null;
            cachedUserType = null;
        }
    }

    public String accessToken()  { ensureAuthenticated(); return cachedAccessToken; }
    public String entityId()     { ensureAuthenticated(); return cachedEntityId; }
    public String currentUserId(){ ensureAuthenticated(); return cachedUserId; }
    public String currentUserType() { ensureAuthenticated(); return cachedUserType; }

    /* ── Request builders ────────────────────────────────────────────── */

    /** Authenticated request spec for the given service — auto-bootstraps the shared session on first use. */
    public RequestSpecification spec(Service service) {
        ensureAuthenticated();
        return given()
                .baseUri(service.baseUri())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Bearer " + cachedAccessToken);
    }

    /** Same as {@link #spec(Service)} but with an explicit token — for expired/malformed/foreign-token negative tests. */
    public RequestSpecification specWithToken(Service service, String token) {
        RequestSpecification rs = given()
                .baseUri(service.baseUri())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
        if (token != null && !token.isEmpty()) {
            rs = rs.header("Authorization", "Bearer " + token);
        }
        return rs;
    }

    /** No Authorization header at all — for "must return 401 without a token" tests. */
    public RequestSpecification unauthenticatedSpec(Service service) {
        return given()
                .baseUri(service.baseUri())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
    }

    /** Authenticated multipart spec (file uploads: Attachment, Documents, FastTrack). */
    public RequestSpecification multipartSpec(Service service) {
        ensureAuthenticated();
        return given()
                .baseUri(service.baseUri())
                .accept(ContentType.JSON)
                .header("Authorization", "Bearer " + cachedAccessToken);
    }
}
