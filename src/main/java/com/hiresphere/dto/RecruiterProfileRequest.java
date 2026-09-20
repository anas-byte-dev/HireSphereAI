package com.hiresphere.dto;

/**
 * RecruiterProfileRequest - Data sent by the client to create/update a recruiter profile.
 *
 * Used for: PUT /api/recruiters/{userId}/profile
 *
 * Example JSON:
 * {
 *   "designation": "HR Manager",
 *   "phone": "+91-9123456789",
 *   "linkedIn": "https://linkedin.com/in/priya-recruiter"
 * }
 */
public class RecruiterProfileRequest {

    private String designation;  // e.g., "HR Manager", "Technical Recruiter"
    private String phone;
    private String linkedIn;

    // ─── Constructors ─────────────────────────────────────────────────────────

    public RecruiterProfileRequest() {}

    public RecruiterProfileRequest(String designation, String phone, String linkedIn) {
        this.designation = designation;
        this.phone = phone;
        this.linkedIn = linkedIn;
    }

    // ─── Getters and Setters ──────────────────────────────────────────────────

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getLinkedIn() { return linkedIn; }
    public void setLinkedIn(String linkedIn) { this.linkedIn = linkedIn; }
}
