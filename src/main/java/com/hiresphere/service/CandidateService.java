package com.hiresphere.service;

import com.hiresphere.dto.CandidateProfileRequest;
import com.hiresphere.model.CandidateProfile;
import com.hiresphere.model.User;
import com.hiresphere.store.DataStore;
import org.springframework.stereotype.Service;

/**
 * CandidateService - Business logic for Candidate Profile operations.
 *
 * Handles:
 *   1. getProfile(userId)           → fetch candidate profile by userId
 *   2. saveProfile(userId, request) → create OR update a candidate profile
 *
 * How create vs update works:
 *   We look up the profile by userId in DataStore.
 *   If found  → update the existing object in-place (no new entry added).
 *   If not found → create a new CandidateProfile and add it to DataStore.
 *
 * This is called an "upsert" (update + insert) pattern.
 * It is the simplest practical approach for a fresher-level project.
 */
@Service
public class CandidateService {

    private final DataStore dataStore;

    // Spring injects the shared DataStore singleton
    public CandidateService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET PROFILE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the CandidateProfile for the given userId.
     *
     * @param userId  The User.id of the candidate
     * @param errorMsg  Collects error message if something goes wrong
     * @return CandidateProfile, or null on failure
     */
    public CandidateProfile getProfile(int userId, StringBuilder errorMsg) {

        // Step 1: Make sure this userId belongs to an active CANDIDATE
        User user = dataStore.findUserById(userId);
        if (user == null) {
            errorMsg.append("User not found with id: " + userId);
            return null;
        }
        if (!user.getRole().equals("CANDIDATE")) {
            errorMsg.append("User is not a CANDIDATE.");
            return null;
        }

        // Step 2: Find the profile linked to this userId
        CandidateProfile profile = dataStore.findCandidateProfileByUserId(userId);
        if (profile == null) {
            errorMsg.append("Profile not found. Please create your profile first.");
            return null;
        }

        return profile;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SAVE PROFILE (Create or Update)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates or updates the CandidateProfile for the given userId.
     *
     * Logic (Upsert):
     *   - If profile exists → update only the fields that are provided (not null)
     *   - If profile does not exist → create a brand-new profile
     *
     * @param userId   The User.id from the URL path
     * @param request  The new profile data from the request body
     * @param errorMsg Collects error if user is invalid
     * @return Updated or created CandidateProfile, or null on failure
     */
    public CandidateProfile saveProfile(int userId, CandidateProfileRequest request,
                                        StringBuilder errorMsg) {

        // Step 1: Validate user exists and is a CANDIDATE
        User user = dataStore.findUserById(userId);
        if (user == null) {
            errorMsg.append("User not found with id: " + userId);
            return null;
        }
        if (!user.getRole().equals("CANDIDATE")) {
            errorMsg.append("Only CANDIDATE users can have a candidate profile.");
            return null;
        }

        // Step 2: Look for existing profile
        CandidateProfile profile = dataStore.findCandidateProfileByUserId(userId);

        if (profile == null) {
            // ── CREATE: no profile exists yet → make a new one ──
            profile = new CandidateProfile();
            profile.setUserId(userId);
            applyFields(profile, request);
            dataStore.addCandidateProfile(profile);
            System.out.println("[CandidateService] Profile CREATED for userId=" + userId);

        } else {
            // ── UPDATE: profile already exists → update it in-place ──
            // We only overwrite fields that are present in the request (not null)
            applyFields(profile, request);
            System.out.println("[CandidateService] Profile UPDATED for userId=" + userId);
        }

        return profile;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private Helper
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Copies non-null fields from the request DTO into the profile object.
     *
     * Why check for null?
     *   A PATCH-style update should not clear existing data when a field
     *   is not included in the request. Only explicitly sent fields are updated.
     */
    private void applyFields(CandidateProfile profile, CandidateProfileRequest request) {
        if (request.getHeadline()    != null) profile.setHeadline(request.getHeadline());
        if (request.getBio()         != null) profile.setBio(request.getBio());
        if (request.getPhone()       != null) profile.setPhone(request.getPhone());
        if (request.getLocation()    != null) profile.setLocation(request.getLocation());
        if (request.getEducation()   != null) profile.setEducation(request.getEducation());
        if (request.getExperience()  != null) profile.setExperience(request.getExperience());
        if (request.getResumeUrl()   != null) profile.setResumeUrl(request.getResumeUrl());
        if (request.getGithubUrl()   != null) profile.setGithubUrl(request.getGithubUrl());
        if (request.getLinkedInUrl() != null) profile.setLinkedInUrl(request.getLinkedInUrl());
        if (request.getSkills()      != null) profile.setSkills(request.getSkills());
    }
}
