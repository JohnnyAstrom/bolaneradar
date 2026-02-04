package com.bolaneradar.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * ================================================================
 * CORS CONFIGURATION
 * ================================================================
 * <p>
 * Hanterar Cross-Origin Resource Sharing (CORS) för API:et.
 * <p>
 * Tillåter frontend-applikationer att anropa backend:
 * - Produktionsfrontend (Render)
 * - Lokal utvecklingsmiljö (localhost)
 * <p>
 * Gäller endast API-endpoints under /api/**.
 * ================================================================
 */
@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(
                                "https://bolaneradar.onrender.com",
                                "https://bolaneradar.vercel.app",
                                "http://localhost:5173"
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }
}