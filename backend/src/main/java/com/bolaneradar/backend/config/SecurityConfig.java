package com.bolaneradar.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Disable CSRF for API usage
                .csrf(csrf -> csrf.disable())

                // Enable HTTP Basic for admin routes
                .httpBasic(Customizer.withDefaults())

                // Stateless API (no sessions stored)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // ----------------------------
                        // 1) Swagger – always public
                        // ----------------------------
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // ----------------------------
                        // 2) CORS preflight OPTIONS
                        // ----------------------------
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ----------------------------
                        // 3) PUBLIC SmartRate API
                        //    POST /api/smartrate/**
                        // ----------------------------
                        .requestMatchers(HttpMethod.POST, "/api/smartrate/**").permitAll()

                        // ----------------------------
                        // 4) ADMIN API
                        //    Everything under /api/admin requires login
                        // ----------------------------
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // ----------------------------
                        // 5) Other modifying API endpoints
                        // ----------------------------
                        .requestMatchers(HttpMethod.POST, "/api/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/**").authenticated()

                        // ----------------------------
                        // 6) Public GET API endpoints
                        // ----------------------------
                        .requestMatchers(HttpMethod.GET, "/api/**").permitAll()

                        // ----------------------------
                        // 7) Everything else is allowed
                        // ----------------------------
                        .anyRequest().permitAll()
                )

                // Enable CORS so WebConfig applies
                .cors(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {

        var adminUser = User.withUsername(adminUsername)
                .password(passwordEncoder.encode(adminPassword))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(adminUser);
    }
}