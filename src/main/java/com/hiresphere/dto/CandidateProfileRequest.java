package com.hiresphere.dto;

import java.util.List;

/**
 * CandidateProfileRequest - Data sent by the client when creating/updating a profile.
 *
 * Used for both:
 *   POST /api/candidates/profile  → create profile
 *   PUT  /api/candidates/profile  → update profile
 *
 * All fields are optional on update — only non-null values will be applied.
 *
 * Example JSON body:
 * {
 *   "headline": "Java Developer | 2025 Fresher",
 *   "bio": "Passionate about backend development",
 *   "phone": "+91-9123456789",
 *   "location": "Mumbai, India",
 *   "education": "B.Tech CSE - Mumbai University (2025)",
 *   "experience": "Fresher",
 *   "resumeUrl": "https://drive.google.com/my-resume",
 *   "githubUrl": "https://github.com/alice",
 *   "linkedInUrl": "https://linkedin.com/in/alice",
 *   "skills": ["Java", "Spring Boot", "React", "Git"]
 * }
 */
public class CandidateProfileRequest {

    private String headline;
    private String bio;
    private String phone;
    private String location;
    private String education;
    private String experience;
    private String resumeUrl;
    private String githubUrl;
    private String linkedInUrl;
    private List<String> skills;

    // ─── Constructors ─────────────────────────────────────────────────────────

    public CandidateProfileRequest() {}

    // ─── Getters and Setters ──────────────────────────────────────────────────

    public String getHeadline() { return headline; }
    public void setHeadline(String headline) { this.headline = headline; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public String getResumeUrl() { return resumeUrl; }
    public void setResumeUrl(String resumeUrl) { this.resumeUrl = resumeUrl; }

    public String getGithubUrl() { return githubUrl; }
    public void setGithubUrl(String githubUrl) { this.githubUrl = githubUrl; }

    public String getLinkedInUrl() { return linkedInUrl; }
    public void setLinkedInUrl(String linkedInUrl) { this.linkedInUrl = linkedInUrl; }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }
}
