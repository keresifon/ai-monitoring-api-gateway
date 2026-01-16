package com.ibm.aimonitoring.gateway.security;

import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Matcher that identifies protected paths that require authentication.
 * Returns match() for protected paths (require authentication).
 * Returns notMatch() for public paths (skip authentication).
 */
@Component
public class PublicPathMatcher implements ServerWebExchangeMatcher {

    @Override
    public Mono<MatchResult> matches(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().value();
        
        // Public paths - skip authentication (return notMatch)
        if (path.startsWith("/api/v1/auth/") ||
            path.startsWith("/api/auth/") ||
            path.equals("/actuator/health")) {
            return MatchResult.notMatch();
        }
        
        // Protected paths - require authentication (return match)
        return MatchResult.match();
    }
}

// Made with Bob
