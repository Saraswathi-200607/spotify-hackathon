package com.hackathon.spotify.config;

import com.hackathon.spotify.ratelimit.RateLimiterContractFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;

@Configuration
public class FilterOrderConfig {
    @Bean
    FilterRegistrationBean<RateLimiterContractFilter> rateLimiterRegistration(
            RateLimiterContractFilter filter) {
        FilterRegistrationBean<RateLimiterContractFilter> registration =
                new FilterRegistrationBean<>(filter);
        registration.setOrder(100);
        return registration;
    }
}
