package com.hiresphere.model;

import java.time.LocalDate;

/**
 * Application - Represents a job application submitted by a candidate.
 *
 * Supported Statuses:
 *   - APPLIED
 *   - SHORTLISTED
 *   - REJECTED
 *   - INTERVIEW
 *   - SELECTED
 */
public class Application {

    private int id;
    private int candidateId;        // Links to User.id (CANDIDATE role)
    private int jobId;              // Links to Job.id
    private String status;          // APPLIED, SHORTLISTED, REJECTED, INTERVIEW, SELECTED
    private String coverLetter;     // Optional message from candidate
    private String resumeUrl;       // Resume link or attachment path
    private LocalDate appliedDate;  // Date of submission

    // Enriched fields for practical display in candidate & recruiter dashboards
    private String jobTitle;
    private String company;
    private String candidateName;
    private String candidateEmail;
    private String candidatePhone;
    private Double matchScore;
    private java.util.List<String> matchingSkills;
    private java.util.List<String> missingSkills;

    // ─── Constructors ─────────────────────────────────────────────────────────

    public Application() {
        this.status = "APPLIED";
        this.appliedDate = LocalDate.now();
    }

    public Application(int candidateId, int jobId, String coverLetter) {
        this.candidateId = candidateId;
        this.jobId = jobId;
        this.coverLetter = coverLetter;
        this.status = "APPLIED";
        this.appliedDate = LocalDate.now();
    }

    // ─── Getters and Setters ──────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCandidateId() { return candidateId; }
    public void setCandidateId(int candidateId) { this.candidateId = candidateId; }

    public int getJobId() { return jobId; }
    public void setJobId(int jobId) { this.jobId = jobId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }

    public String getResumeUrl() { return resumeUrl; }
    public void setResumeUrl(String resumeUrl) { this.resumeUrl = resumeUrl; }

    public LocalDate getAppliedDate() { return appliedDate; }
    public void setAppliedDate(LocalDate appliedDate) { this.appliedDate = appliedDate; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }

    public String getCandidateEmail() { return candidateEmail; }
    public void setCandidateEmail(String candidateEmail) { this.candidateEmail = candidateEmail; }

    public String getCandidatePhone() { return candidatePhone; }
    public void setCandidatePhone(String candidatePhone) { this.candidatePhone = candidatePhone; }

    public Double getMatchScore() { return matchScore; }
    public void setMatchScore(Double matchScore) { this.matchScore = matchScore; }

    public java.util.List<String> getMatchingSkills() { return matchingSkills; }
    public void setMatchingSkills(java.util.List<String> matchingSkills) { this.matchingSkills = matchingSkills; }

    public java.util.List<String> getMissingSkills() { return missingSkills; }
    public void setMissingSkills(java.util.List<String> missingSkills) { this.missingSkills = missingSkills; }
}
