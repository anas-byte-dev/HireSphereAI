package com.hiresphere.model;

import java.time.LocalDateTime;

/**
 * Interview - Represents an interview scheduled by a recruiter for a candidate.
 *
 * Links:
 *   applicationId → Application.id
 *   candidateId   → User.id (CANDIDATE)
 *   recruiterId   → User.id (RECRUITER)
 *
 * Mode: "ONLINE" (Google Meet / Zoom) or "OFFLINE" (in-person)
 * Status: "SCHEDULED", "COMPLETED", "CANCELLED"
 */
public class Interview {

    private int id;
    private int applicationId;     // Links to Application.id
    private int candidateId;       // Who is being interviewed
    private int recruiterId;       // Who is conducting the interview
    private int jobId;             // Which job this interview is for

    // Primary interview schedule fields
    private String date;           // e.g., "2026-09-25"
    private String time;           // e.g., "11:00 AM" or "14:30"
    private String mode;           // "ONLINE" or "OFFLINE"
    private String meetingLink;    // Video call link or physical venue address
    private String status;         // "SCHEDULED", "COMPLETED", "CANCELLED"
    private String notes;          // Any instructions from recruiter

    // Enriched metadata for easy display in candidate & recruiter dashboards
    private String jobTitle;
    private String company;
    private String candidateName;
    private String candidateEmail;
    private String recruiterName;

    // Backward-compatible fields
    private LocalDateTime scheduledAt;
    private String location;

    // ─── Constructors ─────────────────────────────────────────────────────────

    public Interview() {
        this.status = "SCHEDULED";
    }

    public Interview(int applicationId, int candidateId, int recruiterId,
                     int jobId, LocalDateTime scheduledAt, String mode) {
        this.applicationId = applicationId;
        this.candidateId = candidateId;
        this.recruiterId = recruiterId;
        this.jobId = jobId;
        this.scheduledAt = scheduledAt;
        this.mode = mode;
        this.status = "SCHEDULED";
        if (scheduledAt != null) {
            this.date = scheduledAt.toLocalDate().toString();
            this.time = scheduledAt.toLocalTime().toString();
        }
    }

    // ─── Getters and Setters ──────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getApplicationId() { return applicationId; }
    public void setApplicationId(int applicationId) { this.applicationId = applicationId; }

    public int getCandidateId() { return candidateId; }
    public void setCandidateId(int candidateId) { this.candidateId = candidateId; }

    public int getRecruiterId() { return recruiterId; }
    public void setRecruiterId(int recruiterId) { this.recruiterId = recruiterId; }

    public int getJobId() { return jobId; }
    public void setJobId(int jobId) { this.jobId = jobId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getMeetingLink() {
        return meetingLink != null ? meetingLink : location;
    }
    public void setMeetingLink(String meetingLink) {
        this.meetingLink = meetingLink;
        if (this.location == null) {
            this.location = meetingLink;
        }
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }

    public String getCandidateEmail() { return candidateEmail; }
    public void setCandidateEmail(String candidateEmail) { this.candidateEmail = candidateEmail; }

    public String getRecruiterName() { return recruiterName; }
    public void setRecruiterName(String recruiterName) { this.recruiterName = recruiterName; }

    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
        if (scheduledAt != null) {
            if (this.date == null) this.date = scheduledAt.toLocalDate().toString();
            if (this.time == null) this.time = scheduledAt.toLocalTime().toString();
        }
    }

    public String getLocation() {
        return location != null ? location : meetingLink;
    }
    public void setLocation(String location) {
        this.location = location;
        if (this.meetingLink == null) {
            this.meetingLink = location;
        }
    }
}
