package com.bolaneradar.backend.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Konfiguration för OpenAPI (Swagger UI).
 * Gör att alla endpoints dokumenteras automatiskt.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BolåneRadar API")
                        .version("1.0")
                        .description("API för hantering av banker, bolåneräntor och trender.")
                        .contact(new Contact()
                                .name("Johnny Åström")
                                .email("johnny.astrom@hotmail.com")));
    }
}