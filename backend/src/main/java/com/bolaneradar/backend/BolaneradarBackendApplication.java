package com.bolaneradar.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BolaneradarBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BolaneradarBackendApplication.class, args);
    }

}
