package api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

//@SpringBootApplication
@ComponentScan(basePackages = {"java"})
@Configuration
@EnableAutoConfiguration
@ComponentScan
public class RestServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(RestServiceApp.class, args);
    }
}
