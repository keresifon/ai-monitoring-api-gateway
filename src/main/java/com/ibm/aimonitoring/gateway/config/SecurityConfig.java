package com.ibm.aimonitoring.gateway.config;

import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.security.web.server.csrf.XorServerCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.server.util.matcher.AndServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.server.WebFilter;

import reactor.core.publisher.Mono;

/**
 * Security configuration with SPA-compatible CSRF protection.
 * JWT authentication is handled by JwtAuthenticationFilter at order -100.
 * CSRF uses CookieServerCsrfTokenRepository so SPA frontends (Angular, React)
 * can read the XSRF-TOKEN cookie and return it as an X-XSRF-TOKEN header.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private static final Set<HttpMethod> SAFE_METHODS = Set.of(
        HttpMethod.GET, HttpMethod.HEAD, HttpMethod.TRACE, HttpMethod.OPTIONS);

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new XorServerCsrfTokenRequestAttributeHandler())
                .requireCsrfProtectionMatcher(csrfProtectionMatcher())
            )
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(formLogin -> formLogin.disable())
            .logout(logout -> logout.disable())
            .authorizeExchange(exchanges -> exchanges
                .anyExchange().permitAll()
            )
            .build();
    }

    /**
     * Requires CSRF only for state-changing methods (POST, PUT, DELETE, PATCH)
     * on paths that are not exempt. Auth and actuator endpoints are exempt because
     * auth has no existing session to protect and actuator is internal management.
     * Log ingestion is exempt: external agents and CLIs post JSON without browser cookies;
     * search remains protected when using state-changing methods if added later.
     */
    private ServerWebExchangeMatcher csrfProtectionMatcher() {
        ServerWebExchangeMatcher stateChangingMethods = exchange ->
            SAFE_METHODS.contains(exchange.getRequest().getMethod())
                ? ServerWebExchangeMatcher.MatchResult.notMatch()
                : ServerWebExchangeMatcher.MatchResult.match();

        ServerWebExchangeMatcher exemptPaths = new OrServerWebExchangeMatcher(
            new PathPatternParserServerWebExchangeMatcher("/actuator/**"),
            new PathPatternParserServerWebExchangeMatcher("/api/v1/auth/**"),
            new PathPatternParserServerWebExchangeMatcher("/api/auth/**"),
            new PathPatternParserServerWebExchangeMatcher("/fallback/**"),
            new PathPatternParserServerWebExchangeMatcher("/api/v1/logs")
        );

        return new AndServerWebExchangeMatcher(
            stateChangingMethods,
            new NegatedServerWebExchangeMatcher(exemptPaths)
        );
    }

    /**
     * Spring Security 6.x defers CSRF token loading by default. This filter
     * subscribes to the token on every request so the XSRF-TOKEN cookie is
     * always set, allowing SPA frontends to read it for subsequent requests.
     */
    @Bean
    public WebFilter csrfCookieWebFilter() {
        return (exchange, chain) -> {
            Mono<CsrfToken> csrfToken = exchange.getAttribute(CsrfToken.class.getName());
            if (csrfToken != null) {
                return csrfToken.then(chain.filter(exchange));
            }
            return chain.filter(exchange);
        };
    }
}
