package pl.edu.pwr.exampleapi;

import io.restassured.RestAssured; // REST-assured library
import io.restassured.http.ContentType; // REST-assured dependency
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given; // REST-assured given() method
import static org.hamcrest.Matchers.*; // Hamcrest matchers

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ExampleApiApplicationTests {
    @LocalServerPort
    private int port;

    @BeforeEach
    public void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.basePath = "/api/example";
    }

    // GET /all endpoint tests
    @Test
    @Order(1)
    void shouldReturnAllOrdersWithPagination() {
        given()
                .baseUri("http://localhost")    // baseUri() is an alternative to RestAssured.baseURI
                .port(port)                     // port() is an alternative to RestAssured.port
                .basePath("/api/example")   // basePath() is an alternative to RestAssured.basePath
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
    @Order(2)
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

    // GET /{id} endpoint tests
    @Test
    @Order(3)
    void shouldReturnOrderByIdWhenExists() {
        given()
                .pathParam("id", 1)
                .when()
                .get("/{id}")
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("customerName", equalTo("Jan Kowalski"))
                .body("items", hasSize(2))
                .body("items[0].productName", equalTo("Laptop"));
    }

    @Test
    @Order(4)
    void shouldReturnNotFoundWhenOrderDoesNotExist() {
        given()
                .pathParam("id", 9999)
                .when()
                .get("/{id}")
                .then()
                .statusCode(404)
                .body("", anEmptyMap()); // checks for {}
    }

    // POST /add endpoint tests
    @Test
    @Order(5)
    void shouldCreateNewOrderAndReturnIdAndLocationHeader() {
        String requestBody = """
                    {
                      "orderDate": "2025-03-27T15:59:42.752Z",
                      "customerName": "string",
                      "items": [
                        {
                          "productName": "string",
                          "quantity": 0,
                          "price": 0
                        }
                      ]
                    }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/add")
                .then()
                .statusCode(201)
                .header("Location", containsString("/api/example/"))
                .body(notNullValue()); // The ID in body
    }

    @Test
    @Order(6)
    void shouldCreateOrderAndThenGetIt() {
        String requestBody = """
                    {
                      "orderDate": "2025-03-27T15:59:42.752Z",
                      "customerName": "Mr. Test",
                      "items": [
                        {
                          "productName": "Laptop",
                          "quantity": 1,
                          "price": 5000
                        }
                      ]
                    }
                """;

        Long newId =
                given()
                        .contentType(ContentType.JSON)
                        .body(requestBody)
                        .when()
                        .post("/add")
                        .then()
                        .statusCode(201)
                        .extract()
                        .as(Long.class);

        // Use the returned ID to GET it
        given()
                .pathParam("id", newId)
                .when()
                .get("/{id}")
                .then()
                .statusCode(200)
                .body("customerName", equalTo("Mr. Test"));
    }

    // DELETE /{id} endpoint tests
    @Test
    @Order(7)
    void shouldDeleteOrderById() {
        given()
                .pathParam("id", 1)
                .when()
                .delete("/{id}")
                .then()
                .statusCode(204)
                .body(is(emptyOrNullString())); // 204 should have empty body
    }

    @Test
    @Order(8)
    void shouldReturnNoContentWhenDeletingNonExistentOrder() {
        given()
                .pathParam("id", 9999)
                .when()
                .delete("/{id}")
                .then()
                .statusCode(204)
                .body(is(emptyOrNullString()));
    }

    @Test
    @Order(9)
    void shouldDeleteOrderAndThenReturnNotFound() {
        long idToDelete = 2;

        // Delete
        given()
                .pathParam("id", idToDelete)
                .when()
                .delete("/{id}")
                .then()
                .statusCode(204);

        // Try to GET the same ID — expect 404
        given()
                .pathParam("id", idToDelete)
                .when()
                .get("/{id}")
                .then()
                .statusCode(404);
    }

}
