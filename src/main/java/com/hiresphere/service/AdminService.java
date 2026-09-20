package com.hiresphere.service;

import com.hiresphere.dto.AdminStatsResponse;
import com.hiresphere.model.Application;
import com.hiresphere.model.Job;
import com.hiresphere.model.User;
import com.hiresphere.store.DataStore;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AdminService - Handles administrative business logic.
 *
 * Responsibilities:
 * - View all users, or filter by role (CANDIDATE, RECRUITER)
 * - View all jobs (including open and closed)
 * - View all applications across all candidates and jobs
 * - Activate/deactivate users
 * - Activate/deactivate jobs
 * - Provide basic system statistics
 */
@Service
public class AdminService {

    private final DataStore dataStore;

    public AdminService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * Get all registered users.
     */
    public List<User> getAllUsers() {
        return dataStore.getUsers();
    }

    /**
     * Get users by role (e.g. "CANDIDATE", "RECRUITER", "ADMIN").
     */
    public List<User> getUsersByRole(String role) {
        return dataStore.findUsersByRole(role);
    }

    /**
     * Convenience method to get all candidate users.
     */
    public List<User> getCandidates() {
        return dataStore.findUsersByRole("CANDIDATE");
    }

    /**
     * Convenience method to get all recruiter users.
     */
    public List<User> getRecruiters() {
        return dataStore.findUsersByRole("RECRUITER");
    }

    /**
     * Get user by ID.
     */
    public User getUserById(int userId) {
        return dataStore.findUserById(userId);
    }

    /**
     * Get all job postings (active and inactive).
     */
    public List<Job> getAllJobs() {
        return dataStore.getJobs();
    }

    /**
     * Get job by ID.
     */
    public Job getJobById(int jobId) {
        return dataStore.findJobById(jobId);
    }

    /**
     * Get all job applications.
     */
    public List<Application> getAllApplications() {
        return dataStore.getApplications();
    }

    /**
     * Activate or deactivate a user.
     * Returns updated user, or null if user not found.
     */
    public User setUserActiveStatus(int userId, boolean active) {
        User user = dataStore.findUserById(userId);
        if (user == null) {
            return null;
        }
        user.setActive(active);
        return user;
    }

    /**
     * Toggle a user's active status.
     * Returns updated user, or null if user not found.
     */
    public User toggleUserActiveStatus(int userId) {
        User user = dataStore.findUserById(userId);
        if (user == null) {
            return null;
        }
        user.setActive(!user.isActive());
        return user;
    }

    /**
     * Activate or deactivate a job posting.
     * Returns updated job, or null if job not found.
     */
    public Job setJobActiveStatus(int jobId, boolean active) {
        Job job = dataStore.findJobById(jobId);
        if (job == null) {
            return null;
        }
        job.setActive(active);
        return job;
    }

    /**
     * Toggle a job's active status.
     * Returns updated job, or null if job not found.
     */
    public Job toggleJobActiveStatus(int jobId) {
        Job job = dataStore.findJobById(jobId);
        if (job == null) {
            return null;
        }
        job.setActive(!job.isActive());
        return job;
    }

    /**
     * Calculate and return basic system statistics.
     */
    public AdminStatsResponse getBasicStatistics() {
        List<User> users = dataStore.getUsers();
        List<Job> jobs = dataStore.getJobs();
        List<Application> applications = dataStore.getApplications();

        long totalUsers = users.size();
        long totalCandidates = users.stream()
                .filter(u -> "CANDIDATE".equalsIgnoreCase(u.getRole()))
                .count();
        long totalRecruiters = users.stream()
                .filter(u -> "RECRUITER".equalsIgnoreCase(u.getRole()))
                .count();
        long totalJobs = jobs.size();
        long activeJobs = jobs.stream()
                .filter(Job::isActive)
                .count();
        long totalApplications = applications.size();

        return new AdminStatsResponse(
                totalUsers,
                totalCandidates,
                totalRecruiters,
                totalJobs,
                activeJobs,
                totalApplications
        );
    }
}
