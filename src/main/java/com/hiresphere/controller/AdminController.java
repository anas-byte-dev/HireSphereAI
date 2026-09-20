package com.hiresphere.controller;

import com.hiresphere.dto.AdminStatsResponse;
import com.hiresphere.dto.StatusUpdateRequest;
import com.hiresphere.model.Application;
import com.hiresphere.model.Job;
import com.hiresphere.model.User;
import com.hiresphere.service.AdminService;
import com.hiresphere.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AdminController - REST APIs for Admin functionality.
 */
@Tag(name = "Admin", description = "Platform administration, platform metrics, user management, and job controls")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final AuthService authService;

    public AdminController(AdminService adminService, AuthService authService) {
        this.adminService = adminService;
        this.authService = authService;
    }

    /**
     * Optional authorization check for Admin role.
     */
    private ResponseEntity<?> verifyAdmin(String authHeader, Integer adminId) {
        if (authHeader != null && !authHeader.isBlank()) {
            User user = authService.getUserByToken(authHeader);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(errorBody("Invalid or expired session token."));
            }
            if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(errorBody("Access denied: Admin role required."));
            }
        } else if (adminId != null) {
            User user = adminService.getUserById(adminId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(errorBody("Admin user not found with id: " + adminId));
            }
            if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(errorBody("Access denied: Admin role required."));
            }
        }
        return null;
    }

    /**
     * View all users, optionally filtered by ?role=CANDIDATE or ?role=RECRUITER.
     */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@RequestParam(required = false) String role,
                                         @RequestHeader(value = "Authorization", required = false) String authHeader,
                                         @RequestParam(required = false) Integer adminId) {
        ResponseEntity<?> authErr = verifyAdmin(authHeader, adminId);
        if (authErr != null) return authErr;

        if (role != null && !role.isBlank()) {
            return ResponseEntity.ok(adminService.getUsersByRole(role));
        }
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    /**
     * View candidates.
     */
    @GetMapping("/candidates")
    public ResponseEntity<?> getCandidates(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                           @RequestParam(required = false) Integer adminId) {
        ResponseEntity<?> authErr = verifyAdmin(authHeader, adminId);
        if (authErr != null) return authErr;

        return ResponseEntity.ok(adminService.getCandidates());
    }

    /**
     * View recruiters.
     */
    @GetMapping("/recruiters")
    public ResponseEntity<?> getRecruiters(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                           @RequestParam(required = false) Integer adminId) {
        ResponseEntity<?> authErr = verifyAdmin(authHeader, adminId);
        if (authErr != null) return authErr;

        return ResponseEntity.ok(adminService.getRecruiters());
    }

    /**
     * View all jobs.
     */
    @GetMapping("/jobs")
    public ResponseEntity<?> getAllJobs(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                        @RequestParam(required = false) Integer adminId) {
        ResponseEntity<?> authErr = verifyAdmin(authHeader, adminId);
        if (authErr != null) return authErr;

        return ResponseEntity.ok(adminService.getAllJobs());
    }

    /**
     * View all applications.
     */
    @GetMapping("/applications")
    public ResponseEntity<?> getAllApplications(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                                @RequestParam(required = false) Integer adminId) {
        ResponseEntity<?> authErr = verifyAdmin(authHeader, adminId);
        if (authErr != null) return authErr;

        return ResponseEntity.ok(adminService.getAllApplications());
    }

    /**
     * Activate or deactivate a user.
     * Supports both request body { "active": boolean } and query param ?active=true/false.
     */
    @RequestMapping(value = "/users/{id}/status", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<?> updateUserStatus(@PathVariable int id,
                                              @RequestBody(required = false) StatusUpdateRequest request,
                                              @RequestParam(required = false) Boolean active,
                                              @RequestHeader(value = "Authorization", required = false) String authHeader,
                                              @RequestParam(required = false) Integer adminId) {
        ResponseEntity<?> authErr = verifyAdmin(authHeader, adminId);
        if (authErr != null) return authErr;

        Boolean targetActive = (request != null && request.getActive() != null) ? request.getActive() : active;
        User updated;
        if (targetActive == null) {
            updated = adminService.toggleUserActiveStatus(id);
        } else {
            updated = adminService.setUserActiveStatus(id, targetActive);
        }

        if (updated == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(errorBody("User not found with id: " + id));
        }
        return ResponseEntity.ok(updated);
    }

    /**
     * Toggle a user's active status.
     */
    @PutMapping("/users/{id}/toggle-status")
    public ResponseEntity<?> toggleUserStatus(@PathVariable int id,
                                              @RequestHeader(value = "Authorization", required = false) String authHeader,
                                              @RequestParam(required = false) Integer adminId) {
        ResponseEntity<?> authErr = verifyAdmin(authHeader, adminId);
        if (authErr != null) return authErr;

        User updated = adminService.toggleUserActiveStatus(id);
        if (updated == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(errorBody("User not found with id: " + id));
        }
        return ResponseEntity.ok(updated);
    }

    /**
     * Activate or deactivate a job.
     * Supports both request body { "active": boolean } and query param ?active=true/false.
     */
    @RequestMapping(value = "/jobs/{id}/status", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<?> updateJobStatus(@PathVariable int id,
                                             @RequestBody(required = false) StatusUpdateRequest request,
                                             @RequestParam(required = false) Boolean active,
                                             @RequestHeader(value = "Authorization", required = false) String authHeader,
                                             @RequestParam(required = false) Integer adminId) {
        ResponseEntity<?> authErr = verifyAdmin(authHeader, adminId);
        if (authErr != null) return authErr;

        Boolean targetActive = (request != null && request.getActive() != null) ? request.getActive() : active;
        Job updated;
        if (targetActive == null) {
            updated = adminService.toggleJobActiveStatus(id);
        } else {
            updated = adminService.setJobActiveStatus(id, targetActive);
        }

        if (updated == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(errorBody("Job not found with id: " + id));
        }
        return ResponseEntity.ok(updated);
    }

    /**
     * Toggle a job's active status.
     */
    @PutMapping("/jobs/{id}/toggle-status")
    public ResponseEntity<?> toggleJobStatus(@PathVariable int id,
                                             @RequestHeader(value = "Authorization", required = false) String authHeader,
                                             @RequestParam(required = false) Integer adminId) {
        ResponseEntity<?> authErr = verifyAdmin(authHeader, adminId);
        if (authErr != null) return authErr;

        Job updated = adminService.toggleJobActiveStatus(id);
        if (updated == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(errorBody("Job not found with id: " + id));
        }
        return ResponseEntity.ok(updated);
    }

    /**
     * View basic statistics.
     */
    @GetMapping({"/stats", "/statistics"})
    public ResponseEntity<?> getStats(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                      @RequestParam(required = false) Integer adminId) {
        ResponseEntity<?> authErr = verifyAdmin(authHeader, adminId);
        if (authErr != null) return authErr;

        return ResponseEntity.ok(adminService.getBasicStatistics());
    }

    private Map<String, Object> errorBody(String message) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", message);
        return error;
    }
}
