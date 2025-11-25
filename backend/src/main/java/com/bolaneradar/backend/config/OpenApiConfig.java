package com.bolaneradar.backend.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Konfiguration för OpenAPI (Swagger UI).
 * Dokumenterar automatiskt alla endpoints och aktiverar Basic Auth i Swagger UI.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // Aktivera Basic Auth i Swagger UI
                .addSecurityItem(new SecurityRequirement().addList("basicAuth"))
                .components(new Components()
                        .addSecuritySchemes("basicAuth",
                                new SecurityScheme()
                                        .name("basicAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("basic")
                        ))
                // Information som visas i Swagger UI
                .info(new Info()
                        .title("BolåneRadar API")
                        .version("1.0")
                        .description("""
                                API för hantering av banker, bolåneräntor och trender.

                                **Publika endpoints (GET)**  
                                Alla `GET /api/**`–anrop är öppna för alla.  
                                Exempel: hämta banker, räntor och historik.

                                **Skyddade endpoints (Basic Auth krävs)**  
                                Alla `POST`, `PUT` och `DELETE` mot `/api/**` kräver inloggning.  
                                Alla endpoints under `/api/admin/**` kräver inloggning.

                                Klicka på **Authorize** längst upp till höger
                                och logga in med dina adminuppgifter för att använda skyddade endpoints.
                                """)
                        .contact(new Contact()
                                .name("Johnny Åström")
                                .email("johnny.astrom@hotmail.com")));
    }
}