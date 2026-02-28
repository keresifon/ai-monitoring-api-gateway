package com.ibm.aimonitoring.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.cors.reactive.CorsWebFilter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for CorsConfig - CORS filter and configuration.
 * Note: Full CORS header integration tests with Origin are complex with Spring Cloud Gateway
 * due to filter order. We verify bean creation and that the filter doesn't break requests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@Import(TestRedisConfig.class)
class CorsConfigTest {

    @Autowired
    private CorsWebFilter corsWebFilter;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void corsWebFilter_isCreated() {
        assertThat(corsWebFilter).isNotNull();
    }

    @Test
    void request_withoutOrigin_passesThroughFilter() {
        // CORS filter allows requests without Origin; verifies filter doesn't block normal flow
        webTestClient.get()
                .uri("/fallback/generic")
                .exchange()
                .expectStatus().isEqualTo(503);
    }
}
