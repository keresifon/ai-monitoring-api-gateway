package com.ibm.aimonitoring.gateway.controller;

import com.ibm.aimonitoring.gateway.config.TestRedisConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for FallbackController - circuit breaker fallback responses.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@Import(TestRedisConfig.class)
class FallbackControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void logIngestionFallback_returnsServiceUnavailable() {
        webTestClient.post()
                .uri("/fallback/log-ingestion")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody()
                .jsonPath("$.status").isEqualTo("SERVICE_UNAVAILABLE")
                .jsonPath("$.service").isEqualTo("log-ingestion")
                .jsonPath("$.message").value(m -> assertThat(m).asString().contains("Log Ingestion Service"))
                .jsonPath("$.timestamp").exists();
    }

    @Test
    void logProcessorFallback_returnsServiceUnavailable() {
        webTestClient.get()
                .uri("/fallback/log-processor")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody()
                .jsonPath("$.status").isEqualTo("SERVICE_UNAVAILABLE")
                .jsonPath("$.service").isEqualTo("log-processor")
                .jsonPath("$.message").value(m -> assertThat(m).asString().contains("Log Processor Service"))
                .jsonPath("$.timestamp").exists();
    }

    @Test
    void genericFallback_returnsServiceUnavailable() {
        webTestClient.get()
                .uri("/fallback/generic")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody()
                .jsonPath("$.status").isEqualTo("SERVICE_UNAVAILABLE")
                .jsonPath("$.message").value(m -> assertThat(m).asString().contains("temporarily unavailable"))
                .jsonPath("$.timestamp").exists();
    }
}
