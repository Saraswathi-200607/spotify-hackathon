package com.hackathon.spotify.config;

import com.hackathon.spotify.ratelimit.RateLimiterContractFilter;
import com.hackathon.spotify.security.AuthFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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
                .csrf(csrf -> csrf.disable())

                .cors(cors -> {
                })

                .sessionManagement(session -> session.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/health")
                        .permitAll()

                        .anyRequest().authenticated())

                /*
                 * Authentication first.
                 *
                 * AuthFilter validates JWT and puts:
                 * USER_ID
                 * PLAN
                 *
                 * into the request.
                 */
                .addFilterBefore(
                        authFilter,
                        UsernamePasswordAuthenticationFilter.class)

                /*
                 * Rate limiter runs AFTER AuthFilter.
                 *
                 * Therefore the rate limiter can read
                 * the authenticated user's ID and plan.
                 */
                .addFilterAfter(
                        rateLimiterContractFilter,
                        AuthFilter.class);

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(
                List.of("http://localhost:5173"));

        config.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "OPTIONS"));

        config.setAllowedHeaders(
                List.of("*"));

        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                config);

        return source;
    }
}