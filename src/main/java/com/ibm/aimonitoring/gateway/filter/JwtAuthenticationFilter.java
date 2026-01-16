package com.ibm.aimonitoring.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Global filter to validate JWT tokens and add user context headers
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USERNAME = "X-Username";
    private static final String HEADER_USER_ROLES = "X-User-Roles";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        log.debug("JWT Filter - Processing request: {}", path);

        // Skip authentication for public endpoints
        if (isPublicPath(path)) {
            log.debug("JWT Filter - Public path, skipping authentication: {}", path);
            return chain.filter(exchange);
        }

        log.debug("JWT Filter - Protected path, checking authentication: {}", path);

        // Extract JWT token from Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            // Validate and parse JWT token
            Claims claims = validateToken(token);
            
            // Extract user information
            String username = claims.getSubject();
            String userId = claims.get("userId", String.class);
            String roles = claims.get("roles", String.class);

            // Add user context headers to the request
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header(HEADER_USER_ID, userId != null ? userId : "")
                    .header(HEADER_USERNAME, username)
                    .header(HEADER_USER_ROLES, roles != null ? roles : "USER")
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private Claims validateToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/api/v1/auth/") ||
               path.startsWith("/api/auth/") ||  // Keep for backward compatibility
               path.startsWith("/actuator/") ||
               path.equals("/") ||
               path.startsWith("/fallback/") ||
               (path.startsWith("/api/v1/logs") && !path.startsWith("/api/v1/logs/search")) || // Allow log ingestion, but require auth for search
               path.startsWith("/api/v1/dashboard/"); // Allow dashboard endpoints (public read access)
    }

    @Override
    public int getOrder() {
        return -100; // Execute before other filters
    }
}

// Made with Bob