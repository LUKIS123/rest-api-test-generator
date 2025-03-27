package pl.edu.pwr.exampleapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ExampleApiApplication {
    // SWAGGER-UI: http://localhost:8085/swagger-ui/index.html#/
    public static void main(String[] args) {
        SpringApplication.run(ExampleApiApplication.class, args);
    }
}
