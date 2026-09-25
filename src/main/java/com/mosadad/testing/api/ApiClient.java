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

/** RestAssured client for the backend services; logs in once per JVM and reuses the token. */
public class ApiClient {

    private static final Logger log = LogManager.getLogger(ApiClient.class);
    private static final String GATEWAY = ConfigManager.getApiGatewayUrl();

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

    private static volatile String cachedAccessToken;
    private static volatile String cachedRefreshToken;
    private static volatile String cachedEntityId;
    private static volatile String cachedUserId;
    private static volatile String cachedUserType;
    private static final Object LOGIN_LOCK = new Object();

    public ApiClient() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    /** Does not throw on non-2xx; on success caches the session. */
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

    public String accessToken()  { ensureAuthenticated(); return cachedAccessToken; }
    public String entityId()     { ensureAuthenticated(); return cachedEntityId; }
    public String currentUserId(){ ensureAuthenticated(); return cachedUserId; }
    public String currentUserType() { ensureAuthenticated(); return cachedUserType; }

    public RequestSpecification spec(Service service) {
        ensureAuthenticated();
        return given()
                .baseUri(service.baseUri())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Bearer " + cachedAccessToken);
    }

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

    /** No Authorization header — for 401 tests. */
    public RequestSpecification unauthenticatedSpec(Service service) {
        return given()
                .baseUri(service.baseUri())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
    }

}
