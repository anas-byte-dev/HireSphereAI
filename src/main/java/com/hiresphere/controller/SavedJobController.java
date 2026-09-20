package com.hiresphere.controller;

import com.hiresphere.dto.SavedJobRequest;
import com.hiresphere.model.SavedJob;
import com.hiresphere.service.SavedJobService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SavedJobController - REST API endpoints for Saved Jobs (Job Bookmarks).
 */
@Tag(name = "Saved Jobs", description = "Candidate bookmarks and saved job management")
@RestController
@RequestMapping("/api/saved-jobs")
public class SavedJobController {

    private final SavedJobService savedJobService;

    public SavedJobController(SavedJobService savedJobService) {
        this.savedJobService = savedJobService;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/saved-jobs
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Save/bookmark a job for a candidate.
     * Prevents duplicate saves.
     */
    @PostMapping
    public ResponseEntity<?> saveJob(@RequestParam(required = false) Integer candidateId,
                                     @RequestBody(required = false) SavedJobRequest request) {

        if (request == null) {
            request = new SavedJobRequest();
        }

        StringBuilder errorMsg = new StringBuilder();
        SavedJob savedJob = savedJobService.saveJob(candidateId, request, errorMsg);

        if (savedJob == null) {
            String error = errorMsg.toString();
            HttpStatus status = error.contains("not found") ? HttpStatus.NOT_FOUND :
                    (error.contains("Only users with CANDIDATE") ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST);
            return ResponseEntity.status(status).body(errorBody(error));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(savedJob);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/saved-jobs
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * View saved jobs.
     * Filter by ?candidateId={id} to see jobs saved by a specific candidate.
     */
    @GetMapping
    public ResponseEntity<?> getSavedJobs(@RequestParam(required = false) Integer candidateId) {
        if (candidateId != null && candidateId > 0) {
            List<SavedJob> list = savedJobService.getSavedJobsByCandidate(candidateId);
            return ResponseEntity.ok(list);
        }
        return ResponseEntity.ok(savedJobService.getAllSavedJobs());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/saved-jobs/candidate/{candidateId}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * View saved jobs for a specific candidate.
     */
    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<List<SavedJob>> getSavedJobsByCandidatePath(@PathVariable int candidateId) {
        List<SavedJob> list = savedJobService.getSavedJobsByCandidate(candidateId);
        return ResponseEntity.ok(list);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/saved-jobs/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * View saved job by record ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getSavedJobById(@PathVariable int id) {
        SavedJob savedJob = savedJobService.getSavedJobById(id);
        if (savedJob == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(errorBody("Saved job not found with id: " + id));
        }
        return ResponseEntity.ok(savedJob);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/saved-jobs/check
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Check if a specific job has already been saved by a candidate.
     */
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> isJobSaved(@RequestParam int candidateId,
                                                          @RequestParam int jobId) {
        boolean saved = savedJobService.isJobSaved(candidateId, jobId);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("candidateId", candidateId);
        response.put("jobId", jobId);
        response.put("isSaved", saved);
        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/saved-jobs/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Remove a saved job by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeSavedJob(@PathVariable int id) {
        boolean removed = savedJobService.removeSavedJob(id);
        if (!removed) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(errorBody("Saved job not found with id: " + id));
        }
        return ResponseEntity.ok(successBody("Saved job removed successfully."));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/saved-jobs?candidateId={cId}&jobId={jId}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Remove a saved job by candidateId and jobId.
     */
    @DeleteMapping
    public ResponseEntity<?> removeSavedJobByCandidateAndJob(@RequestParam int candidateId,
                                                             @RequestParam int jobId) {
        boolean removed = savedJobService.removeSavedJobByCandidateAndJob(candidateId, jobId);
        if (!removed) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(errorBody("Saved job bookmark not found for candidate " + candidateId + " and job " + jobId));
        }
        return ResponseEntity.ok(successBody("Saved job removed successfully."));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/saved-jobs/job/{jobId}?candidateId={cId}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Remove a saved job directly by jobId and candidateId.
     */
    @DeleteMapping("/job/{jobId}")
    public ResponseEntity<?> removeSavedJobByJobId(@PathVariable int jobId,
                                                   @RequestParam int candidateId) {
        boolean removed = savedJobService.removeSavedJobByCandidateAndJob(candidateId, jobId);
        if (!removed) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(errorBody("Saved job bookmark not found for candidate " + candidateId + " and job " + jobId));
        }
        return ResponseEntity.ok(successBody("Saved job removed successfully."));
    }

    // ── Helper Methods ────────────────────────────────────────────────────────

    private Map<String, Object> errorBody(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", true);
        body.put("message", message);
        return body;
    }

    private Map<String, Object> successBody(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("message", message);
        return body;
    }
}
