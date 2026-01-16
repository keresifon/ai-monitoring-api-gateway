package com.ibm.aimonitoring.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * CORS Configuration for API Gateway
 * Reads allowed origins from environment variable CORS_ALLOWED_ORIGINS
 */
@Configuration
public class CorsConfig {

    @Value("${CORS_ALLOWED_ORIGINS:http://localhost:4200,http://localhost:3000,http://localhost:80,http://localhost,http://frontend:80,http://frontend}")
    private String allowedOrigins;

    @Bean
    public CorsWebFilter corsWebFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Parse comma-separated origins using a safe, non-regex approach
        // This avoids ReDoS vulnerabilities from backtracking in regex patterns
        List<String> origins = parseOrigins(allowedOrigins);
        config.setAllowedOrigins(origins);
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }

    /**
     * Safely parse comma-separated origins without using vulnerable regex patterns.
     * This method avoids ReDoS (Regular Expression Denial of Service) vulnerabilities
     * by using simple string operations instead of regex with quantifiers.
     * 
     * @param originsString Comma-separated string of allowed origins
     * @return List of trimmed origin strings
     */
    private List<String> parseOrigins(String originsString) {
        if (originsString == null || originsString.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Limit input size to prevent DoS attacks
        final int MAX_INPUT_LENGTH = 10000;
        if (originsString.length() > MAX_INPUT_LENGTH) {
            throw new IllegalArgumentException("CORS_ALLOWED_ORIGINS exceeds maximum length of " + MAX_INPUT_LENGTH);
        }
        
        List<String> origins = new ArrayList<>();
        // Use simple comma split and trim - no regex backtracking
        String[] parts = originsString.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                origins.add(trimmed);
            }
        }
        
        return origins;
    }
}
