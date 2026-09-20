package com.hiresphere.model;

/**
 * User - Represents a registered user in CareerHub.
 *
 * This is a plain Java class (POJO - Plain Old Java Object).
 * No @Entity, no JPA, no database. Just simple fields + getters/setters.
 *
 * Roles: CANDIDATE, RECRUITER, ADMIN
 */
public class User {

    private int id;
    private String name;
    private String email;
    private String password;  // Stored as plain text for simplicity (not for production!)
    private String role;      // "CANDIDATE", "RECRUITER", or "ADMIN"
    private boolean active;   // Admin can activate/deactivate a user

    // ─── Constructors ─────────────────────────────────────────────────────────

    public User() {
        this.active = true; // New users are active by default
    }

    public User(String name, String email, String password, String role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.active = true;
    }

    public User(int id, String name, String email, String password, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.active = true;
    }

    // ─── Getters and Setters ──────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + name + "', email='" + email + "', role='" + role + "', active=" + active + "}";
    }
}
