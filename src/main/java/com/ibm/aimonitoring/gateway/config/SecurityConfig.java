package com.ibm.aimonitoring.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Security configuration that permits all requests.
 * JWT authentication is handled by JwtAuthenticationFilter at order -100.
 * This configuration only ensures Spring Security doesn't block requests.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            // Note: Lambdas are required here as Spring Security's Spec classes are not public.
            // Method references cannot be used as the Spec types are package-private.
            // SonarQube warning S1612 is a false positive for this Spring Security API pattern.
            .csrf(csrf -> csrf.disable()) // NOSONAR - Spring Security API requires lambda
            .httpBasic(httpBasic -> httpBasic.disable()) // NOSONAR - Spring Security API requires lambda
            .formLogin(formLogin -> formLogin.disable()) // NOSONAR - Spring Security API requires lambda
            .logout(logout -> logout.disable()) // NOSONAR - Spring Security API requires lambda
            .authorizeExchange(exchanges -> exchanges
                // Permit all requests - authentication is handled by JwtAuthenticationFilter
                .anyExchange().permitAll()
            )
            .build();
    }
}

// Made with Bob
