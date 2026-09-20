package com.hiresphere.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * SkillMatchRequest - DTO for direct skill comparison without requiring stored IDs.
 */
public class SkillMatchRequest {

    private List<String> candidateSkills = new ArrayList<>();
    private List<String> jobSkills = new ArrayList<>();

    public SkillMatchRequest() {}

    public SkillMatchRequest(List<String> candidateSkills, List<String> jobSkills) {
        this.candidateSkills = candidateSkills;
        this.jobSkills = jobSkills;
    }

    public List<String> getCandidateSkills() {
        return candidateSkills;
    }

    public void setCandidateSkills(List<String> candidateSkills) {
        this.candidateSkills = candidateSkills != null ? candidateSkills : new ArrayList<>();
    }

    public List<String> getJobSkills() {
        return jobSkills;
    }

    public void setJobSkills(List<String> jobSkills) {
        this.jobSkills = jobSkills != null ? jobSkills : new ArrayList<>();
    }
}
