package org.acme.resteasy;

import fr.istic.tlc.domain.User;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class ExampleResourceTest {

    @Test
    public void testCreateUser() {
        User user = new User("Michel");
        given()
                .body(user)
                .when()
                .post("/api/users")
                .then()
                .statusCode(200);
    }

}