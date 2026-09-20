package com.hiresphere.controller;

import com.hiresphere.dto.AuthResponse;
import com.hiresphere.dto.LoginRequest;
import com.hiresphere.dto.RegisterRequest;
import com.hiresphere.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AuthController - Exposes the authentication REST endpoints.
 */
@Tag(name = "Authentication", description = "Registration, login, and session authentication for Candidates and Recruiters")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    // Constructor injection
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/auth/register
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Register a new user account.
     *
     * Request body (JSON):
     * {
     *   "name": "Alice Fernandes",
     *   "email": "alice@example.com",
     *   "password": "mypassword",
     *   "role": "CANDIDATE"
     * }
     *
     * Success response (200):
     * {
     *   "userId": 4,
     *   "name": "Alice Fernandes",
     *   "email": "alice@example.com",
     *   "role": "CANDIDATE",
     *   "token": "550e8400-e29b-41d4-a716-446655440000",
     *   "message": "Registration successful! Welcome to CareerHub."
     * }
     *
     * Failure response (400):
     * {
     *   "error": "An account with this email already exists."
     * }
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {

        // StringBuilder collects the error message from the service
        StringBuilder errorMsg = new StringBuilder();
        AuthResponse response = authService.register(request, errorMsg);

        if (response == null) {
            // Return 400 Bad Request with the error message
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(errorBody(errorMsg.toString()));
        }

        // Return 200 OK with AuthResponse
        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/auth/login
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Login an existing user.
     *
     * Request body (JSON):
     * {
     *   "email": "alice@example.com",
     *   "password": "mypassword"
     * }
     *
     * Success response (200):
     * {
     *   "userId": 3,
     *   "name": "Alice Fernandes",
     *   "email": "alice@example.com",
     *   "role": "CANDIDATE",
     *   "token": "some-uuid-token",
     *   "message": "Login successful! Welcome back, Alice Fernandes."
     * }
     *
     * Failure response (401):
     * {
     *   "error": "Incorrect password."
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        StringBuilder errorMsg = new StringBuilder();
        AuthResponse response = authService.login(request, errorMsg);

        if (response == null) {
            String err = errorMsg.toString();
            HttpStatus status = err.contains("deactivated") ? HttpStatus.FORBIDDEN : HttpStatus.UNAUTHORIZED;
            return ResponseEntity
                    .status(status)
                    .body(errorBody(err));
        }

        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/auth/me  (alias: /api/auth/validate)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Get current authenticated user profile using token.
     * Accepts header Authorization: Bearer <token> or ?token=<token>.
     */
    @GetMapping({"/me", "/validate"})
    public ResponseEntity<?> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                            @RequestParam(required = false) String token) {
        String effectiveToken = (authHeader != null && !authHeader.isBlank()) ? authHeader : token;
        if (effectiveToken == null || effectiveToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(errorBody("Missing authentication token. Please provide Authorization header."));
        }

        com.hiresphere.model.User user = authService.getUserByToken(effectiveToken);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(errorBody("Invalid or expired session token. Please log in again."));
        }

        if (!user.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(errorBody("Your account has been deactivated. Please contact admin."));
        }

        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("userId", user.getId());
        profile.put("name", user.getName());
        profile.put("email", user.getEmail());
        profile.put("role", user.getRole());
        profile.put("active", user.isActive());
        return ResponseEntity.ok(profile);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/auth/logout
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Logout and invalidate session token.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                    @RequestParam(required = false) String token) {
        String effectiveToken = (authHeader != null && !authHeader.isBlank()) ? authHeader : token;
        boolean loggedOut = authService.logout(effectiveToken);

        Map<String, Object> response = new LinkedHashMap<>();
        if (loggedOut) {
            response.put("message", "Logged out successfully.");
        } else {
            response.put("message", "Session was not active or token was invalid.");
        }
        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Builds a simple JSON error body: { "error": "message" }
     * Uses LinkedHashMap to preserve insertion order in JSON output.
     */
    private Map<String, String> errorBody(String message) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("error", message);
        return body;
    }
}
