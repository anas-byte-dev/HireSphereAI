package com.hiresphere.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Job - Represents a job posting created by a recruiter.
 *
 * Practical fields:
 * - id: unique job identifier
 * - title: job title
 * - company: company name
 * - location: job location (e.g. "Bangalore, India" or "Remote")
 * - employmentType: "FULL_TIME", "PART_TIME", "INTERNSHIP", "CONTRACT"
 * - salaryRange: e.g., "₹4-6 LPA"
 * - experience: e.g., "Fresher", "0-2 years"
 * - skills: skills list, e.g. ["Java", "Spring Boot", "REST API"]
 * - description: full job description
 * - requirements: job requirements and qualifications
 * - postedDate: date job was posted
 * - deadline: application deadline
 * - recruiterId: links to User.id (recruiter who created this posting)
 * - active: active status (true/false)
 */
public class Job {

    private int id;
    private String title;
    private String company;                 // e.g. "TechCorp Solutions"
    private int companyId;                  // Links to Company.id (if configured)
    private String location;                // e.g., "Pune, India" or "Remote"
    private String employmentType;          // "FULL_TIME", "PART_TIME", "INTERNSHIP", "CONTRACT"
    private String salaryRange;             // e.g., "₹4-6 LPA"
    private String experience;              // e.g., "Fresher", "0-2 years"
    private List<String> skills;            // e.g., ["Java", "Spring Boot", "REST API"]
    private String description;             // Full job description
    private String requirements;            // Job requirements / qualifications
    private LocalDate postedDate;           // Date job was posted
    private LocalDate deadline;             // Application deadline
    private int recruiterId;                // Links to User.id (RECRUITER role)
    private boolean active = true;          // Active status
    private String status = "OPEN";         // "OPEN" or "CLOSED" (synced with active)

    // ─── Constructors ─────────────────────────────────────────────────────────

    public Job() {
        this.skills = new ArrayList<>();
        this.active = true;
        this.status = "OPEN";
        this.postedDate = LocalDate.now();
    }

    public Job(int recruiterId, int companyId, String title, String location, String employmentType) {
        this.recruiterId = recruiterId;
        this.companyId = companyId;
        this.title = title;
        this.location = location;
        this.employmentType = employmentType;
        this.skills = new ArrayList<>();
        this.active = true;
        this.status = "OPEN";
        this.postedDate = LocalDate.now();
    }

    // ─── Getters and Setters ──────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public int getCompanyId() { return companyId; }
    public void setCompanyId(int companyId) { this.companyId = companyId; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }

    // Backward-compatibility alias for employmentType
    public String getJobType() { return getEmploymentType(); }
    public void setJobType(String jobType) { setEmploymentType(jobType); }

    public String getSalaryRange() { return salaryRange; }
    public void setSalaryRange(String salaryRange) { this.salaryRange = salaryRange; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    // Backward-compatibility alias for experience
    public String getExperienceRequired() { return getExperience(); }
    public void setExperienceRequired(String experienceRequired) { setExperience(experienceRequired); }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills != null ? skills : new ArrayList<>(); }

    // Backward-compatibility alias for skills
    public List<String> getRequiredSkills() { return getSkills(); }
    public void setRequiredSkills(List<String> requiredSkills) { setSkills(requiredSkills); }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }

    public LocalDate getPostedDate() { return postedDate; }
    public void setPostedDate(LocalDate postedDate) { this.postedDate = postedDate; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public int getRecruiterId() { return recruiterId; }
    public void setRecruiterId(int recruiterId) { this.recruiterId = recruiterId; }

    public boolean isActive() { return active; }
    public boolean getActive() { return active; }
    public void setActive(boolean active) {
        this.active = active;
        this.status = active ? "OPEN" : "CLOSED";
    }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        this.active = "OPEN".equalsIgnoreCase(status) || "ACTIVE".equalsIgnoreCase(status);
    }
}
