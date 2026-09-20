package com.hiresphere;

import com.hiresphere.dto.StatusUpdateRequest;
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
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[*].email", hasItem("admin@careerhub.com")))
                .andExpect(jsonPath("$[*].email", hasItem("recruiter@techcorp.com")))
                .andExpect(jsonPath("$[*].email", hasItem("alice@example.com")));
    }

    @Test
    public void testGetCandidates() throws Exception {
        mockMvc.perform(get("/api/admin/candidates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[*].role", everyItem(is("CANDIDATE"))))
                .andExpect(jsonPath("$[*].email", hasItem("alice@example.com")));
    }

    @Test
    public void testGetRecruiters() throws Exception {
        mockMvc.perform(get("/api/admin/recruiters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[*].role", everyItem(is("RECRUITER"))))
                .andExpect(jsonPath("$[*].email", hasItem("recruiter@techcorp.com")));
    }

    @Test
    public void testGetAllJobs() throws Exception {
        mockMvc.perform(get("/api/admin/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[*].title", hasItem("Java Backend Developer")));
    }

    @Test
    public void testGetAllApplications() throws Exception {
        mockMvc.perform(get("/api/admin/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].jobTitle", is("Java Backend Developer")));
    }

    @Test
    public void testActivateDeactivateUser() throws Exception {
        // Deactivate user 3 (Alice)
        StatusUpdateRequest deactivateReq = new StatusUpdateRequest(false);
        mockMvc.perform(put("/api/admin/users/3/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deactivateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.active", is(false)));

        // Reactivate user 3 (Alice)
        StatusUpdateRequest activateReq = new StatusUpdateRequest(true);
        mockMvc.perform(put("/api/admin/users/3/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(activateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    public void testToggleUserStatus() throws Exception {
        // Toggle user 2 (Rahul)
        mockMvc.perform(put("/api/admin/users/2/toggle-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.active", is(false)));

        // Toggle back to active
        mockMvc.perform(put("/api/admin/users/2/toggle-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    public void testActivateDeactivateJob() throws Exception {
        // Deactivate Job 2
        StatusUpdateRequest deactivateReq = new StatusUpdateRequest(false);
        mockMvc.perform(put("/api/admin/jobs/2/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deactivateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.active", is(false)))
                .andExpect(jsonPath("$.status", is("CLOSED")));

        // Reactivate Job 2
        StatusUpdateRequest activateReq = new StatusUpdateRequest(true);
        mockMvc.perform(put("/api/admin/jobs/2/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(activateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.active", is(true)))
                .andExpect(jsonPath("$.status", is("OPEN")));
    }

    @Test
    public void testToggleJobStatus() throws Exception {
        // Toggle job 3
        mockMvc.perform(put("/api/admin/jobs/3/toggle-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.active", is(false)));

        // Toggle back
        mockMvc.perform(put("/api/admin/jobs/3/toggle-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    public void testGetBasicStatistics() throws Exception {
        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers", greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.totalCandidates", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.totalRecruiters", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.totalJobs", greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.activeJobs", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.totalApplications", greaterThanOrEqualTo(1)));
    }

    @Test
    public void testUserNotFoundForStatusUpdate() throws Exception {
        mockMvc.perform(put("/api/admin/users/999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\": false}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("User not found with id: 999")));
    }

    @Test
    public void testJobNotFoundForStatusUpdate() throws Exception {
        mockMvc.perform(put("/api/admin/jobs/999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\": false}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("Job not found with id: 999")));
    }
}
