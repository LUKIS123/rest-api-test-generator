package pl.edu.pwr.exampleapi;

import io.restassured.RestAssured; // REST-assured library
import io.restassured.http.ContentType; // REST-assured dependency
import org.junit.jupiter.api.*; // JUnit
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static io.restassured.RestAssured.given; // REST-assured given method
import static org.hamcrest.Matchers.*; // Hamcrest matchers

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class myExampleTestClass {

    @BeforeEach
    public void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8085;
        RestAssured.basePath = "/api/example";
    }

    @Test
    void myExampleTest() {
        given()
                .when()
                .get("/1")
                .then()
                .statusCode(200)
                .body("items", notNullValue())
                .body("id", equalTo(1))
                .body("customerName", equalTo("Jan Kowalski"))
                .body("items[0].productName", equalTo("Laptop"));
    }

}
