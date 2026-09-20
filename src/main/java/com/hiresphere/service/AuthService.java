package com.hiresphere.service;

import com.hiresphere.dto.AuthResponse;
import com.hiresphere.dto.LoginRequest;
import com.hiresphere.dto.RegisterRequest;
import com.hiresphere.model.CandidateProfile;
import com.hiresphere.model.Company;
import com.hiresphere.model.RecruiterProfile;
import com.hiresphere.model.User;
import com.hiresphere.store.DataStore;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * AuthService - Handles all authentication logic for CareerHub.
 *
 * Two main operations:
 *   1. register() - Creates a new user account (CANDIDATE or RECRUITER)
 *   2. login()    - Verifies credentials and returns a session token
 *
 * How session tokens work (simple version):
 *   - On login, we generate a UUID string (e.g. "abc-123-xyz")
 *   - We store it in DataStore.activeSessions: token → userId
 *   - We return the token to the frontend
 *   - Frontend saves the token in localStorage
 *   - For protected routes (future), frontend sends token in the header
 *
 * Returns null on failure — the Controller converts null to HTTP 400/401.
 */
@Service
public class AuthService {

    private final DataStore dataStore;

    // Constructor injection — Spring provides the shared DataStore bean
    public AuthService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REGISTER
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Registers a new CANDIDATE or RECRUITER.
     *
     * Steps:
     *   1. Validate required fields are not empty
     *   2. Validate role is CANDIDATE or RECRUITER (ADMIN cannot self-register)
     *   3. Check that email is not already in use
     *   4. Create and save the User
     *   5. Create an empty profile (CandidateProfile or RecruiterProfile)
     *   6. Generate a session token and return AuthResponse
     *
     * @return AuthResponse on success, null on failure (with error message set)
     */
    public AuthResponse register(RegisterRequest request, StringBuilder errorMsg) {

        // 1. Validate required fields
        if (isBlank(request.getName())) {
            errorMsg.append("Name is required.");
            return null;
        }
        if (isBlank(request.getEmail())) {
            errorMsg.append("Email is required.");
            return null;
        }
        if (!request.getEmail().contains("@") || !request.getEmail().contains(".")) {
            errorMsg.append("Please provide a valid email address.");
            return null;
        }
        if (isBlank(request.getPassword())) {
            errorMsg.append("Password is required.");
            return null;
        }
        if (request.getPassword().trim().length() < 4) {
            errorMsg.append("Password must be at least 4 characters.");
            return null;
        }
        if (isBlank(request.getRole())) {
            errorMsg.append("Role is required.");
            return null;
        }

        // 2. Validate role — only CANDIDATE or RECRUITER can self-register
        String role = request.getRole().toUpperCase();
        if (!role.equals("CANDIDATE") && !role.equals("RECRUITER")) {
            errorMsg.append("Role must be CANDIDATE or RECRUITER.");
            return null;
        }

        // 3. Check for duplicate email (case-insensitive)
        String emailLower = request.getEmail().toLowerCase().trim();
        boolean emailTaken = dataStore.getUsers().stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(emailLower));
        if (emailTaken) {
            errorMsg.append("An account with this email already exists.");
            return null;
        }

        // 4. Create and save User
        User newUser = new User(
                request.getName().trim(),
                emailLower,
                request.getPassword(),
                role
        );
        dataStore.addUser(newUser);

        // 5. Create an empty profile based on role
        if (role.equals("CANDIDATE")) {
            CandidateProfile profile = new CandidateProfile();
            profile.setUserId(newUser.getId());
            dataStore.addCandidateProfile(profile);

        } else {
            // RECRUITER — also create a placeholder Company
            Company company = new Company();
            company.setName("My Company");
            company.setLocation("Not specified");
            dataStore.addCompany(company);

            RecruiterProfile profile = new RecruiterProfile();
            profile.setUserId(newUser.getId());
            profile.setCompanyId(company.getId());
            dataStore.addRecruiterProfile(profile);
        }

        // 6. Generate session token and return response
        String token = UUID.randomUUID().toString();
        dataStore.saveSession(token, newUser.getId());

        System.out.println("[AuthService] New user registered: " + newUser.getEmail() + " | Role: " + role);

        return new AuthResponse(
                newUser.getId(),
                newUser.getName(),
                newUser.getEmail(),
                newUser.getRole(),
                token,
                "Registration successful! Welcome to HireSphere AI."
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LOGIN
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Logs in an existing user (any role: CANDIDATE, RECRUITER, ADMIN).
     *
     * Steps:
     *   1. Validate required fields
     *   2. Find user by email (case-insensitive)
     *   3. Check password matches
     *   4. Check account is active (Admin can deactivate accounts)
     *   5. Generate a new session token and return AuthResponse
     *
     * @return AuthResponse on success, null on failure
     */
    public AuthResponse login(LoginRequest request, StringBuilder errorMsg) {

        // 1. Validate fields
        if (isBlank(request.getEmail())) {
            errorMsg.append("Email is required.");
            return null;
        }
        if (isBlank(request.getPassword())) {
            errorMsg.append("Password is required.");
            return null;
        }

        // 2. Find user by email
        String emailLower = request.getEmail().toLowerCase().trim();
        User foundUser = dataStore.getUsers().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(emailLower) ||
                        ("admin@hiresphere.ai".equalsIgnoreCase(emailLower) && "admin@careerhub.com".equalsIgnoreCase(u.getEmail())) ||
                        ("admin@careerhub.com".equalsIgnoreCase(emailLower) && "admin@hiresphere.ai".equalsIgnoreCase(u.getEmail())))
                .findFirst()
                .orElse(null);

        if (foundUser == null) {
            errorMsg.append("No account found with this email.");
            return null;
        }

        // 3. Check password
        if (!foundUser.getPassword().equals(request.getPassword())) {
            errorMsg.append("Incorrect password.");
            return null;
        }

        // 4. Check if account is active
        if (!foundUser.isActive()) {
            errorMsg.append("Your account has been deactivated. Please contact admin.");
            return null;
        }

        // 5. Generate session token and return response
        String token = UUID.randomUUID().toString();
        dataStore.saveSession(token, foundUser.getId());

        System.out.println("[AuthService] User logged in: " + foundUser.getEmail() + " | Role: " + foundUser.getRole());

        return new AuthResponse(
                foundUser.getId(),
                foundUser.getName(),
                foundUser.getEmail(),
                foundUser.getRole(),
                token,
                "Login successful! Welcome back, " + foundUser.getName() + "."
        );
    }

    /**
     * Resolve active user by session token.
     */
    public User getUserByToken(String token) {
        if (token == null || token.isBlank()) return null;
        String cleanToken = token.startsWith("Bearer ") ? token.substring(7).trim() : token.trim();
        int userId = dataStore.getUserIdByToken(cleanToken);
        if (userId <= 0) return null;
        return dataStore.findUserById(userId);
    }

    /**
     * Invalidate session token (logout).
     */
    public boolean logout(String token) {
        if (token == null || token.isBlank()) return false;
        String cleanToken = token.startsWith("Bearer ") ? token.substring(7).trim() : token.trim();
        if (dataStore.isValidToken(cleanToken)) {
            dataStore.removeSession(cleanToken);
            return true;
        }
        return false;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────────────────────────────────

    /** Returns true if the string is null or contains only whitespace. */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
