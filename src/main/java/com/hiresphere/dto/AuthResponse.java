package com.hiresphere.dto;

/**
 * AuthResponse - Data sent back to the client after a successful login or register.
 *
 * Contains:
 *   - Basic user info (id, name, email, role)
 *   - A simple session token (UUID string)
 *
 * What is a token?
 *   A token is a unique random string we generate when a user logs in.
 *   The frontend stores this token (e.g., in localStorage) and sends it
 *   with future requests so the backend knows who is making the request.
 *
 *   We use UUID.randomUUID().toString() — simple, no dependencies.
 *   Example token: "550e8400-e29b-41d4-a716-446655440000"
 *
 *   This is NOT JWT. It is a simpler session-based token approach
 *   that is easy to understand and explain in an interview.
 */
public class AuthResponse {

    private int userId;
    private String name;
    private String email;
    private String role;
    private String token;   // UUID session token — store in localStorage on frontend
    private String message; // Success message, e.g., "Login successful"

    // ─── Constructors ─────────────────────────────────────────────────────────

    public AuthResponse() {}

    public AuthResponse(int userId, String name, String email, String role,
                        String token, String message) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.token = token;
        this.message = message;
    }

    // ─── Getters and Setters ──────────────────────────────────────────────────

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
