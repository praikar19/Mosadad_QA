package com.mosadad.testing.tests.api.inthub;

import com.mosadad.testing.api.ApiAssertions;
import com.mosadad.testing.base.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** inthub service — Workshop. The real path is the service root POST /List, not /Workshop/List. */
@Epic("Mosadad Recovery Claim")
@Feature("IntHub API — Workshop")
public class WorkshopApiTest extends BaseApiTest {

    @Test(groups = {"api", "inthub", "workshop"})
    @Severity(SeverityLevel.NORMAL)
    @Description("POST /List with an empty filter returns 200 with a real, non-empty list of UAE repair workshops.")
    public void listWorkshopsWithEmptyFilterReturns200() {
        Response res = inthub().body(Map.of()).post("/List");
        ApiAssertions.assertEnvelopeSuccess(res);
        List<Map<String, Object>> workshops = res.jsonPath().getList("response");
        assertThat(workshops).isNotEmpty();
        assertThat(workshops.get(0)).containsKeys("id", "name");
    }
}
