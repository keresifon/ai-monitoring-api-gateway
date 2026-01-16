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
            .csrf(csrf -> csrf.disable())
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(formLogin -> formLogin.disable())
            .logout(logout -> logout.disable())
            .authorizeExchange(exchanges -> exchanges
                // Permit all requests - authentication is handled by JwtAuthenticationFilter
                .anyExchange().permitAll()
            )
            .build();
    }
}

// Made with Bob
