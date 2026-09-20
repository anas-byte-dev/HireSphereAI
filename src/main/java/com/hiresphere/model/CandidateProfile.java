package com.hiresphere.model;

import java.util.ArrayList;
import java.util.List;

/**
 * CandidateProfile - Stores professional details for a CANDIDATE user.
 *
 * Linked to User via userId (like a foreign key, but in plain Java).
 * One User → one CandidateProfile.
 *
 * Skills are stored as a simple List<String> — e.g., ["Java", "React", "SQL"]
 */
public class CandidateProfile {

    private int id;
    private int userId;             // Links to User.id
    private String headline;        // e.g., "Java Developer | 2025 Graduate"
    private String bio;             // Short summary about the candidate
    private String phone;
    private String location;        // e.g., "Mumbai, India"
    private String education;       // e.g., "B.Tech CSE - Mumbai University (2025)"
    private String experience;      // e.g., "Fresher" or "1 year at XYZ Corp"
    private String resumeUrl;       // Link to resume (file path or URL)
    private String githubUrl;       // e.g., "https://github.com/alice"
    private String linkedInUrl;     // e.g., "https://linkedin.com/in/alice"
    private List<String> skills;    // e.g., ["Java", "Spring Boot", "React"]

    // ─── Constructors ─────────────────────────────────────────────────────────

    public CandidateProfile() {
        this.skills = new ArrayList<>();
    }

    public CandidateProfile(int userId, String headline, String location, String education) {
        this.userId = userId;
        this.headline = headline;
        this.location = location;
        this.education = education;
        this.skills = new ArrayList<>();
    }

    // ─── Getters and Setters ──────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

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
