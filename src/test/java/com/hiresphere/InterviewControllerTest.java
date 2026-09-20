package com.hiresphere;

import com.hiresphere.dto.InterviewRequest;
import com.hiresphere.dto.InterviewUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = HireSphereApplication.class)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class InterviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testRecruiterCanScheduleInterview() throws Exception {
        // Application 1 exists in sample data
        InterviewRequest req = new InterviewRequest(
                1,
                "2026-10-01",
                "02:00 PM",
                "ONLINE",
                "https://meet.google.com/abc-defg-hij"
        );
        req.setNotes("Round 1: Core Java and Spring concepts");

        mockMvc.perform(post("/api/interviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.applicationId", is(1)))
                .andExpect(jsonPath("$.candidateId", is(3)))
                .andExpect(jsonPath("$.recruiterId", is(2)))
                .andExpect(jsonPath("$.date", is("2026-10-01")))
                .andExpect(jsonPath("$.time", is("02:00 PM")))
                .andExpect(jsonPath("$.mode", is("ONLINE")))
                .andExpect(jsonPath("$.meetingLink", is("https://meet.google.com/abc-defg-hij")))
                .andExpect(jsonPath("$.status", is("SCHEDULED")))
                .andExpect(jsonPath("$.jobTitle", is("Java Backend Developer")))
                .andExpect(jsonPath("$.candidateName", is("Alice Fernandes")));

        // Verify application status updated to INTERVIEW
        mockMvc.perform(get("/api/applications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("INTERVIEW")));
    }

    @Test
    public void testCandidateCanViewScheduledInterviews() throws Exception {
        // Sample data has Interview 1 for candidate 3
        mockMvc.perform(get("/api/interviews/candidate/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].candidateId", is(3)))
                .andExpect(jsonPath("$[0].date", notNullValue()))
                .andExpect(jsonPath("$[0].time", notNullValue()))
                .andExpect(jsonPath("$[0].meetingLink", notNullValue()))
                .andExpect(jsonPath("$[0].status", is("SCHEDULED")));

        // Query param version
        mockMvc.perform(get("/api/interviews?candidateId=3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].candidateId", is(3)));
    }

    @Test
    public void testRecruiterCanViewInterviews() throws Exception {
        // Sample data has Interview 1 for recruiter 2
        mockMvc.perform(get("/api/interviews/recruiter/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].recruiterId", is(2)))
                .andExpect(jsonPath("$[0].candidateName", is("Alice Fernandes")));

        // Query param version
        mockMvc.perform(get("/api/interviews?recruiterId=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].recruiterId", is(2)));
    }

    @Test
    public void testRecruiterCanUpdateInterview() throws Exception {
        // Update sample Interview 1
        InterviewUpdateRequest updateReq = new InterviewUpdateRequest(
                "2026-10-05",
                "04:30 PM",
                "ONLINE",
                "https://meet.google.com/rescheduled-link",
                "COMPLETED"
        );
        updateReq.setNotes("Candidate performed exceptionally well.");

        mockMvc.perform(put("/api/interviews/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.date", is("2026-10-05")))
                .andExpect(jsonPath("$.time", is("04:30 PM")))
                .andExpect(jsonPath("$.status", is("COMPLETED")))
                .andExpect(jsonPath("$.meetingLink", is("https://meet.google.com/rescheduled-link")))
                .andExpect(jsonPath("$.notes", containsString("exceptionally well")));
    }

    @Test
    public void testScheduleWithNonExistentApplicationReturns404() throws Exception {
        InterviewRequest req = new InterviewRequest(
                9999,
                "2026-10-01",
                "10:00 AM",
                "ONLINE",
                "https://meet.google.com/test"
        );

        mockMvc.perform(post("/api/interviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Application not found")));
    }

    @Test
    public void testUpdateWithInvalidStatusReturns400() throws Exception {
        InterviewUpdateRequest updateReq = new InterviewUpdateRequest();
        updateReq.setStatus("INVALID_STATUS");

        mockMvc.perform(put("/api/interviews/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Invalid status")));
    }

    @Test
    public void testGetInterviewById() throws Exception {
        mockMvc.perform(get("/api/interviews/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.applicationId", is(1)))
                .andExpect(jsonPath("$.candidateName", is("Alice Fernandes")));
    }
}
