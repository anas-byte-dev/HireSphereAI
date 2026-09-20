package com.hiresphere.service;

import com.hiresphere.dto.ApplicationRequest;
import com.hiresphere.model.Application;
import com.hiresphere.model.CandidateProfile;
import com.hiresphere.model.Job;
import com.hiresphere.model.User;
import com.hiresphere.store.DataStore;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ApplicationService - Business logic for Job Applications.
 *
 * Supported Statuses:
 *   APPLIED, SHORTLISTED, REJECTED, INTERVIEW, SELECTED
 */
import com.hiresphere.dto.SkillMatchResponse;

@Service
public class ApplicationService {

    private final DataStore dataStore;
    private final NotificationService notificationService;
    private final SkillMatchService skillMatchService;

    private static final Set<String> ALLOWED_STATUSES = new HashSet<>(Arrays.asList(
            "APPLIED", "REVIEWING", "SHORTLISTED", "REJECTED", "INTERVIEW", "INTERVIEW SCHEDULED", "SELECTED", "ACCEPTED"
    ));

    public ApplicationService(DataStore dataStore,
                              NotificationService notificationService,
                              SkillMatchService skillMatchService) {
        this.dataStore = dataStore;
        this.notificationService = notificationService;
        this.skillMatchService = skillMatchService;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // APPLY FOR A JOB  →  POST /api/applications
    // ─────────────────────────────────────────────────────────────────────────

    public Application applyForJob(Integer candidateId, ApplicationRequest request, StringBuilder errorMsg) {
        int effectiveCandidateId = (candidateId != null && candidateId > 0)
                ? candidateId
                : (request.getCandidateId() != null ? request.getCandidateId() : 0);

        if (effectiveCandidateId <= 0) {
            errorMsg.append("Candidate ID is required (pass via ?candidateId=X or in request body).");
            return null;
        }

        if (request.getJobId() == null || request.getJobId() <= 0) {
            errorMsg.append("Job ID is required.");
            return null;
        }

        // Validate candidate
        User candidate = dataStore.findUserById(effectiveCandidateId);
        if (candidate == null) {
            errorMsg.append("Candidate not found with id: ").append(effectiveCandidateId);
            return null;
        }
        if (!"CANDIDATE".equalsIgnoreCase(candidate.getRole())) {
            errorMsg.append("Only users with CANDIDATE role can apply for jobs.");
            return null;
        }

        // Validate job
        Job job = dataStore.findJobById(request.getJobId());
        if (job == null) {
            errorMsg.append("Job not found with id: ").append(request.getJobId());
            return null;
        }
        if (!job.isActive()) {
            errorMsg.append("Cannot apply to an inactive or closed job.");
            return null;
        }

        // Prevent duplicate applications
        if (dataStore.hasCandidateApplied(effectiveCandidateId, request.getJobId())) {
            errorMsg.append("You have already applied to this job.");
            return null;
        }

        // Enforce 70% skill match requirement
        StringBuilder matchErr = new StringBuilder();
        SkillMatchResponse match = skillMatchService.matchCandidateWithJob(effectiveCandidateId, job.getId(), matchErr);
        if (match != null && match.getRequiredJobSkills() != null && !match.getRequiredJobSkills().isEmpty()) {
            if (match.getMatchPercentage() < 70.0) {
                String missing = match.getMissingSkills() != null && !match.getMissingSkills().isEmpty()
                        ? String.join(", ", match.getMissingSkills())
                        : "Required technical skills";
                errorMsg.append(String.format("Your skill match is %.1f%%. A minimum 70%% skill match is required to apply for this position. Missing skills: %s. Please update your profile skills or browse matching roles.",
                        match.getMatchPercentage(), missing));
                return null;
            }
        }

        // Build Application
        Application application = new Application();
        application.setCandidateId(effectiveCandidateId);
        application.setJobId(job.getId());
        application.setStatus("APPLIED");
        application.setAppliedDate(LocalDate.now());
        application.setCoverLetter(request.getCoverLetter());

        if (match != null) {
            application.setMatchScore(match.getMatchPercentage());
            application.setMatchingSkills(match.getMatchingSkills());
            application.setMissingSkills(match.getMissingSkills());
        }

        // Populate job metadata
        application.setJobTitle(job.getTitle());
        application.setCompany(job.getCompany());

        // Populate candidate metadata
        application.setCandidateName(candidate.getName());
        application.setCandidateEmail(candidate.getEmail());

        CandidateProfile profile = dataStore.findCandidateProfileByUserId(effectiveCandidateId);
        if (profile != null) {
            application.setCandidatePhone(profile.getPhone());
            application.setResumeUrl(profile.getResumeUrl());
        }

        if (request.getResumeUrl() != null && !request.getResumeUrl().isBlank()) {
            application.setResumeUrl(request.getResumeUrl().trim());
        }

        dataStore.addApplication(application);

        // Notify candidate
        notificationService.createNotification(
                effectiveCandidateId,
                "APPLICATION_SUBMITTED",
                "Your application for " + job.getTitle() + " at " + job.getCompany() + " has been submitted successfully."
        );

        // Notify recruiter
        if (job.getRecruiterId() > 0) {
            notificationService.createNotification(
                    job.getRecruiterId(),
                    "NEW_APPLICANT",
                    "New application received from " + candidate.getName() + " for job: " + job.getTitle()
            );
        }

        System.out.println("[ApplicationService] Application SUBMITTED: id=" + application.getId()
                + " | candidateId=" + effectiveCandidateId + " | jobId=" + job.getId());

        return application;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CANDIDATE: VIEW APPLICATIONS  →  GET /api/applications?candidateId=X
    // ─────────────────────────────────────────────────────────────────────────

    public List<Application> getApplicationsByCandidate(int candidateId, StringBuilder errorMsg) {
        User user = dataStore.findUserById(candidateId);
        if (user == null) {
            errorMsg.append("Candidate not found with id: ").append(candidateId);
            return null;
        }
        return dataStore.findApplicationsByCandidateId(candidateId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // VIEW APPLICATION DETAILS  →  GET /api/applications/{id}
    // ─────────────────────────────────────────────────────────────────────────

    public Application getApplicationById(int applicationId, StringBuilder errorMsg) {
        Application application = dataStore.findApplicationById(applicationId);
        if (application == null) {
            errorMsg.append("Application not found with id: ").append(applicationId);
        }
        return application;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RECRUITER: VIEW APPLICANTS  →  GET /api/applications?recruiterId=X(&jobId=Y)
    // ─────────────────────────────────────────────────────────────────────────

    public List<Application> getApplicantsForRecruiter(int recruiterId, Integer jobId, StringBuilder errorMsg) {
        User recruiter = dataStore.findUserById(recruiterId);
        if (recruiter == null) {
            errorMsg.append("Recruiter not found with id: ").append(recruiterId);
            return null;
        }
        if (!"RECRUITER".equalsIgnoreCase(recruiter.getRole())) {
            errorMsg.append("Only RECRUITER users can view job applicants.");
            return null;
        }

        if (jobId != null && jobId > 0) {
            Job job = dataStore.findJobById(jobId);
            if (job == null) {
                errorMsg.append("Job not found with id: ").append(jobId);
                return null;
            }
            if (job.getRecruiterId() != recruiterId) {
                errorMsg.append("You are not authorized to view applicants for this job. It belongs to another recruiter.");
                return null;
            }
            return dataStore.findApplicationsByJobId(jobId);
        }

        // Return all applicants across all jobs posted by this recruiter
        Set<Integer> recruiterJobIds = dataStore.getJobs().stream()
                .filter(j -> j.getRecruiterId() == recruiterId)
                .map(Job::getId)
                .collect(Collectors.toSet());

        return dataStore.getApplications().stream()
                .filter(app -> recruiterJobIds.contains(app.getJobId()))
                .collect(Collectors.toList());
    }

    public List<Application> getApplicationsByJobId(int jobId, StringBuilder errorMsg) {
        Job job = dataStore.findJobById(jobId);
        if (job == null) {
            errorMsg.append("Job not found with id: ").append(jobId);
            return null;
        }
        return dataStore.findApplicationsByJobId(jobId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RECRUITER: UPDATE STATUS  →  PUT /api/applications/{id}/status
    // ─────────────────────────────────────────────────────────────────────────

    public Application updateStatus(Integer recruiterId, int applicationId, String newStatus, StringBuilder errorMsg) {
        if (newStatus == null || newStatus.trim().isEmpty()) {
            errorMsg.append("Application status is required.");
            return null;
        }

        String normalizedStatus = newStatus.trim().toUpperCase();
        if (!ALLOWED_STATUSES.contains(normalizedStatus)) {
            errorMsg.append("Invalid status: '").append(newStatus)
                    .append("'. Allowed values are: APPLIED, SHORTLISTED, REJECTED, INTERVIEW, SELECTED.");
            return null;
        }

        Application application = dataStore.findApplicationById(applicationId);
        if (application == null) {
            errorMsg.append("Application not found with id: ").append(applicationId);
            return null;
        }

        // Ownership check if recruiterId is supplied
        if (recruiterId != null && recruiterId > 0) {
            User recruiter = dataStore.findUserById(recruiterId);
            if (recruiter == null) {
                errorMsg.append("Recruiter not found with id: ").append(recruiterId);
                return null;
            }
            if (!"RECRUITER".equalsIgnoreCase(recruiter.getRole())) {
                errorMsg.append("Only RECRUITER users can update application status.");
                return null;
            }

            Job job = dataStore.findJobById(application.getJobId());
            if (job != null && job.getRecruiterId() != recruiterId) {
                errorMsg.append("You are not authorized to update this application. The job belongs to another recruiter.");
                return null;
            }
        }

        application.setStatus(normalizedStatus);

        // Notify candidate of status change
        notificationService.createNotification(
                application.getCandidateId(),
                "APPLICATION_STATUS_CHANGED",
                "Your application status for " + application.getJobTitle() + " has been updated to: " + normalizedStatus
        );

        System.out.println("[ApplicationService] Application STATUS UPDATED: id=" + applicationId
                + " → " + normalizedStatus + " by recruiterId=" + recruiterId);

        return application;
    }
}
