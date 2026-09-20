package com.hiresphere.service;

import com.hiresphere.dto.CompanyRequest;
import com.hiresphere.dto.RecruiterProfileRequest;
import com.hiresphere.model.Company;
import com.hiresphere.model.RecruiterProfile;
import com.hiresphere.model.User;
import com.hiresphere.store.DataStore;
import org.springframework.stereotype.Service;

/**
 * RecruiterService - Business logic for Recruiter Profile and Company operations.
 *
 * Handles:
 *   1. getProfile(userId)                     → fetch recruiter profile
 *   2. saveProfile(userId, request)            → create or update recruiter profile
 *   3. getCompany(userId)                      → fetch company linked to recruiter
 *   4. saveCompany(userId, request)            → create or update company info
 *
 * Key relationship:
 *   User (RECRUITER) → RecruiterProfile → Company
 *   RecruiterProfile.companyId links to Company.id
 *
 * Upsert pattern (same as CandidateService):
 *   - If object exists → update it in-place
 *   - If not found    → create a new one
 */
@Service
public class RecruiterService {

    private final DataStore dataStore;

    public RecruiterService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET RECRUITER PROFILE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the RecruiterProfile for the given userId.
     */
    public RecruiterProfile getProfile(int userId, StringBuilder errorMsg) {

        // Validate user is a RECRUITER
        User user = dataStore.findUserById(userId);
        if (user == null) {
            errorMsg.append("User not found with id: " + userId);
            return null;
        }
        if (!user.getRole().equals("RECRUITER")) {
            errorMsg.append("User is not a RECRUITER.");
            return null;
        }

        RecruiterProfile profile = dataStore.findRecruiterProfileByUserId(userId);
        if (profile == null) {
            errorMsg.append("Recruiter profile not found. Please complete your profile first.");
            return null;
        }

        return profile;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SAVE RECRUITER PROFILE (Create or Update)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates or updates the RecruiterProfile for the given userId.
     *
     * If the recruiter doesn't have a company yet, we also create a blank
     * Company and link it to the profile.
     */
    public RecruiterProfile saveProfile(int userId, RecruiterProfileRequest request,
                                        StringBuilder errorMsg) {

        User user = dataStore.findUserById(userId);
        if (user == null) {
            errorMsg.append("User not found with id: " + userId);
            return null;
        }
        if (!user.getRole().equals("RECRUITER")) {
            errorMsg.append("Only RECRUITER users can have a recruiter profile.");
            return null;
        }

        RecruiterProfile profile = dataStore.findRecruiterProfileByUserId(userId);

        if (profile == null) {
            // ── CREATE: no profile yet — create one with a default Company ──
            // First create a blank company so we have a companyId to link
            Company company = new Company();
            company.setName(user.getName() + "'s Company");   // default name
            company.setLocation("Not specified");
            dataStore.addCompany(company);

            profile = new RecruiterProfile();
            profile.setUserId(userId);
            profile.setCompanyId(company.getId());
            applyProfileFields(profile, request);
            dataStore.addRecruiterProfile(profile);

            System.out.println("[RecruiterService] Profile CREATED for userId=" + userId
                    + " | companyId=" + company.getId());
        } else {
            // ── UPDATE: profile exists — update fields ──
            applyProfileFields(profile, request);
            System.out.println("[RecruiterService] Profile UPDATED for userId=" + userId);
        }

        return profile;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET COMPANY
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the Company linked to the recruiter's profile.
     *
     * Flow:
     *   userId → RecruiterProfile.companyId → Company
     */
    public Company getCompany(int userId, StringBuilder errorMsg) {

        User user = dataStore.findUserById(userId);
        if (user == null) {
            errorMsg.append("User not found with id: " + userId);
            return null;
        }
        if (!user.getRole().equals("RECRUITER")) {
            errorMsg.append("Only RECRUITER users can access company info.");
            return null;
        }

        RecruiterProfile profile = dataStore.findRecruiterProfileByUserId(userId);
        if (profile == null) {
            errorMsg.append("Recruiter profile not found. Please create your profile first.");
            return null;
        }

        Company company = dataStore.findCompanyById(profile.getCompanyId());
        if (company == null) {
            errorMsg.append("Company not found. Please update your company information.");
            return null;
        }

        return company;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SAVE COMPANY (Create or Update)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates or updates the Company linked to this recruiter.
     *
     * If recruiter has no profile yet → we create the profile first,
     * then update the company. This way the recruiter never gets stuck.
     *
     * If profile exists → find the linked company and update it.
     */
    public Company saveCompany(int userId, CompanyRequest request, StringBuilder errorMsg) {

        User user = dataStore.findUserById(userId);
        if (user == null) {
            errorMsg.append("User not found with id: " + userId);
            return null;
        }
        if (!user.getRole().equals("RECRUITER")) {
            errorMsg.append("Only RECRUITER users can manage company information.");
            return null;
        }

        RecruiterProfile profile = dataStore.findRecruiterProfileByUserId(userId);
        Company company;

        if (profile == null) {
            // No profile yet → create both profile and company together
            company = new Company();
            dataStore.addCompany(company);

            profile = new RecruiterProfile();
            profile.setUserId(userId);
            profile.setCompanyId(company.getId());
            dataStore.addRecruiterProfile(profile);

            System.out.println("[RecruiterService] Profile + Company AUTO-CREATED for userId=" + userId);
        } else {
            // Profile exists → get the linked company
            company = dataStore.findCompanyById(profile.getCompanyId());

            if (company == null) {
                // Edge case: profile exists but company was lost — recreate it
                company = new Company();
                dataStore.addCompany(company);
                profile.setCompanyId(company.getId());
                System.out.println("[RecruiterService] Company RE-CREATED for userId=" + userId);
            }
        }

        // Apply all company fields from request (non-null only)
        applyCompanyFields(company, request);

        System.out.println("[RecruiterService] Company SAVED: " + company.getName()
                + " | companyId=" + company.getId());

        return company;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Copies non-null fields from RecruiterProfileRequest into the profile object.
     */
    private void applyProfileFields(RecruiterProfile profile, RecruiterProfileRequest request) {
        if (request.getDesignation() != null) profile.setDesignation(request.getDesignation());
        if (request.getPhone()       != null) profile.setPhone(request.getPhone());
        if (request.getLinkedIn()    != null) profile.setLinkedIn(request.getLinkedIn());
    }

    /**
     * Copies non-null / non-zero fields from CompanyRequest into the company object.
     * For int fields (size), 0 is treated as "not provided" — skip update.
     */
    private void applyCompanyFields(Company company, CompanyRequest request) {
        if (request.getName()         != null) company.setName(request.getName());
        if (request.getDescription()  != null) company.setDescription(request.getDescription());
        if (request.getWebsite()      != null) company.setWebsite(request.getWebsite());
        if (request.getLocation()     != null) company.setLocation(request.getLocation());
        if (request.getIndustry()     != null) company.setIndustry(request.getIndustry());
        if (request.getSize()         > 0)     company.setSize(request.getSize());
        if (request.getContactEmail() != null) company.setContactEmail(request.getContactEmail());
        if (request.getContactPhone() != null) company.setContactPhone(request.getContactPhone());
    }
}
