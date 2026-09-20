package com.hiresphere.model;

/**
 * RecruiterProfile - Stores professional details for a RECRUITER user.
 *
 * Linked to User via userId.
 * Also linked to a Company via companyId.
 * One User → one RecruiterProfile → one Company.
 */
public class RecruiterProfile {

    private int id;
    private int userId;         // Links to User.id
    private int companyId;      // Links to Company.id
    private String designation; // e.g., "HR Manager", "Technical Recruiter"
    private String phone;
    private String linkedIn;    // LinkedIn profile URL

    // ─── Constructors ─────────────────────────────────────────────────────────

    public RecruiterProfile() {}

    public RecruiterProfile(int userId, int companyId, String designation) {
        this.userId = userId;
        this.companyId = companyId;
        this.designation = designation;
    }

    // ─── Getters and Setters ──────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getCompanyId() { return companyId; }
    public void setCompanyId(int companyId) { this.companyId = companyId; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getLinkedIn() { return linkedIn; }
    public void setLinkedIn(String linkedIn) { this.linkedIn = linkedIn; }
}
