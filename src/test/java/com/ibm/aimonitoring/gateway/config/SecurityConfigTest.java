package com.ibm.aimonitoring.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for SecurityConfig - CSRF exemption and filter behavior.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@Import(TestRedisConfig.class)
class SecurityConfigTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void getRequest_safeMethod_doesNotRequireCsrf() {
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void postToExemptAuthPath_doesNotRequireCsrf() {
        var result = webTestClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .returnResult(Void.class);

        assertThat(result.getStatus()).as("Should not get 403 CSRF on exempt auth path")
                .isNotEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void postToLogIngestionPath_doesNotRequireCsrf() {
        String body = "{\"level\":\"INFO\",\"message\":\"csrf test\",\"service\":\"test\"}";
        var result = webTestClient.post()
                .uri("/api/v1/logs")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .returnResult(Void.class);

        assertThat(result.getStatus()).as("Should not get 403 CSRF on log ingestion path")
                .isNotEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void getRequest_includesCsrfCookieWhenFilterRuns() {
        var responseHeaders = webTestClient.get()
                .uri("/fallback/generic")
                .exchange()
                .expectStatus().isEqualTo(503) // Fallback returns 503
                .returnResult(Void.class)
                .getResponseHeaders();

        assertThat(responseHeaders.get("Set-Cookie"))
                .as("XSRF-TOKEN cookie should be set by CSRF filter")
                .anyMatch(cookie -> cookie.startsWith("XSRF-TOKEN="));
    }
}
