package com.hiresphere.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * SkillMatchResponse - DTO returned by the Skill Matching API.
 *
 * Contains:
 *   - candidate skills
 *   - required job skills
 *   - matching skills
 *   - match percentage
 *   - missing skills
 *   - human-readable match summary
 */
public class SkillMatchResponse {

    private Integer candidateId;
    private Integer jobId;
    private String jobTitle;
    private List<String> candidateSkills = new ArrayList<>();
    private List<String> requiredJobSkills = new ArrayList<>();
    private List<String> matchingSkills = new ArrayList<>();
    private List<String> missingSkills = new ArrayList<>();
    private double matchPercentage;
    private String matchSummary;

    public SkillMatchResponse() {}

    public SkillMatchResponse(List<String> candidateSkills,
                              List<String> requiredJobSkills,
                              List<String> matchingSkills,
                              List<String> missingSkills,
                              double matchPercentage,
                              String matchSummary) {
        this.candidateSkills = candidateSkills;
        this.requiredJobSkills = requiredJobSkills;
        this.matchingSkills = matchingSkills;
        this.missingSkills = missingSkills;
        this.matchPercentage = matchPercentage;
        this.matchSummary = matchSummary;
    }

    public Integer getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Integer candidateId) {
        this.candidateId = candidateId;
    }

    public Integer getJobId() {
        return jobId;
    }

    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public List<String> getCandidateSkills() {
        return candidateSkills;
    }

    public void setCandidateSkills(List<String> candidateSkills) {
        this.candidateSkills = candidateSkills != null ? candidateSkills : new ArrayList<>();
    }

    public List<String> getRequiredJobSkills() {
        return requiredJobSkills;
    }

    public void setRequiredJobSkills(List<String> requiredJobSkills) {
        this.requiredJobSkills = requiredJobSkills != null ? requiredJobSkills : new ArrayList<>();
    }

    public List<String> getMatchingSkills() {
        return matchingSkills;
    }

    public void setMatchingSkills(List<String> matchingSkills) {
        this.matchingSkills = matchingSkills != null ? matchingSkills : new ArrayList<>();
    }

    public List<String> getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(List<String> missingSkills) {
        this.missingSkills = missingSkills != null ? missingSkills : new ArrayList<>();
    }

    public double getMatchPercentage() {
        return matchPercentage;
    }

    public void setMatchPercentage(double matchPercentage) {
        this.matchPercentage = matchPercentage;
    }

    public String getMatchSummary() {
        return matchSummary;
    }

    public void setMatchSummary(String matchSummary) {
        this.matchSummary = matchSummary;
    }
}
