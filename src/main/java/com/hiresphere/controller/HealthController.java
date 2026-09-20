package com.hiresphere.controller;

import com.hiresphere.service.HealthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * HealthController - Provides a health check endpoint for CareerHub backend.
 */
@Tag(name = "System Health", description = "Server status, health diagnostics, and timestamp checks")
@RestController
@RequestMapping("/api")
public class HealthController {

    private final HealthService healthService;

    // Constructor Injection - Spring injects HealthService automatically
    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    /**
     * GET /api/health
     *
     * Returns a JSON response confirming the backend is alive.
     * This is useful for testing that the server started correctly.
     *
     * Example response:
     * {
     *   "status": "UP",
     *   "message": "CareerHub Backend is running",
     *   "timestamp": "2025-01-01T10:00:00",
     *   "usersInMemory": 0
     * }
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {

        // Map acts like a JSON object: key → value
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", healthService.getStatus());
        response.put("message", "HireSphere AI Backend is running");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.put("usersInMemory", healthService.getTotalUsersInMemory());

        // ResponseEntity.ok() sends HTTP 200 OK with the body
        return ResponseEntity.ok(response);
    }
}
