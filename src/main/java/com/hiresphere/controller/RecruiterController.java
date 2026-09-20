package com.hiresphere.controller;

import com.hiresphere.dto.CompanyRequest;
import com.hiresphere.dto.RecruiterProfileRequest;
import com.hiresphere.model.Company;
import com.hiresphere.model.RecruiterProfile;
import com.hiresphere.service.RecruiterService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * RecruiterController - REST endpoints for Recruiter Profile and Company operations.
 */
@Tag(name = "Recruiters", description = "Recruiter profile and company information management")
@RestController
@RequestMapping("/api/recruiters")
public class RecruiterController {

    private final RecruiterService recruiterService;

    public RecruiterController(RecruiterService recruiterService) {
        this.recruiterService = recruiterService;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/recruiters/{userId}/profile
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * View recruiter profile.
     *
     * Example: GET /api/recruiters/2/profile
     *
     * Success (200):
     * {
     *   "id": 1,
     *   "userId": 2,
     *   "companyId": 1,
     *   "designation": "Technical Recruiter",
     *   "phone": "+91-9876543210",
     *   "linkedIn": "https://linkedin.com/in/john-recruiter"
     * }
     */
    @GetMapping("/{userId}/profile")
    public ResponseEntity<?> getProfile(@PathVariable int userId) {

        StringBuilder errorMsg = new StringBuilder();
        RecruiterProfile profile = recruiterService.getProfile(userId, errorMsg);

        if (profile == null) {
            String err = errorMsg.toString();
            HttpStatus status = (err.contains("not found") || err.contains("not exist"))
                    ? HttpStatus.NOT_FOUND
                    : (err.contains("not a RECRUITER") ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST);
            return ResponseEntity
                    .status(status)
                    .body(errorBody(err));
        }

        return ResponseEntity.ok(profile);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/recruiters/{userId}/profile
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Create or update recruiter profile (Upsert).
     */
    @PutMapping("/{userId}/profile")
    public ResponseEntity<?> saveProfile(@PathVariable int userId,
                                         @RequestBody RecruiterProfileRequest request) {

        StringBuilder errorMsg = new StringBuilder();
        RecruiterProfile profile = recruiterService.saveProfile(userId, request, errorMsg);

        if (profile == null) {
            String err = errorMsg.toString();
            HttpStatus status = (err.contains("not found") || err.contains("not exist"))
                    ? HttpStatus.NOT_FOUND
                    : (err.contains("Only RECRUITER") || err.contains("not a RECRUITER") ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST);
            return ResponseEntity
                    .status(status)
                    .body(errorBody(err));
        }

        return ResponseEntity.ok(profile);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/recruiters/{userId}/company
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * View company info for a recruiter.
     */
    @GetMapping("/{userId}/company")
    public ResponseEntity<?> getCompany(@PathVariable int userId) {

        StringBuilder errorMsg = new StringBuilder();
        Company company = recruiterService.getCompany(userId, errorMsg);

        if (company == null) {
            String err = errorMsg.toString();
            HttpStatus status = (err.contains("not found") || err.contains("not exist"))
                    ? HttpStatus.NOT_FOUND
                    : (err.contains("not a RECRUITER") ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST);
            return ResponseEntity
                    .status(status)
                    .body(errorBody(err));
        }

        return ResponseEntity.ok(company);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/recruiters/{userId}/company
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Create or update company information (Upsert).
     */
    @PutMapping("/{userId}/company")
    public ResponseEntity<?> saveCompany(@PathVariable int userId,
                                         @RequestBody CompanyRequest request) {

        StringBuilder errorMsg = new StringBuilder();
        Company company = recruiterService.saveCompany(userId, request, errorMsg);

        if (company == null) {
            String err = errorMsg.toString();
            HttpStatus status = (err.contains("not found") || err.contains("not exist"))
                    ? HttpStatus.NOT_FOUND
                    : (err.contains("Only RECRUITER") || err.contains("not a RECRUITER") ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST);
            return ResponseEntity
                    .status(status)
                    .body(errorBody(err));
        }

        return ResponseEntity.ok(company);
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
