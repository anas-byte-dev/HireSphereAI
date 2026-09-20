package com.hiresphere.service;

import com.hiresphere.dto.InterviewRequest;
import com.hiresphere.dto.InterviewUpdateRequest;
import com.hiresphere.model.Application;
import com.hiresphere.model.Interview;
import com.hiresphere.model.Job;
import com.hiresphere.model.User;
import com.hiresphere.store.DataStore;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * InterviewService - Business logic for scheduling, viewing, and updating interviews.
 *
 * Supported Statuses:
 *   SCHEDULED, COMPLETED, CANCELLED
 */
@Service
public class InterviewService {

    private final DataStore dataStore;
    private final NotificationService notificationService;

    private static final Set<String> ALLOWED_STATUSES = new HashSet<>(Arrays.asList(
            "SCHEDULED", "COMPLETED", "CANCELLED"
    ));

    public InterviewService(DataStore dataStore, NotificationService notificationService) {
        this.dataStore = dataStore;
        this.notificationService = notificationService;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SCHEDULE INTERVIEW
    // ─────────────────────────────────────────────────────────────────────────

    public Interview scheduleInterview(InterviewRequest request, StringBuilder errorMsg) {
        if (request == null) {
            errorMsg.append("Request body cannot be null.");
            return null;
        }

        if (request.getApplicationId() == null || request.getApplicationId() <= 0) {
            errorMsg.append("Application ID is required.");
            return null;
        }

        if (request.getDate() == null || request.getDate().trim().isEmpty()) {
            errorMsg.append("Interview date is required (e.g. '2026-09-25').");
            return null;
        }

        if (request.getTime() == null || request.getTime().trim().isEmpty()) {
            errorMsg.append("Interview time is required (e.g. '11:00 AM' or '14:30').");
            return null;
        }

        // Validate application exists
        Application application = dataStore.findApplicationById(request.getApplicationId());
        if (application == null) {
            errorMsg.append("Application not found with id: ").append(request.getApplicationId());
            return null;
        }

        // Resolve Job
        Job job = dataStore.findJobById(application.getJobId());

        // Resolve Candidate
        int candidateId = (request.getCandidateId() != null && request.getCandidateId() > 0)
                ? request.getCandidateId()
                : application.getCandidateId();
        User candidate = dataStore.findUserById(candidateId);

        // Resolve Recruiter
        int recruiterId = (request.getRecruiterId() != null && request.getRecruiterId() > 0)
                ? request.getRecruiterId()
                : (job != null ? job.getRecruiterId() : 0);
        User recruiter = dataStore.findUserById(recruiterId);

        // Build Interview
        Interview interview = new Interview();
        interview.setApplicationId(application.getId());
        interview.setCandidateId(candidateId);
        interview.setRecruiterId(recruiterId);
        interview.setJobId(application.getJobId());

        interview.setDate(request.getDate().trim());
        interview.setTime(request.getTime().trim());
        interview.setMode(request.getMode() != null && !request.getMode().trim().isEmpty()
                ? request.getMode().trim().toUpperCase()
                : "ONLINE");
        interview.setMeetingLink(request.getMeetingLink() != null ? request.getMeetingLink().trim() : "");
        interview.setNotes(request.getNotes() != null ? request.getNotes().trim() : "");

        String status = (request.getStatus() != null && !request.getStatus().trim().isEmpty())
                ? request.getStatus().trim().toUpperCase()
                : "SCHEDULED";
        interview.setStatus(status);

        // Populate enriched metadata
        if (job != null) {
            interview.setJobTitle(job.getTitle());
            interview.setCompany(job.getCompany());
        } else {
            interview.setJobTitle(application.getJobTitle());
            interview.setCompany(application.getCompany());
        }

        if (candidate != null) {
            interview.setCandidateName(candidate.getName());
            interview.setCandidateEmail(candidate.getEmail());
        } else {
            interview.setCandidateName(application.getCandidateName());
            interview.setCandidateEmail(application.getCandidateEmail());
        }

        if (recruiter != null) {
            interview.setRecruiterName(recruiter.getName());
        }

        // Safe LocalDateTime attempt
        try {
            LocalDate localDate = LocalDate.parse(interview.getDate());
            interview.setScheduledAt(LocalDateTime.of(localDate, LocalTime.of(10, 0)));
        } catch (Exception ignored) {
            interview.setScheduledAt(LocalDateTime.now().plusDays(3));
        }

        // Keep application status in sync with interview
        application.setStatus("INTERVIEW");

        dataStore.addInterview(interview);

        // Notify candidate of scheduled interview
        notificationService.createNotification(
                candidateId,
                "INTERVIEW_SCHEDULED",
                "An interview has been scheduled for " + interview.getJobTitle() + " on " + interview.getDate() + " at " + interview.getTime() + " (" + interview.getMode() + ")."
        );

        return interview;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UPDATE INTERVIEW
    // ─────────────────────────────────────────────────────────────────────────

    public Interview updateInterview(int id, InterviewUpdateRequest request, StringBuilder errorMsg) {
        Interview interview = dataStore.findInterviewById(id);
        if (interview == null) {
            errorMsg.append("Interview not found with id: ").append(id);
            return null;
        }

        if (request.getDate() != null && !request.getDate().trim().isEmpty()) {
            interview.setDate(request.getDate().trim());
        }

        if (request.getTime() != null && !request.getTime().trim().isEmpty()) {
            interview.setTime(request.getTime().trim());
        }

        if (request.getMode() != null && !request.getMode().trim().isEmpty()) {
            interview.setMode(request.getMode().trim().toUpperCase());
        }

        if (request.getMeetingLink() != null) {
            interview.setMeetingLink(request.getMeetingLink().trim());
        }

        if (request.getNotes() != null) {
            interview.setNotes(request.getNotes().trim());
        }

        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            String upperStatus = request.getStatus().trim().toUpperCase();
            if (!ALLOWED_STATUSES.contains(upperStatus)) {
                errorMsg.append("Invalid status '").append(upperStatus)
                        .append("'. Allowed statuses: ").append(ALLOWED_STATUSES);
                return null;
            }
            interview.setStatus(upperStatus);
        }

        // Notify candidate of interview update
        notificationService.createNotification(
                interview.getCandidateId(),
                "INTERVIEW_UPDATED",
                "Your interview for " + interview.getJobTitle() + " has been updated. Date: " + interview.getDate() + ", Time: " + interview.getTime() + ", Status: " + interview.getStatus() + "."
        );

        return interview;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // VIEW INTERVIEWS
    // ─────────────────────────────────────────────────────────────────────────

    public List<Interview> getInterviewsByCandidate(int candidateId) {
        return dataStore.findInterviewsByCandidateId(candidateId);
    }

    public List<Interview> getInterviewsByRecruiter(int recruiterId) {
        return dataStore.findInterviewsByRecruiterId(recruiterId);
    }

    public List<Interview> getInterviewsByApplication(int applicationId) {
        return dataStore.findInterviewsByApplicationId(applicationId);
    }

    public Interview getInterviewById(int id) {
        return dataStore.findInterviewById(id);
    }

    public List<Interview> getAllInterviews() {
        return dataStore.getInterviews();
    }
}
