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
                .csrf(csrf -> csrf.disable())
                .httpBasic(Customizer.withDefaults())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        // Swagger alltid öppet
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // Tillåt preflight-requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Admin: alltid autentiserat (GET, POST, PUT, DELETE)
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Protect all modifying API calls
                        .requestMatchers(HttpMethod.POST, "/api/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/**").authenticated()

                        // Public GET API
                        .requestMatchers(HttpMethod.GET, "/api/**").permitAll()

                        // Allt annat tillåts
                        .anyRequest().permitAll()
                );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        var adminUser = User.withUsername(adminUsername)
                .password("{noop}" + adminPassword)
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(adminUser);
    }
}