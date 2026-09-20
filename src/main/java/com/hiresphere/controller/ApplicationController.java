package com.hiresphere.controller;

import com.hiresphere.dto.ApplicationRequest;
import com.hiresphere.dto.ApplicationStatusUpdateRequest;
import com.hiresphere.model.Application;
import com.hiresphere.service.ApplicationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ApplicationController - REST API endpoints for Job Applications.
 */
@Tag(name = "Applications", description = "Application submission, candidate tracking, and recruiter review workflows")
@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/applications
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Apply for a job.
     * Prevents duplicate applications from the same candidate to the same job.
     */
    @PostMapping
    public ResponseEntity<?> applyForJob(@RequestParam(required = false) Integer candidateId,
                                         @RequestBody ApplicationRequest request) {

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

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/applications
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Query applications:
     * - ?candidateId=X  → View applications submitted by candidate
     * - ?recruiterId=X  → View applicants for jobs owned by recruiter (optional ?jobId=Y)
     */
    @GetMapping
    public ResponseEntity<?> getApplications(
            @RequestParam(required = false) Integer candidateId,
            @RequestParam(required = false) Integer recruiterId,
            @RequestParam(required = false) Integer jobId) {

        StringBuilder errorMsg = new StringBuilder();

        if (candidateId != null) {
            List<Application> list = applicationService.getApplicationsByCandidate(candidateId, errorMsg);
            if (list == null) {
                String err = errorMsg.toString();
                HttpStatus status = err.contains("not found") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
                return ResponseEntity.status(status).body(errorBody(err));
            }
            return ResponseEntity.ok(list);
        }

        if (recruiterId != null) {
            List<Application> list = applicationService.getApplicantsForRecruiter(recruiterId, jobId, errorMsg);
            if (list == null) {
                String err = errorMsg.toString();
                HttpStatus status = err.contains("not found") ? HttpStatus.NOT_FOUND :
                        (err.contains("not authorized") || err.contains("Only RECRUITER") ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST);
                return ResponseEntity.status(status).body(errorBody(err));
            }
            return ResponseEntity.ok(list);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorBody("Please provide either ?candidateId={id} or ?recruiterId={id}"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/applications/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * View detailed application information.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getApplicationById(@PathVariable int id) {
        StringBuilder errorMsg = new StringBuilder();
        Application application = applicationService.getApplicationById(id, errorMsg);

        if (application == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(errorBody(errorMsg.toString()));
        }

        return ResponseEntity.ok(application);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/applications/candidate/{candidateId}
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<?> getCandidateApplications(@PathVariable int candidateId) {
        StringBuilder errorMsg = new StringBuilder();
        List<Application> list = applicationService.getApplicationsByCandidate(candidateId, errorMsg);

        if (list == null) {
            String err = errorMsg.toString();
            HttpStatus status = err.contains("not found") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(errorBody(err));
        }

        return ResponseEntity.ok(list);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/applications/job/{jobId}
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/job/{jobId}")
    public ResponseEntity<?> getApplicantsByJob(@PathVariable int jobId,
                                                @RequestParam(required = false) Integer recruiterId) {
        StringBuilder errorMsg = new StringBuilder();
        List<Application> list;
        if (recruiterId != null && recruiterId > 0) {
            list = applicationService.getApplicantsForRecruiter(recruiterId, jobId, errorMsg);
        } else {
            list = applicationService.getApplicationsByJobId(jobId, errorMsg);
        }

        if (list == null) {
            String err = errorMsg.toString();
            HttpStatus status = err.contains("not found") ? HttpStatus.NOT_FOUND :
                    (err.contains("not authorized") || err.contains("Only RECRUITER") ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST);
            return ResponseEntity.status(status).body(errorBody(err));
        }

        return ResponseEntity.ok(list);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/applications/{id}/status
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Update application status (recruiter only).
     * Supported statuses: APPLIED, SHORTLISTED, REJECTED, INTERVIEW, SELECTED.
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable int id,
                                          @RequestParam(required = false) Integer recruiterId,
                                          @RequestBody ApplicationStatusUpdateRequest request) {

        int effectiveRecruiterId = (recruiterId != null && recruiterId > 0)
                ? recruiterId
                : (request.getRecruiterId() != null ? request.getRecruiterId() : 0);

        StringBuilder errorMsg = new StringBuilder();
        Application updated = applicationService.updateStatus(effectiveRecruiterId, id, request.getStatus(), errorMsg);

        if (updated == null) {
            String err = errorMsg.toString();
            HttpStatus status = err.contains("not found") ? HttpStatus.NOT_FOUND :
                    (err.contains("not authorized") || err.contains("Only RECRUITER") ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST);
            return ResponseEntity
                    .status(status)
                    .body(errorBody(err));
        }

        return ResponseEntity.ok(updated);
    }

    /**
     * Convenience alias: PUT /api/applications/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateApplication(@PathVariable int id,
                                               @RequestParam(required = false) Integer recruiterId,
                                               @RequestBody ApplicationStatusUpdateRequest request) {
        return updateStatus(id, recruiterId, request);
    }

    private Map<String, String> errorBody(String message) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("error", message);
        return body;
    }
}
