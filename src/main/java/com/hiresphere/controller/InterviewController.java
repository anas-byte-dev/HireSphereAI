package com.hiresphere.controller;

import com.hiresphere.dto.InterviewRequest;
import com.hiresphere.dto.InterviewUpdateRequest;
import com.hiresphere.model.Interview;
import com.hiresphere.service.InterviewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * InterviewController - REST API endpoints for interview scheduling and tracking.
 */
@Tag(name = "Interviews", description = "Interview scheduling, meeting link assignment, and interview calendar management")
@RestController
@RequestMapping("/api/interviews")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/interviews
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Schedule a new interview for an application.
     */
    @PostMapping
    public ResponseEntity<?> scheduleInterview(@RequestBody InterviewRequest request) {
        StringBuilder errorMsg = new StringBuilder();
        Interview interview = interviewService.scheduleInterview(request, errorMsg);

        if (interview == null) {
            String err = errorMsg.toString();
            HttpStatus status = err.contains("not found") ? HttpStatus.NOT_FOUND :
                    (err.contains("not authorized") || err.contains("Only RECRUITER") ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST);
            return ResponseEntity.status(status).body(errorBody(err));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(interview);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/interviews/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Update interview schedule, mode, meeting link, notes, or status.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateInterview(@PathVariable int id,
                                             @RequestBody InterviewUpdateRequest request) {
        StringBuilder errorMsg = new StringBuilder();
        Interview updated = interviewService.updateInterview(id, request, errorMsg);

        if (updated == null) {
            String err = errorMsg.toString();
            HttpStatus status = err.contains("not found") ? HttpStatus.NOT_FOUND :
                    (err.contains("not authorized") || err.contains("Only RECRUITER") ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST);
            return ResponseEntity.status(status).body(errorBody(err));
        }

        return ResponseEntity.ok(updated);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/interviews
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * View interviews with optional filters:
     *   - ?candidateId={id}
     *   - ?recruiterId={id}
     *   - ?applicationId={id}
     */
    @GetMapping
    public ResponseEntity<?> getInterviews(@RequestParam(required = false) Integer candidateId,
                                           @RequestParam(required = false) Integer recruiterId,
                                           @RequestParam(required = false) Integer applicationId) {

        if (candidateId != null && candidateId > 0) {
            return ResponseEntity.ok(interviewService.getInterviewsByCandidate(candidateId));
        }

        if (recruiterId != null && recruiterId > 0) {
            return ResponseEntity.ok(interviewService.getInterviewsByRecruiter(recruiterId));
        }

        if (applicationId != null && applicationId > 0) {
            return ResponseEntity.ok(interviewService.getInterviewsByApplication(applicationId));
        }

        return ResponseEntity.ok(interviewService.getAllInterviews());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/interviews/candidate/{candidateId}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Candidate views their scheduled interviews.
     */
    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<List<Interview>> getCandidateInterviews(@PathVariable int candidateId) {
        return ResponseEntity.ok(interviewService.getInterviewsByCandidate(candidateId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/interviews/recruiter/{recruiterId}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Recruiter views their interviews.
     */
    @GetMapping("/recruiter/{recruiterId}")
    public ResponseEntity<List<Interview>> getRecruiterInterviews(@PathVariable int recruiterId) {
        return ResponseEntity.ok(interviewService.getInterviewsByRecruiter(recruiterId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/interviews/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * View interview details by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getInterviewById(@PathVariable int id) {
        Interview interview = interviewService.getInterviewById(id);
        if (interview == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(errorBody("Interview not found with id: " + id));
        }
        return ResponseEntity.ok(interview);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Map<String, Object> errorBody(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", true);
        body.put("message", message);
        return body;
    }
}
