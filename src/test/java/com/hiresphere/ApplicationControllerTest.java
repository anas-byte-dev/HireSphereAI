package com.hiresphere;

import com.hiresphere.dto.ApplicationRequest;
import com.hiresphere.dto.ApplicationStatusUpdateRequest;
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
public class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCandidateCanApplyForJob() throws Exception {
        // Candidate 3 applies for job 2 (React Frontend Intern)
        ApplicationRequest req = new ApplicationRequest(3, 2, "I have great experience with React!");
        req.setResumeUrl("https://example.com/resume.pdf");

        String json = objectMapper.writeValueAsString(req);

        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.candidateId", is(3)))
                .andExpect(jsonPath("$.jobId", is(2)))
                .andExpect(jsonPath("$.status", is("APPLIED")))
                .andExpect(jsonPath("$.jobTitle", is("React Frontend Intern")))
                .andExpect(jsonPath("$.company", is("TechCorp Solutions")))
                .andExpect(jsonPath("$.candidateName", is("Alice Fernandes")));
    }

    @Test
    public void testPreventDuplicateApplication() throws Exception {
        // Candidate 3 has already applied to job 1 in sample data
        ApplicationRequest req = new ApplicationRequest(3, 1, "Applying again");
        String json = objectMapper.writeValueAsString(req);

        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("already applied")));
    }

    @Test
    public void testCandidateCanViewTheirApplications() throws Exception {
        mockMvc.perform(get("/api/applications").param("candidateId", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].candidateId", is(3)))
                .andExpect(jsonPath("$[0].status", notNullValue()));
    }

    @Test
    public void testViewApplicationDetails() throws Exception {
        // Sample application 1 exists
        mockMvc.perform(get("/api/applications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.candidateId", is(3)))
                .andExpect(jsonPath("$.jobId", is(1)))
                .andExpect(jsonPath("$.status", notNullValue()));
    }

    @Test
    public void testRecruiterCanViewApplicants() throws Exception {
        mockMvc.perform(get("/api/applications").param("recruiterId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));
    }

    @Test
    public void testRecruiterCanUpdateApplicationStatusFlow() throws Exception {
        // 1. SHORTLISTED
        ApplicationStatusUpdateRequest req1 = new ApplicationStatusUpdateRequest("SHORTLISTED", 2);
        mockMvc.perform(put("/api/applications/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SHORTLISTED")));

        // 2. INTERVIEW
        ApplicationStatusUpdateRequest req2 = new ApplicationStatusUpdateRequest("INTERVIEW", 2);
        mockMvc.perform(put("/api/applications/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("INTERVIEW")));

        // 3. SELECTED
        ApplicationStatusUpdateRequest req3 = new ApplicationStatusUpdateRequest("SELECTED", 2);
        mockMvc.perform(put("/api/applications/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req3)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SELECTED")));

        // 4. REJECTED
        ApplicationStatusUpdateRequest req4 = new ApplicationStatusUpdateRequest("REJECTED", 2);
        mockMvc.perform(put("/api/applications/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req4)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("REJECTED")));
    }

    @Test
    public void testInvalidStatusRejected() throws Exception {
        ApplicationStatusUpdateRequest req = new ApplicationStatusUpdateRequest("INVALID_STATUS", 2);
        mockMvc.perform(put("/api/applications/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Allowed values are: APPLIED, SHORTLISTED, REJECTED, INTERVIEW, SELECTED")));
    }
}
