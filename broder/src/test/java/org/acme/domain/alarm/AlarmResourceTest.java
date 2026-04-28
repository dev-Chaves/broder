package org.acme.domain.alarm;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

@QuarkusTest
class AlarmResourceTest {

    @Test
    void shouldListAlarms() {
        given()
                .when().get("/alarms")
                .then()
                .statusCode(200)
                .body("content", notNullValue())
                .body("totalElements", greaterThanOrEqualTo(0))
                .body("totalPages", greaterThanOrEqualTo(0))
                .body("page", equalTo(0))
                .body("size", equalTo(20));
    }

    @Test
    void shouldCreateAndRetrieveAlarm() {
        // Create
        int id = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "Integration Test Alarm",
                            "description": "Created by AlarmResourceTest",
                            "query": "up > 0",
                            "comparison": ">",
                            "threshold": "0",
                            "evaluationIntervalSeconds": 30,
                            "severity": "HIGH",
                            "category": "test"
                        }
                        """)
                .when().post("/alarms")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("Integration Test Alarm"))
                .body("query", equalTo("up > 0"))
                .body("comparison", equalTo(">"))
                .body("threshold", equalTo("0"))
                .body("enabled", equalTo(true))
                .body("status", equalTo("ACTIVE"))
                .extract().path("id");

        // Retrieve
        given()
                .when().get("/alarms/{id}", id)
                .then()
                .statusCode(200)
                .body("id", equalTo(id))
                .body("name", equalTo("Integration Test Alarm"));

        // Cleanup
        given()
                .when().delete("/alarms/{id}", id)
                .then()
                .statusCode(204);

        // Verify deletion
        given()
                .when().get("/alarms/{id}", id)
                .then()
                .statusCode(404);
    }

    @Test
    void shouldUpdateAlarm() {
        // Create
        int id = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "Update Test Alarm",
                            "description": "Before update",
                            "query": "up > 0",
                            "comparison": ">",
                            "threshold": "0",
                            "severity": "MEDIUM",
                            "category": "test"
                        }
                        """)
                .when().post("/alarms")
                .then()
                .statusCode(201)
                .extract().path("id");

        // Update
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "Updated Alarm Name",
                            "description": "After update",
                            "severity": "CRITICAL"
                        }
                        """)
                .when().put("/alarms/{id}", id)
                .then()
                .statusCode(200)
                .body("name", equalTo("Updated Alarm Name"))
                .body("description", equalTo("After update"))
                .body("severity", equalTo("CRITICAL"));

        // Cleanup
        given()
                .when().delete("/alarms/{id}", id)
                .then()
                .statusCode(204);
    }

    @Test
    void shouldReturn404ForNonExistentAlarm() {
        given()
                .when().get("/alarms/999999")
                .then()
                .statusCode(404);
    }

    @Test
    void shouldRejectInvalidAlarmCreation() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "",
                            "query": "up > 0",
                            "comparison": ">",
                            "threshold": "0"
                        }
                        """)
                .when().post("/alarms")
                .then()
                .statusCode(400);
    }
}
