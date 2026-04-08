package com.ibm.aimonitoring.gateway.config;

import java.net.InetSocketAddress;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimiterConfigTest {

    @Test
    void keyResolver_usesForwardedForWhenNoPrincipal() {
        KeyResolver resolver = new RateLimiterConfig().rateLimiterKeyResolver();

        MockServerHttpRequest request = MockServerHttpRequest.post("/api/v1/logs")
                .header("X-Forwarded-For", "203.0.113.1, 10.0.0.1")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(resolver.resolve(exchange))
                .assertNext(key -> assertThat(key).isEqualTo("203.0.113.1"))
                .verifyComplete();
    }

    @Test
    void keyResolver_usesRemoteAddressWhenNoForwardedHeader() {
        KeyResolver resolver = new RateLimiterConfig().rateLimiterKeyResolver();

        MockServerHttpRequest request = MockServerHttpRequest.post("/api/v1/logs")
                .remoteAddress(new InetSocketAddress("198.51.100.2", 443))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(resolver.resolve(exchange))
                .assertNext(key -> assertThat(key).isEqualTo("198.51.100.2"))
                .verifyComplete();
    }
}
