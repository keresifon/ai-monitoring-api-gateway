package com.ibm.aimonitoring.gateway.config;

import java.security.Principal;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import reactor.core.publisher.Mono;

/**
 * Redis {@link org.springframework.cloud.gateway.filter.factory.RequestRateLimiterGatewayFilterFactory}
 * requires a non-empty key. The default {@code PrincipalNameKeyResolver} yields an empty key for
 * unauthenticated requests, which makes the gateway return <strong>403 Forbidden</strong> before
 * the request reaches upstream services. Public routes (e.g. {@code POST /api/v1/logs}) must fall
 * back to a client identifier (forwarded IP or remote address).
 */
@Configuration
public class RateLimiterConfig {

    @Bean
    public KeyResolver rateLimiterKeyResolver() {
        return exchange -> exchange.getPrincipal()
                .map(Principal::getName)
                .switchIfEmpty(Mono.fromCallable(() -> clientKey(exchange)));
    }

    private static String clientKey(org.springframework.web.server.ServerWebExchange exchange) {
        String forwarded = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        var addr = exchange.getRequest().getRemoteAddress();
        if (addr != null) {
            return addr.getAddress().getHostAddress();
        }
        return "unknown";
    }
}
