package com.ibm.aimonitoring.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Fallback controller for circuit breaker failures
 * Provides graceful degradation when backend services are unavailable
 */
@Slf4j
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @PostMapping("/log-ingestion")
    public ResponseEntity<Map<String, Object>> logIngestionFallback() {
        log.warn("Log Ingestion Service is currently unavailable - Circuit breaker activated");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SERVICE_UNAVAILABLE");
        response.put("message", "Log Ingestion Service is temporarily unavailable. Please try again later.");
        response.put("timestamp", Instant.now().toString());
        response.put("service", "log-ingestion");
        
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }

    @GetMapping("/log-processor")
    public ResponseEntity<Map<String, Object>> logProcessorFallback() {
        log.warn("Log Processor Service is currently unavailable - Circuit breaker activated");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SERVICE_UNAVAILABLE");
        response.put("message", "Log Processor Service is temporarily unavailable. Please try again later.");
        response.put("timestamp", Instant.now().toString());
        response.put("service", "log-processor");
        
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }

    @GetMapping("/generic")
    public ResponseEntity<Map<String, Object>> genericFallback() {
        log.warn("A backend service is currently unavailable - Circuit breaker activated");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SERVICE_UNAVAILABLE");
        response.put("message", "The requested service is temporarily unavailable. Please try again later.");
        response.put("timestamp", Instant.now().toString());
        
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }
}

// Made with Bob