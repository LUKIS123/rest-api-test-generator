    package pl.edu.pwr;

    import io.restassured.RestAssured; // REST-assured library
    import io.restassured.http.ContentType; // REST-assured dependency
    import org.junit.jupiter.api.*; // JUnit

    import static io.restassured.RestAssured.given; // REST-assured given method
    import static org.hamcrest.Matchers.*; // Hamcrest matchers

    class myExampleTestClass {

        @BeforeEach
        public void setup() {
            RestAssured.baseURI = "http://localhost";
            RestAssured.port = 8080;
            RestAssured.basePath = "/api/users";
        }

        @Test
        void myExampleTest() {
            given()
                    .baseUri("https://api.example.com")
                    .basePath("/users")
                    .header("Authorization", "Bearer token")
                    .queryParam("id", 123)
                .when()
                    .get("/test")
                .then()
                    .statusCode(200)
                    .body("username", notNullValue())
                    .body("password", notNullValue())
                    .body("email", notNullValue())
                    .body("list[0].id", equalTo(123))
                    .body("name", equalTo("John Doe"))
                    .body("age", equalTo(30))
                    .header("Content-Type", equalTo("application/json"))
                    .header("Location", equalTo("https://api.example.com/users/123"));
        }

        @Test
        void myExampleTest2() {
            given()
                    .baseUri("https://api.example.com")
                    .basePath("/users")
                    .header("Authorization", "Bearer token")
                    .queryParam("id", 123)
                .when()
                    .get("/test")
                .then()
                    .statusCode(200)
                    .body("username", notNullValue())
                    .body("password", notNullValue())
                    .body("email", notNullValue())
                    .body("list[0].id", equalTo(123))
                    .body("name", equalTo("John Doe"))
                    .body("age", equalTo(30))
                    .header("Content-Type", equalTo("application/json"))
                    .header("Location", equalTo("https://api.example.com/users/123"));
        }

    }
