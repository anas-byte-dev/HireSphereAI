package com.hiresphere.controller;

import com.hiresphere.dto.CandidateProfileRequest;
import com.hiresphere.model.CandidateProfile;
import com.hiresphere.service.CandidateService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * CandidateController - REST endpoints for Candidate Profile operations.
 */
@Tag(name = "Candidates", description = "Candidate profile management, bio, education, resume links, and skill inventory")
@RestController
@RequestMapping("/api/candidates")
public class CandidateController {

    private final CandidateService candidateService;

    // Constructor injection
    public CandidateController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/candidates/{userId}/profile
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * View a candidate's profile.
     *
     * Example: GET /api/candidates/3/profile
     *
     * Success response (200):
     * {
     *   "id": 1,
     *   "userId": 3,
     *   "headline": "Java Developer | 2025 Fresher",
     *   "bio": "...",
     *   "phone": "+91-9123456789",
     *   "location": "Mumbai, India",
     *   "education": "B.Tech CSE - Mumbai University (2025)",
     *   "experience": "Fresher",
     *   "resumeUrl": "https://...",
     *   "githubUrl": "https://github.com/alice",
     *   "linkedInUrl": "https://linkedin.com/in/alice",
     *   "skills": ["Java", "Spring Boot", "React"]
     * }
     *
     * @param userId  The candidate's userId from the URL path
     */
    @GetMapping("/{userId}/profile")
    public ResponseEntity<?> getProfile(@PathVariable int userId) {

        StringBuilder errorMsg = new StringBuilder();
        CandidateProfile profile = candidateService.getProfile(userId, errorMsg);

        if (profile == null) {
            String err = errorMsg.toString();
            HttpStatus status = (err.contains("not found") || err.contains("not exist"))
                    ? HttpStatus.NOT_FOUND
                    : (err.contains("not a CANDIDATE") ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST);
            return ResponseEntity
                    .status(status)
                    .body(errorBody(err));
        }

        return ResponseEntity.ok(profile);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/candidates/{userId}/profile
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Create or update a candidate's profile (Upsert).
     *
     * Using PUT means "set the entire resource".
     * Our service handles both create and update internally.
     */
    @PutMapping("/{userId}/profile")
    public ResponseEntity<?> saveProfile(@PathVariable int userId,
                                         @RequestBody CandidateProfileRequest request) {

        StringBuilder errorMsg = new StringBuilder();
        CandidateProfile profile = candidateService.saveProfile(userId, request, errorMsg);

        if (profile == null) {
            String err = errorMsg.toString();
            HttpStatus status = (err.contains("not found") || err.contains("not exist"))
                    ? HttpStatus.NOT_FOUND
                    : (err.contains("Only CANDIDATE") ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST);
            return ResponseEntity
                    .status(status)
                    .body(errorBody(err));
        }

        return ResponseEntity.ok(profile);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────────────────────────────────

    private Map<String, String> errorBody(String message) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("error", message);
        return body;
    }
}
