package com.ibm.aimonitoring.gateway.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test configuration to mock Redis connections for tests.
 * This prevents the need for a running Redis instance during tests.
 */
@TestConfiguration
public class TestRedisConfig {

    @Bean
    @Primary
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
        ReactiveRedisConnectionFactory factory = mock(ReactiveRedisConnectionFactory.class);
        ReactiveRedisConnection connection = mock(ReactiveRedisConnection.class);
        
        // Mock getReactiveConnection() to return a Mono with the mocked connection
        when(factory.getReactiveConnection()).thenAnswer(invocation -> Mono.just(connection));
        
        // Note: We don't need to mock close() for context loading to work
        // The connection mock will handle any method calls that aren't explicitly mocked
        
        return factory;
    }

}
