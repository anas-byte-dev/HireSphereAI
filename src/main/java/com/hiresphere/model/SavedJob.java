package com.hiresphere.model;

import java.time.LocalDate;

/**
 * SavedJob - Represents a job bookmarked/saved by a candidate.
 *
 * Very simple model — just tracks which candidate saved which job, and when.
 * Links:
 *   candidateId → User.id (CANDIDATE role)
 *   jobId       → Job.id
 */
public class SavedJob {

    private int id;
    private int candidateId;   // Who saved the job
    private int jobId;         // Which job was saved
    private LocalDate savedDate;

    // Enriched fields for easy display on candidate dashboards
    private String jobTitle;
    private String company;
    private String location;
    private String salaryRange;
    private String employmentType;
    private Job job;

    // ─── Constructors ─────────────────────────────────────────────────────────

    public SavedJob() {
        this.savedDate = LocalDate.now();
    }

    public SavedJob(int candidateId, int jobId) {
        this.candidateId = candidateId;
        this.jobId = jobId;
        this.savedDate = LocalDate.now();
    }

    // ─── Getters and Setters ──────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCandidateId() { return candidateId; }
    public void setCandidateId(int candidateId) { this.candidateId = candidateId; }

    public int getJobId() { return jobId; }
    public void setJobId(int jobId) { this.jobId = jobId; }

    public LocalDate getSavedDate() { return savedDate; }
    public void setSavedDate(LocalDate savedDate) { this.savedDate = savedDate; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getSalaryRange() { return salaryRange; }
    public void setSalaryRange(String salaryRange) { this.salaryRange = salaryRange; }

    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }

    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; }
}
