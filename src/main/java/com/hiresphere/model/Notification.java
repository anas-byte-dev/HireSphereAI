package com.hiresphere.model;

import java.time.LocalDateTime;

/**
 * Notification - Represents a notification sent to a user.
 *
 * Notifications are created by:
 *  - Recruiters: when they update application status or schedule interviews
 *  - System: when a job matches a candidate's skills
 *
 * Links:
 *   userId → User.id (who receives this notification)
 *
 * type examples: "APPLICATION_UPDATE", "INTERVIEW_SCHEDULED", "JOB_MATCH"
 */
public class Notification {

    private int id;
    private int userId;         // Who receives this notification
    private String type;        // Type of notification
    private String message;     // Human-readable message shown to user
    private String date;        // Date of notification (e.g. "2026-09-18")
    private boolean isRead;     // Has the user seen this notification?
    private LocalDateTime createdAt;

    // ─── Constructors ─────────────────────────────────────────────────────────

    public Notification() {
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
        this.date = java.time.LocalDate.now().toString();
    }

    public Notification(int userId, String type, String message) {
        this.userId = userId;
        this.type = type;
        this.message = message;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
        this.date = java.time.LocalDate.now().toString();
    }

    // ─── Getters and Setters ──────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public boolean isRead() { return isRead; }
    public boolean getRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        if (createdAt != null && this.date == null) {
            this.date = createdAt.toLocalDate().toString();
        }
    }
}
