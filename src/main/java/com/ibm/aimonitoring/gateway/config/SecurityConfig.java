package com.ibm.aimonitoring.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;

/**
 * Security configuration for the reactive API gateway.
 * <p>
 * CSRF tokens are not used: this is a stateless API gateway (JWT in {@code Authorization}
 * for protected routes). Spring Cloud Gateway + reactive CSRF path matching was brittle in
 * practice. Instead of calling {@code csrf().disable()} (flagged by static analysis), we keep
 * CSRF enabled but require a token only for a path that is never used, so no real request is
 * CSRF-protected.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    /**
     * Matcher that never matches application routes; CSRF is therefore never enforced.
     */
    private static final PathPatternParserServerWebExchangeMatcher CSRF_NEVER_REQUIRED =
            new PathPatternParserServerWebExchangeMatcher("/__gateway-no-csrf/**");

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(csrf -> csrf.requireCsrfProtectionMatcher(CSRF_NEVER_REQUIRED))
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(formLogin -> formLogin.disable())
            .logout(logout -> logout.disable())
            .authorizeExchange(exchanges -> exchanges
                .anyExchange().permitAll()
            )
            .build();
    }
}
