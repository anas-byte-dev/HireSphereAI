package com.hiresphere;

import com.hiresphere.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = HireSphereApplication.class)
@AutoConfigureMockMvc
public class SecurityAndValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCandidateCannotCreateRecruiterProfile() throws Exception {
        // User 3 is Alice (CANDIDATE)
        RecruiterProfileRequest req = new RecruiterProfileRequest("Lead Recruiter", "+91-9999999999", "https://linkedin.com");
        mockMvc.perform(put("/api/recruiters/3/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", containsString("Only RECRUITER")));
    }

    @Test
    public void testRecruiterCannotCreateCandidateProfile() throws Exception {
        // User 2 is Rahul (RECRUITER)
        CandidateProfileRequest req = new CandidateProfileRequest();
        req.setHeadline("Fake Candidate");
        mockMvc.perform(put("/api/candidates/2/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", containsString("Only CANDIDATE")));
    }

    @Test
    public void testCandidateCannotPostJob() throws Exception {
        // User 3 is Alice (CANDIDATE)
        JobRequest req = new JobRequest();
        req.setTitle("Unauthorized Job");
        req.setCompany("Rogue Co");

        mockMvc.perform(post("/api/jobs")
                        .param("recruiterId", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", containsString("Only RECRUITER")));
    }

    @Test
    public void testRecruiterCannotApplyForJob() throws Exception {
        // User 2 is Rahul (RECRUITER)
        ApplicationRequest req = new ApplicationRequest(2, 1, "Recruiter trying to apply");
        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", containsString("Only users with CANDIDATE")));
    }

    @Test
    public void testRecruiterCannotSaveJob() throws Exception {
        // User 2 is Rahul (RECRUITER)
        SavedJobRequest req = new SavedJobRequest(2, 1);
        mockMvc.perform(post("/api/saved-jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("Only users with CANDIDATE")));
    }

    @Test
    public void testCrossRecruiterJobUpdateUnauthorized() throws Exception {
        // Job 1 belongs to recruiter 2
        // Register another recruiter (id = 4 or higher)
        RegisterRequest reg = new RegisterRequest("Intruder Recruiter", "intruder@recruiter.com", "pass123", "RECRUITER");
        String res = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        int intruderId = objectMapper.readTree(res).get("userId").asInt();

        // Intruder tries to update recruiter 2's job
        JobRequest updateReq = new JobRequest();
        updateReq.setTitle("Hijacked Job Title");
        mockMvc.perform(put("/api/jobs/1")
                        .param("recruiterId", String.valueOf(intruderId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", containsString("not authorized to update this job")));
    }

    @Test
    public void testNonExistentJobReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/jobs/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("Job not found")));

        mockMvc.perform(delete("/api/jobs/99999").param("recruiterId", "2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("Job not found")));
    }

    @Test
    public void testNonExistentUserReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/candidates/99999/profile"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("User not found")));

        mockMvc.perform(get("/api/recruiters/99999/profile"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("User not found")));
    }

    @Test
    public void testNonExistentApplicationReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/applications/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("Application not found")));
    }

    @Test
    public void testAdminAccessVerification() throws Exception {
        // User 3 is Alice (CANDIDATE) -> trying to access admin endpoint with adminId=3 should be 403
        mockMvc.perform(get("/api/admin/users").param("adminId", "3"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", containsString("Admin role required")));

        // User 1 is Admin -> 200 OK
        mockMvc.perform(get("/api/admin/users").param("adminId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));

        // Invalid token -> 401 Unauthorized
        mockMvc.perform(get("/api/admin/users").header("Authorization", "Bearer invalid-token-12345"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", containsString("Invalid or expired session token")));
    }
}
