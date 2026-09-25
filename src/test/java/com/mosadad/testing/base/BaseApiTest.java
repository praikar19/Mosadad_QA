package com.mosadad.testing.base;

import com.mosadad.testing.api.ApiClient;
import com.mosadad.testing.api.ApiClient.Service;
import io.restassured.specification.RequestSpecification;

/** Base for API tests: one shared authenticated session per JVM. */
public abstract class BaseApiTest extends BaseTest {

    protected RequestSpecification claims()     { return apiClient.spec(Service.CLAIMS); }
    protected RequestSpecification inthub()     { return apiClient.spec(Service.INTHUB); }
    protected RequestSpecification invoice()    { return apiClient.spec(Service.INVOICE); }
    protected RequestSpecification quotation()  { return apiClient.spec(Service.QUOTATION); }
    protected RequestSpecification settlement() { return apiClient.spec(Service.SETTLEMENT); }
    protected RequestSpecification tenant()     { return apiClient.spec(Service.TENANT); }

    protected RequestSpecification noAuth(Service service) { return apiClient.unauthenticatedSpec(service); }

    protected RequestSpecification withToken(Service service, String token) { return apiClient.specWithToken(service, token); }

    protected String entityId()      { return apiClient.entityId(); }
    protected String currentUserId() { return apiClient.currentUserId(); }
    protected String currentUserType() { return apiClient.currentUserType(); }
}
