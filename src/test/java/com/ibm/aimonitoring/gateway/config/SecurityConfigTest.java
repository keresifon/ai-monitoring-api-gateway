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
 * Tests for SecurityConfig (CSRF disabled; public routes must not return 403 from security).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@Import(TestRedisConfig.class)
class SecurityConfigTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void getRequest_actuatorHealth_ok() {
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void postToAuthLogin_notForbiddenBySecurity() {
        var result = webTestClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .returnResult(Void.class);

        assertThat(result.getStatus()).as("Should not get 403 from gateway security")
                .isNotEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void postToLogIngestion_notForbiddenBySecurity() {
        String body = "{\"level\":\"INFO\",\"message\":\"test\",\"service\":\"test\"}";
        var result = webTestClient.post()
                .uri("/api/v1/logs")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .returnResult(Void.class);

        assertThat(result.getStatus()).as("Should not get 403 from gateway security")
                .isNotEqualTo(HttpStatus.FORBIDDEN);
    }
}
