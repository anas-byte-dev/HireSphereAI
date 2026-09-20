package com.hiresphere.dto;

/**
 * LoginRequest - Data sent by the client when logging in.
 *
 * Example JSON body:
 * {
 *   "email": "alice@example.com",
 *   "password": "mypassword"
 * }
 */
public class LoginRequest {

    private String email;
    private String password;

    // ─── Constructors ─────────────────────────────────────────────────────────

    public LoginRequest() {}

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // ─── Getters and Setters ──────────────────────────────────────────────────

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
