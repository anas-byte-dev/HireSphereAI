package com.hiresphere.service;

import com.hiresphere.dto.SkillMatchResponse;
import com.hiresphere.model.CandidateProfile;
import com.hiresphere.model.Job;
import com.hiresphere.model.User;
import com.hiresphere.store.DataStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * SkillMatchService - Pure rule-based skill matching engine.
 *
 * Designed to be clean, simple, and intuitive for fresher interviews.
 *
 * Algorithm:
 * 1. Normalize candidate skills to lowercase for case-insensitive comparison.
 * 2. Loop through the job's required skills:
 *    - If present in candidate skills -> add to matchingSkills.
 *    - Otherwise -> add to missingSkills.
 * 3. Match percentage = (matchingSkills / totalRequiredSkills) * 100.
 *
 * NO AI, ML, or external dependencies.
 */
@Service
public class SkillMatchService {

    private final DataStore dataStore;

    public SkillMatchService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * Core matching algorithm comparing candidate skills against job required skills.
     */
    public SkillMatchResponse matchSkills(List<String> candidateSkills, List<String> jobSkills) {
        List<String> cSkills = candidateSkills != null ? candidateSkills : new ArrayList<>();
        List<String> jSkills = jobSkills != null ? jobSkills : new ArrayList<>();

        // Normalize candidate skills for case-insensitive lookup
        Set<String> normalizedCandidateSkills = new HashSet<>();
        for (String skill : cSkills) {
            if (skill != null && !skill.trim().isEmpty()) {
                normalizedCandidateSkills.add(skill.trim().toLowerCase());
            }
        }

        List<String> matchingSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();

        // Compare each required job skill
        for (String jobSkill : jSkills) {
            if (jobSkill == null || jobSkill.trim().isEmpty()) {
                continue;
            }
            String trimmedSkill = jobSkill.trim();
            if (normalizedCandidateSkills.contains(trimmedSkill.toLowerCase())) {
                matchingSkills.add(trimmedSkill);
            } else {
                missingSkills.add(trimmedSkill);
            }
        }

        // Calculate match percentage
        int totalRequired = jSkills.size();
        double matchPercentage = 0.0;

        if (totalRequired > 0) {
            double raw = ((double) matchingSkills.size() / totalRequired) * 100.0;
            matchPercentage = Math.round(raw * 10.0) / 10.0;
        } else if (cSkills.isEmpty()) {
            matchPercentage = 100.0; // No requirements, neutral match
        } else {
            matchPercentage = 100.0;
        }

        String summary = String.format("%d out of %d required skills matched (%.1f%%)",
                matchingSkills.size(), totalRequired, matchPercentage);

        return new SkillMatchResponse(
                cSkills,
                jSkills,
                matchingSkills,
                missingSkills,
                matchPercentage,
                summary
        );
    }

    /**
     * Matches a candidate's saved profile skills against a stored job's required skills.
     */
    public SkillMatchResponse matchCandidateWithJob(int candidateId, int jobId, StringBuilder errorMsg) {
        // Validate candidate user exists
        User user = dataStore.findUserById(candidateId);
        if (user == null) {
            errorMsg.append("Candidate not found with id: ").append(candidateId);
            return null;
        }

        // Validate job exists
        Job job = dataStore.findJobById(jobId);
        if (job == null) {
            errorMsg.append("Job not found with id: ").append(jobId);
            return null;
        }

        // Retrieve candidate profile skills
        List<String> candidateSkills = new ArrayList<>();
        CandidateProfile profile = dataStore.findCandidateProfileByUserId(candidateId);
        if (profile != null && profile.getSkills() != null) {
            candidateSkills = profile.getSkills();
        }

        // Calculate match
        SkillMatchResponse response = matchSkills(candidateSkills, job.getSkills());
        response.setCandidateId(candidateId);
        response.setJobId(jobId);
        response.setJobTitle(job.getTitle());

        return response;
    }
}
