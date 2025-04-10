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
                    .body("id", equalTo(123))
                    .body("name", equalTo("John Doe"))
                    .body("age", equalTo(30))
                    .header("Content-Type", containsString("application/json"))
                    .header("Location", containsString("https://api.example.com/users/123"));
        }

