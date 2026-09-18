package com.mosadad.testing.base;

import com.mosadad.testing.api.ApiClient;
import com.mosadad.testing.api.ApiClient.Service;
import io.restassured.specification.RequestSpecification;

/**
 * Base for every pure API test class (no browser). All 6 backend
 * microservices share one authenticated session — {@code apiClient}
 * bootstraps the real login once per JVM on first use (see
 * {@link ApiClient#ensureAuthenticated()}), so dozens of test classes don't
 * each pay for their own login call.
 *
 * Short per-service accessors below keep test bodies readable:
 * {@code claims().get("/Claim/Latest/" + entityId())} instead of spelling
 * out {@code apiClient.spec(Service.CLAIMS)} in every method.
 */
public abstract class BaseApiTest extends BaseTest {

    protected RequestSpecification claims()     { return apiClient.spec(Service.CLAIMS); }
    protected RequestSpecification inthub()     { return apiClient.spec(Service.INTHUB); }
    protected RequestSpecification invoice()    { return apiClient.spec(Service.INVOICE); }
    protected RequestSpecification quotation()  { return apiClient.spec(Service.QUOTATION); }
    protected RequestSpecification settlement() { return apiClient.spec(Service.SETTLEMENT); }
    protected RequestSpecification tenant()     { return apiClient.spec(Service.TENANT); }

    /** No Authorization header — for "missing token returns 401" tests. */
    protected RequestSpecification noAuth(Service service) { return apiClient.unauthenticatedSpec(service); }

    /** Authenticated request spec with an explicit (e.g. malformed/expired) token, bypassing the cached session. */
    protected RequestSpecification withToken(Service service, String token) { return apiClient.specWithToken(service, token); }

    protected String entityId()      { return apiClient.entityId(); }
    protected String currentUserId() { return apiClient.currentUserId(); }
    protected String currentUserType() { return apiClient.currentUserType(); }
}
