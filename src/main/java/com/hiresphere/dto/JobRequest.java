package com.hiresphere.dto;

import java.util.List;

/**
 * JobRequest - Data transfer object for creating or updating a job posting.
 *
 * Supports both modern requested field names:
 * - company
 * - employmentType
 * - experience
 * - skills
 * - recruiterId
 * - active
 *
 * As well as previous aliases:
 * - jobType
 * - experienceRequired
 * - requiredSkills
 * - status
 */
public class JobRequest {

    private String title;
    private String company;
    private String location;
    private String employmentType;      // "FULL_TIME", "PART_TIME", "INTERNSHIP", "CONTRACT"
    private String salaryRange;
    private String experience;          // "Fresher", "0-2 years"
    private List<String> skills;
    private String description;
    private String requirements;
    private String deadline;            // yyyy-MM-dd
    private Integer recruiterId;
    private Boolean active;
    private String status;

    public JobRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getCompanyName() { return getCompany(); }
    public void setCompanyName(String companyName) { setCompany(companyName); }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getEmploymentType() {
        return employmentType != null ? employmentType : jobType;
    }
    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
        this.jobType = employmentType;
    }

    // Alias for employmentType
    private String jobType;
    public String getJobType() { return getEmploymentType(); }
    public void setJobType(String jobType) { setEmploymentType(jobType); }

    public String getSalaryRange() { return salaryRange; }
    public void setSalaryRange(String salaryRange) { this.salaryRange = salaryRange; }

    public String getSalary() { return getSalaryRange(); }
    public void setSalary(String salary) { setSalaryRange(salary); }

    public String getExperience() {
        return experience != null ? experience : experienceRequired;
    }
    public void setExperience(String experience) {
        this.experience = experience;
        this.experienceRequired = experience;
    }

    // Alias for experience
    private String experienceRequired;
    public String getExperienceRequired() { return getExperience(); }
    public void setExperienceRequired(String experienceRequired) { setExperience(experienceRequired); }

    public List<String> getSkills() {
        return skills != null ? skills : requiredSkills;
    }
    public void setSkills(List<String> skills) {
        this.skills = skills;
        this.requiredSkills = skills;
    }

    // Alias for skills
    private List<String> requiredSkills;
    public List<String> getRequiredSkills() { return getSkills(); }
    public void setRequiredSkills(List<String> requiredSkills) { setSkills(requiredSkills); }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRequirements() { return requirements; }
    public void setRequirements(Object requirementsObj) {
        if (requirementsObj == null) {
            this.requirements = null;
        } else if (requirementsObj instanceof List<?> list) {
            this.requirements = String.join(", ", list.stream().map(Object::toString).toList());
            if (this.skills == null || this.skills.isEmpty()) {
                this.skills = list.stream().map(Object::toString).toList();
            }
        } else {
            this.requirements = requirementsObj.toString();
        }
    }

    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }

    public Integer getRecruiterId() { return recruiterId; }
    public void setRecruiterId(Integer recruiterId) { this.recruiterId = recruiterId; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
