package com.hackathon.spotify.config;

import com.hackathon.spotify.ratelimit.RateLimiterContractFilter;
import com.hackathon.spotify.security.AuthFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class SecurityConfig {

    private final AuthFilter authFilter;
    private final RateLimiterContractFilter rateLimiterContractFilter;

    public SecurityConfig(
            AuthFilter authFilter,
            RateLimiterContractFilter rateLimiterContractFilter) {

        this.authFilter = authFilter;
        this.rateLimiterContractFilter = rateLimiterContractFilter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                // Disable CSRF because this is a stateless REST API
                .csrf(csrf -> csrf.disable())

                // Enable CORS
                .cors(cors -> {
                })

                // JWT-based authentication; no server-side sessions
                .sessionManagement(session -> session.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        // Allow browser CORS preflight requests
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**")
                        .permitAll()

                        // Public authentication and health endpoints
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/health")
                        .permitAll()

                        // Everything else requires authentication
                        .anyRequest()
                        .authenticated())

                // JWT authentication filter
                .addFilterBefore(
                        authFilter,
                        UsernamePasswordAuthenticationFilter.class)

                // Rate limiting filter
                .addFilterAfter(
                        rateLimiterContractFilter,
                        AuthFilter.class);

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        /*
         * Frontend URL is provided through the Render
         * environment variable FRONTEND_URL.
         */
        String frontendUrl = System.getenv("FRONTEND_URL");

        if (frontendUrl == null || frontendUrl.isBlank()) {
            frontendUrl = "http://localhost:5173";
        }

        config.setAllowedOrigins(
                Arrays.asList(frontendUrl));

        config.setAllowedMethods(
                Arrays.asList(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "OPTIONS"));

        config.setAllowedHeaders(
                Arrays.asList("*"));

        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                config);

        return source;
    }
}