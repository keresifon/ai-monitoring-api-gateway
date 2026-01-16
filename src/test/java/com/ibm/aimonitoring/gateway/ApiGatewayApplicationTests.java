package com.ibm.aimonitoring.gateway;

import com.ibm.aimonitoring.gateway.config.TestRedisConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Basic integration test for API Gateway Application
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "jwt.secret=test-secret-key-for-testing-purposes-only-minimum-32-characters",
        "spring.cloud.gateway.routes[0].id=test-route",
        "spring.cloud.gateway.routes[0].uri=http://localhost:8081",
        "spring.cloud.gateway.routes[0].predicates[0]=Path=/api/v1/test/**",
        "spring.cloud.gateway.routes[0].filters[0].name=CircuitBreaker",
        "spring.cloud.gateway.routes[0].filters[0].args.name=testCircuitBreaker",
        "spring.cloud.gateway.routes[0].filters[0].args.fallbackUri=forward:/fallback/test"
    }
)
@ActiveProfiles("test")
@Import(TestRedisConfig.class)
class ApiGatewayApplicationTests {

    @Test
    void contextLoads() {
        // Verifies that the Spring application context loads successfully
    }
}

// Made with Bob