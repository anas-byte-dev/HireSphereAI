package com.hiresphere.service;

import com.hiresphere.dto.SavedJobRequest;
import com.hiresphere.model.Job;
import com.hiresphere.model.SavedJob;
import com.hiresphere.model.User;
import com.hiresphere.store.DataStore;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * SavedJobService - Business logic for Saved Jobs (Job Bookmarks).
 *
 * Candidates can bookmark jobs, remove bookmarks, and view their saved jobs list.
 * Prevents saving the same job multiple times.
 */
@Service
public class SavedJobService {

    private final DataStore dataStore;

    public SavedJobService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SAVE A JOB
    // ─────────────────────────────────────────────────────────────────────────

    public SavedJob saveJob(Integer candidateIdParam, SavedJobRequest request, StringBuilder errorMsg) {
        int candidateId = (candidateIdParam != null && candidateIdParam > 0)
                ? candidateIdParam
                : (request != null && request.getCandidateId() > 0 ? request.getCandidateId() : 0);

        int jobId = (request != null && request.getJobId() > 0) ? request.getJobId() : 0;

        if (candidateId <= 0) {
            errorMsg.append("Candidate ID is required (pass via ?candidateId=X or in request body).");
            return null;
        }

        if (jobId <= 0) {
            errorMsg.append("Job ID is required.");
            return null;
        }

        // Validate candidate exists
        User candidate = dataStore.findUserById(candidateId);
        if (candidate == null) {
            errorMsg.append("Candidate not found with id: ").append(candidateId);
            return null;
        }
        if (!"CANDIDATE".equalsIgnoreCase(candidate.getRole())) {
            errorMsg.append("Only users with CANDIDATE role can save jobs.");
            return null;
        }

        // Validate job exists
        Job job = dataStore.findJobById(jobId);
        if (job == null) {
            errorMsg.append("Job not found with id: ").append(jobId);
            return null;
        }

        // Prevent duplicate saved jobs
        if (dataStore.isJobSavedByCandidate(candidateId, jobId)) {
            errorMsg.append("Job is already saved by this candidate.");
            return null;
        }

        // Create new SavedJob with enriched job metadata
        SavedJob savedJob = new SavedJob(candidateId, jobId);
        savedJob.setSavedDate(LocalDate.now());
        savedJob.setJobTitle(job.getTitle());
        savedJob.setCompany(job.getCompany());
        savedJob.setLocation(job.getLocation());
        savedJob.setSalaryRange(job.getSalaryRange());
        savedJob.setEmploymentType(job.getEmploymentType());
        savedJob.setJob(job);

        dataStore.addSavedJob(savedJob);
        return savedJob;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // VIEW SAVED JOBS
    // ─────────────────────────────────────────────────────────────────────────

    public List<SavedJob> getSavedJobsByCandidate(int candidateId) {
        List<SavedJob> list = dataStore.findSavedJobsByCandidateId(candidateId);
        // Refresh job details in case job info was updated
        for (SavedJob sj : list) {
            Job job = dataStore.findJobById(sj.getJobId());
            if (job != null) {
                sj.setJobTitle(job.getTitle());
                sj.setCompany(job.getCompany());
                sj.setLocation(job.getLocation());
                sj.setSalaryRange(job.getSalaryRange());
                sj.setEmploymentType(job.getEmploymentType());
                sj.setJob(job);
            }
        }
        return list;
    }

    public List<SavedJob> getAllSavedJobs() {
        return dataStore.getSavedJobs();
    }

    public SavedJob getSavedJobById(int id) {
        SavedJob sj = dataStore.findSavedJobById(id);
        if (sj != null) {
            Job job = dataStore.findJobById(sj.getJobId());
            if (job != null) {
                sj.setJobTitle(job.getTitle());
                sj.setCompany(job.getCompany());
                sj.setLocation(job.getLocation());
                sj.setSalaryRange(job.getSalaryRange());
                sj.setEmploymentType(job.getEmploymentType());
                sj.setJob(job);
            }
        }
        return sj;
    }

    public boolean isJobSaved(int candidateId, int jobId) {
        return dataStore.isJobSavedByCandidate(candidateId, jobId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REMOVE SAVED JOB
    // ─────────────────────────────────────────────────────────────────────────

    public boolean removeSavedJob(int id) {
        SavedJob existing = dataStore.findSavedJobById(id);
        if (existing == null) {
            return false;
        }
        dataStore.removeSavedJob(id);
        return true;
    }

    public boolean removeSavedJobByCandidateAndJob(int candidateId, int jobId) {
        return dataStore.removeSavedJobByCandidateAndJob(candidateId, jobId);
    }
}
