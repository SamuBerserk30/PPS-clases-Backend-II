package co.edu.cesde.pps;

import
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PpsApplication {

    public static void main(String[] args) {
        DotenvDevelopmentLoader.load().
        SpringApplication.run(PpsApplication.class, args);
    }
}
