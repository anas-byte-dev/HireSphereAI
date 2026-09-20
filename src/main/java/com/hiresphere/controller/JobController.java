package com.hiresphere.controller;

import com.hiresphere.dto.JobRequest;
import com.hiresphere.model.Job;
import com.hiresphere.service.JobService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.hiresphere.dto.ApplicationRequest;
import com.hiresphere.model.Application;
import com.hiresphere.service.ApplicationService;

/**
 * JobController - REST endpoints for Job Management.
 */
@Tag(name = "Jobs", description = "Job posting creation, retrieval, filtering, search, and status updates")
@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;
    private final ApplicationService applicationService;

    public JobController(JobService jobService, ApplicationService applicationService) {
        this.jobService = jobService;
        this.applicationService = applicationService;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/jobs
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<?> createJob(@RequestParam(required = false) Integer recruiterId,
                                       @RequestBody JobRequest request) {

        StringBuilder errorMsg = new StringBuilder();
        Job job = jobService.createJob(recruiterId, request, errorMsg);

        if (job == null) {
            String err = errorMsg.toString();
            HttpStatus status = err.contains("not found") ? HttpStatus.NOT_FOUND :
                    (err.contains("Only RECRUITER") ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST);
            return ResponseEntity
                    .status(status)
                    .body(errorBody(err));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(job);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/jobs
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * View jobs.
     * - Candidates view active jobs (default when no recruiterId is given)
     * - Recruiters view their posted jobs (?recruiterId=X)
     * - Optional search filters supported (keyword, location, employmentType, experience, skills)
     */
    @GetMapping
    public ResponseEntity<List<Job>> getJobs(
            @RequestParam(required = false) Integer recruiterId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String employmentType,
            @RequestParam(required = false) String jobType,
            @RequestParam(required = false) String experience,
            @RequestParam(required = false) String skills) {

        List<Job> jobs;

        if (recruiterId != null) {
            // Recruiter dashboard: view their jobs
            jobs = jobService.getJobsByRecruiter(recruiterId);
        } else {
            String effectiveType = (jobType != null && !jobType.isBlank()) ? jobType : employmentType;
            jobs = jobService.searchJobs(keyword, location, effectiveType, experience, skills);
        }

        return ResponseEntity.ok(jobs);
    }

    /**
     * View jobs posted by a specific recruiter.
     */
    @GetMapping("/recruiter/{recruiterId}")
    public ResponseEntity<List<Job>> getJobsByRecruiterPath(@PathVariable int recruiterId) {
        return ResponseEntity.ok(jobService.getJobsByRecruiter(recruiterId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/jobs/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * View job details by id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getJobById(@PathVariable int id) {

        StringBuilder errorMsg = new StringBuilder();
        Job job = jobService.getJobById(id, errorMsg);

        if (job == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(errorBody(errorMsg.toString()));
        }

        return ResponseEntity.ok(job);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/jobs/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Update a job.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateJob(@PathVariable int id,
                                       @RequestParam(required = false) Integer recruiterId,
                                       @RequestBody JobRequest request) {

        StringBuilder errorMsg = new StringBuilder();
        Job job = jobService.updateJob(recruiterId, id, request, errorMsg);

        if (job == null) {
            String err = errorMsg.toString();
            HttpStatus status = err.contains("not found") ? HttpStatus.NOT_FOUND :
                    (err.contains("not authorized") || err.contains("Only RECRUITER") ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST);
            return ResponseEntity
                    .status(status)
                    .body(errorBody(err));
        }

        return ResponseEntity.ok(job);
    }

    /**
     * Toggle or update job active/open status.
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> toggleJobStatus(@PathVariable int id,
                                            @RequestParam(required = false) Integer recruiterId,
                                            @RequestBody(required = false) Map<String, Object> body) {

        StringBuilder errorMsg = new StringBuilder();
        JobRequest req = new JobRequest();
        if (body != null && body.containsKey("active")) {
            req.setActive(Boolean.valueOf(String.valueOf(body.get("active"))));
        } else if (body != null && body.containsKey("status")) {
            req.setStatus(String.valueOf(body.get("status")));
        }

        Job job = jobService.updateJob(recruiterId, id, req, errorMsg);

        if (job == null) {
            String err = errorMsg.toString();
            HttpStatus status = err.contains("not found") ? HttpStatus.NOT_FOUND :
                    (err.contains("not authorized") || err.contains("Only RECRUITER") ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST);
            return ResponseEntity
                    .status(status)
                    .body(errorBody(err));
        }

        return ResponseEntity.ok(job);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/jobs/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Delete a job.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteJob(@PathVariable int id,
                                       @RequestParam(required = false) Integer recruiterId) {

        StringBuilder errorMsg = new StringBuilder();
        boolean deleted = jobService.deleteJob(recruiterId, id, errorMsg);

        if (!deleted) {
            String err = errorMsg.toString();
            HttpStatus status = err.contains("not found") ? HttpStatus.NOT_FOUND :
                    (err.contains("not authorized") || err.contains("Only RECRUITER") ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST);
            return ResponseEntity
                    .status(status)
                    .body(errorBody(err));
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Job deleted successfully.");
        response.put("jobId", id);
        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/jobs/{id}/apply
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Apply directly to a specific job by ID.
     * Prevents duplicate applications and enforces 70% skill match.
     */
    @PostMapping("/{id}/apply")
    public ResponseEntity<?> applyToJob(@PathVariable int id,
                                        @RequestBody(required = false) ApplicationRequest request,
                                        @RequestParam(required = false) Integer candidateId) {

        if (request == null) {
            request = new ApplicationRequest();
        }
        request.setJobId(id);
        if (candidateId != null && candidateId > 0) {
            request.setCandidateId(candidateId);
        }

        StringBuilder errorMsg = new StringBuilder();
        Application application = applicationService.applyForJob(candidateId, request, errorMsg);

        if (application == null) {
            String err = errorMsg.toString();
            HttpStatus status = err.contains("not found") ? HttpStatus.NOT_FOUND :
                    (err.contains("Only users with CANDIDATE") ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST);
            return ResponseEntity
                    .status(status)
                    .body(errorBody(err));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(application);
    }

    private Map<String, String> errorBody(String message) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("error", message);
        return body;
    }
}
