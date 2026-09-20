package com.hiresphere.service;

import com.hiresphere.dto.JobRequest;
import com.hiresphere.model.Company;
import com.hiresphere.model.Job;
import com.hiresphere.model.RecruiterProfile;
import com.hiresphere.model.User;
import com.hiresphere.store.DataStore;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JobService - Business logic for Job Management (CRUD).
 *
 * Operations:
 *   1. createJob(recruiterId, request)     → POST /api/jobs
 *   2. getActiveJobs()                     → GET  /api/jobs (candidates see active/open jobs)
 *   3. getJobsByRecruiter(recruiterId)     → GET  /api/jobs?recruiterId=X (recruiter sees their own)
 *   4. getJobById(jobId)                   → GET  /api/jobs/{id}
 *   5. updateJob(recruiterId, jobId, req)  → PUT  /api/jobs/{id}
 *   6. deleteJob(recruiterId, jobId)       → DELETE /api/jobs/{id}
 */
@Service
public class JobService {

    private final DataStore dataStore;

    public JobService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE JOB  →  POST /api/jobs
    // ─────────────────────────────────────────────────────────────────────────

    public Job createJob(Integer recruiterId, JobRequest request, StringBuilder errorMsg) {
        int effectiveRecruiterId = (recruiterId != null && recruiterId > 0)
                ? recruiterId
                : (request.getRecruiterId() != null ? request.getRecruiterId() : 0);

        if (effectiveRecruiterId <= 0) {
            errorMsg.append("Recruiter ID is required (pass via ?recruiterId=X or recruiterId in body).");
            return null;
        }

        User user = dataStore.findUserById(effectiveRecruiterId);
        if (user == null) {
            errorMsg.append("Recruiter not found with id: ").append(effectiveRecruiterId);
            return null;
        }
        if (!"RECRUITER".equalsIgnoreCase(user.getRole())) {
            errorMsg.append("Only RECRUITER users can post jobs.");
            return null;
        }

        if (isBlank(request.getTitle())) {
            errorMsg.append("Job title is required.");
            return null;
        }
        if (isBlank(request.getLocation())) {
            errorMsg.append("Job location is required.");
            return null;
        }
        if (isBlank(request.getEmploymentType())) {
            errorMsg.append("Employment type is required (e.g., FULL_TIME, PART_TIME, INTERNSHIP, CONTRACT).");
            return null;
        }

        RecruiterProfile profile = dataStore.findRecruiterProfileByUserId(effectiveRecruiterId);
        int companyId = (profile != null) ? profile.getCompanyId() : 0;

        Job job = new Job();
        job.setRecruiterId(effectiveRecruiterId);
        job.setCompanyId(companyId);
        job.setPostedDate(LocalDate.now());
        job.setActive(true);

        // Determine company name
        if (request.getCompany() != null && !request.getCompany().isBlank()) {
            job.setCompany(request.getCompany().trim());
        } else if (companyId > 0) {
            Company comp = dataStore.findCompanyById(companyId);
            if (comp != null && comp.getName() != null) {
                job.setCompany(comp.getName());
            }
        }

        applyFields(job, request);
        dataStore.addJob(job);

        System.out.println("[JobService] Job CREATED: '" + job.getTitle()
                + "' | id=" + job.getId() + " | recruiterId=" + effectiveRecruiterId);

        return job;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET ACTIVE JOBS  →  GET /api/jobs (candidate view)
    // ─────────────────────────────────────────────────────────────────────────

    public List<Job> getActiveJobs() {
        return dataStore.getJobs().stream()
                .filter(Job::isActive)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SEARCH / FILTER ACTIVE JOBS  →  GET /api/jobs
    // ─────────────────────────────────────────────────────────────────────────

    public List<Job> searchJobs(String keyword, String location,
                                String employmentType, String experience,
                                String skills) {

        return dataStore.getJobs().stream()
                // 1. Only active jobs are shown to candidates
                .filter(Job::isActive)

                // 2. Keyword filter (checks title, description, requirements, company, and skills)
                .filter(j -> {
                    if (isBlank(keyword)) return true;
                    String kw = keyword.toLowerCase().trim();
                    boolean inSkills = j.getSkills() != null && j.getSkills().stream()
                            .anyMatch(s -> s.toLowerCase().contains(kw));
                    return contains(j.getTitle(), kw)
                            || contains(j.getDescription(), kw)
                            || contains(j.getRequirements(), kw)
                            || contains(j.getCompany(), kw)
                            || inSkills;
                })

                // 3. Location filter (case-insensitive substring)
                .filter(j -> {
                    if (isBlank(location)) return true;
                    return contains(j.getLocation(), location.toLowerCase().trim());
                })

                // 4. Job type / Employment type filter (flexible matching: FULL_TIME, full time, INTERNSHIP)
                .filter(j -> {
                    if (isBlank(employmentType)) return true;
                    if (j.getEmploymentType() == null) return false;
                    String reqType = employmentType.trim().replace(" ", "_").toUpperCase();
                    String curType = j.getEmploymentType().trim().replace(" ", "_").toUpperCase();
                    return curType.contains(reqType) || reqType.contains(curType);
                })

                // 5. Experience filter (e.g. "fresher", "0-2 years")
                .filter(j -> {
                    if (isBlank(experience)) return true;
                    return contains(j.getExperience(), experience.toLowerCase().trim());
                })

                // 6. Skills filter (comma-separated: e.g. "java,react")
                .filter(j -> {
                    if (isBlank(skills)) return true;
                    if (j.getSkills() == null || j.getSkills().isEmpty()) return false;

                    String[] requestedSkills = skills.toLowerCase().split(",");
                    List<String> jobSkills = j.getSkills().stream()
                            .map(String::toLowerCase)
                            .collect(Collectors.toList());

                    for (String requestedSkill : requestedSkills) {
                        String trimmed = requestedSkill.trim();
                        if (trimmed.isEmpty()) continue;
                        for (String jobSkill : jobSkills) {
                            if (jobSkill.contains(trimmed) || trimmed.contains(jobSkill)) {
                                return true;
                            }
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET JOBS BY RECRUITER  →  GET /api/jobs?recruiterId=X
    // ─────────────────────────────────────────────────────────────────────────

    public List<Job> getJobsByRecruiter(int recruiterId) {
        return dataStore.getJobs().stream()
                .filter(j -> j.getRecruiterId() == recruiterId)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET JOB BY ID  →  GET /api/jobs/{id}
    // ─────────────────────────────────────────────────────────────────────────

    public Job getJobById(int jobId, StringBuilder errorMsg) {
        Job job = dataStore.findJobById(jobId);
        if (job == null) {
            errorMsg.append("Job not found with id: ").append(jobId);
        }
        return job;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UPDATE JOB  →  PUT /api/jobs/{id}
    // ─────────────────────────────────────────────────────────────────────────

    public Job updateJob(Integer recruiterId, int jobId, JobRequest request,
                         StringBuilder errorMsg) {

        int effectiveRecruiterId = (recruiterId != null && recruiterId > 0)
                ? recruiterId
                : (request.getRecruiterId() != null ? request.getRecruiterId() : 0);

        Job job = dataStore.findJobById(jobId);
        if (job == null) {
            errorMsg.append("Job not found with id: ").append(jobId);
            return null;
        }

        // Ownership check if recruiterId provided
        if (effectiveRecruiterId > 0) {
            User user = dataStore.findUserById(effectiveRecruiterId);
            if (user == null) {
                errorMsg.append("Recruiter not found with id: ").append(effectiveRecruiterId);
                return null;
            }
            if (!"RECRUITER".equalsIgnoreCase(user.getRole())) {
                errorMsg.append("Only RECRUITER users can update jobs.");
                return null;
            }
            if (job.getRecruiterId() != effectiveRecruiterId) {
                errorMsg.append("You are not authorized to update this job. It belongs to another recruiter.");
                return null;
            }
        }

        applyFields(job, request);

        System.out.println("[JobService] Job UPDATED: id=" + jobId
                + " | effectiveRecruiterId=" + effectiveRecruiterId);

        return job;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE JOB  →  DELETE /api/jobs/{id}
    // ─────────────────────────────────────────────────────────────────────────

    public boolean deleteJob(Integer recruiterId, int jobId, StringBuilder errorMsg) {
        Job job = dataStore.findJobById(jobId);
        if (job == null) {
            errorMsg.append("Job not found with id: ").append(jobId);
            return false;
        }

        if (recruiterId != null && recruiterId > 0) {
            User user = dataStore.findUserById(recruiterId);
            if (user == null) {
                errorMsg.append("Recruiter not found with id: ").append(recruiterId);
                return false;
            }
            if (!"RECRUITER".equalsIgnoreCase(user.getRole())) {
                errorMsg.append("Only RECRUITER users can delete jobs.");
                return false;
            }
            if (job.getRecruiterId() != recruiterId) {
                errorMsg.append("You are not authorized to delete this job. It belongs to another recruiter.");
                return false;
            }
        }

        dataStore.removeJob(jobId);

        System.out.println("[JobService] Job DELETED: id=" + jobId
                + " by recruiterId=" + recruiterId);

        return true;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void applyFields(Job job, JobRequest request) {
        if (request.getTitle()          != null) job.setTitle(request.getTitle());
        if (request.getCompany()        != null) job.setCompany(request.getCompany());
        if (request.getDescription()    != null) job.setDescription(request.getDescription());
        if (request.getRequirements()   != null) job.setRequirements(request.getRequirements());
        if (request.getLocation()       != null) job.setLocation(request.getLocation());
        if (request.getEmploymentType() != null) job.setEmploymentType(request.getEmploymentType());
        if (request.getSalaryRange()    != null) job.setSalaryRange(request.getSalaryRange());
        if (request.getExperience()     != null) job.setExperience(request.getExperience());
        if (request.getSkills()         != null) job.setSkills(request.getSkills());

        if (request.getActive() != null) {
            job.setActive(request.getActive());
        } else if (request.getStatus() != null) {
            job.setStatus(request.getStatus());
        }

        if (request.getDeadline() != null && !request.getDeadline().isBlank()) {
            try {
                job.setDeadline(LocalDate.parse(request.getDeadline()));
            } catch (Exception e) {
                System.out.println("[JobService] Warning: invalid deadline format: " + request.getDeadline());
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean contains(String field, String searchTerm) {
        if (field == null) return false;
        return field.toLowerCase().contains(searchTerm);
    }
}
