package pl.edu.pwr.exampleapi;

import io.restassured.RestAssured; // REST-assured library
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given; // REST-assured given() method
import static org.hamcrest.Matchers.*; // Hamcrest matchers

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExampleApiApplicationTests {
    @LocalServerPort
    private int port;

    @BeforeEach
    public void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.basePath = "/api/example";
    }

    @Test
    void shouldReturnAllOrdersWithPagination() {
        given()
                .queryParam("page", 1)
                .queryParam("size", 10)
                .when()
                .get("/all")
                .then()
                .statusCode(200)
                .body("items", hasSize(2))
                .body("items[0].customerName", equalTo("Jan Kowalski"))
                .body("items[0].items[0].productName", equalTo("Laptop"))
                .body("totalItemCount", equalTo(2))
                .body("totalPages", equalTo(1))
                .body("itemsFrom", equalTo(1))
                .body("itemsTo", equalTo(10));
    }

    @Test
    void shouldReturnEmptyItemsIfPageIsOutOfBounds() {
        given()
                .queryParam("page", 100)
                .queryParam("size", 10)
                .when()
                .get("/all")
                .then()
                .statusCode(200)
                .body("items", empty());
    }

}
