package com.bolaneradar.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Huvudentré för BolåneRadar backend.
 * <p>
 * Applikationen exponerar ett REST-API för publik och administrativ användning.
 * Tidsstyrda jobb (t.ex. scraping) körs externt via CI/CD eller batch-runner,
 * inte via Spring @Scheduled.
 */
@SpringBootApplication
public class BolaneradarBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BolaneradarBackendApplication.class, args);
    }
}