package com.ibm.aimonitoring.gateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.ibm.aimonitoring.gateway.config.TestRedisConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for JwtAuthenticationManager - JWT token validation.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestRedisConfig.class)
@TestPropertySource(properties = "jwt.secret=test-secret-key-for-testing-purposes-only-minimum-32-characters")
class JwtAuthenticationManagerTest {

    private static final String SECRET = "test-secret-key-for-testing-purposes-only-minimum-32-characters";

    @Autowired
    private JwtAuthenticationManager jwtAuthenticationManager;

    private SecretKey key;

    @BeforeEach
    void setUp() {
        key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void authenticate_validToken_returnsAuthentication() {
        String token = Jwts.builder()
                .subject("testuser")
                .claim("roles", List.of("ROLE_USER"))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();

        UsernamePasswordAuthenticationToken authInput = new UsernamePasswordAuthenticationToken(
                "testuser", token, List.of());

        var result = jwtAuthenticationManager.authenticate(authInput).block();

        assertThat(result).isNotNull();
        assertThat(result.isAuthenticated()).isTrue();
        assertThat(result.getPrincipal()).isEqualTo("testuser");
        assertThat(result.getCredentials()).isEqualTo(token);
        assertThat(result.getAuthorities())
                .hasSize(1)
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    void authenticate_invalidToken_returnsEmptyMono() {
        UsernamePasswordAuthenticationToken authInput = new UsernamePasswordAuthenticationToken(
                null, "invalid-jwt-token", List.of());

        var result = jwtAuthenticationManager.authenticate(authInput).blockOptional();

        assertThat(result).isEmpty();
    }

    @Test
    void authenticate_expiredToken_returnsEmptyMono() {
        String token = Jwts.builder()
                .subject("testuser")
                .claim("roles", List.of("ROLE_USER"))
                .issuedAt(new Date(System.currentTimeMillis() - 7200000))
                .expiration(new Date(System.currentTimeMillis() - 3600000))
                .signWith(key)
                .compact();

        UsernamePasswordAuthenticationToken authInput = new UsernamePasswordAuthenticationToken(
                null, token, List.of());

        var result = jwtAuthenticationManager.authenticate(authInput).blockOptional();

        assertThat(result).isEmpty();
    }

    @Test
    void authenticate_wrongSecret_returnsEmptyMono() {
        var wrongKey = Keys.hmacShaKeyFor("different-secret-key-for-testing-minimum-32-chars".getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("testuser")
                .claim("roles", List.of("ROLE_USER"))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(wrongKey)
                .compact();

        UsernamePasswordAuthenticationToken authInput = new UsernamePasswordAuthenticationToken(
                null, token, List.of());

        var result = jwtAuthenticationManager.authenticate(authInput).blockOptional();

        assertThat(result).isEmpty();
    }
}
