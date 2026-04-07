package com.ibm.aimonitoring.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Security configuration for the reactive API gateway.
 * <p>
 * CSRF is disabled: Spring Cloud Gateway plus reactive CSRF path matching is brittle
 * (pathWithinApplication vs request path), and programmatic clients (curl, agents) cannot
 * send browser XSRF cookies. JWT authentication for protected routes is enforced by
 * {@link com.ibm.aimonitoring.gateway.filter.JwtAuthenticationFilter}; the SPA uses JWT in
 * storage, not a server session cookie, so gateway CSRF does not meaningfully mitigate
 * cross-site requests for API calls.
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
                .anyExchange().permitAll()
            )
            .build();
    }
}
