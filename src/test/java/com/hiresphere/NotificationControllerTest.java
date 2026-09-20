package com.hiresphere;

import com.hiresphere.dto.ApplicationRequest;
import com.hiresphere.dto.ApplicationStatusUpdateRequest;
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
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testUserCanViewTheirNotifications() throws Exception {
        // Candidate 3 has notifications in sample data
        mockMvc.perform(get("/api/notifications?userId=3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].userId", is(3)))
                .andExpect(jsonPath("$[0].message", notNullValue()))
                .andExpect(jsonPath("$[0].date", notNullValue()))
                .andExpect(jsonPath("$[0].read", notNullValue()));

        // Path-variable alternative
        mockMvc.perform(get("/api/notifications/user/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].userId", is(3)));
    }

    @Test
    public void testMarkNotificationAsRead() throws Exception {
        // Notification 1 in sample data
        mockMvc.perform(put("/api/notifications/1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.read", is(true)));

        // Verify status persists
        mockMvc.perform(get("/api/notifications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read", is(true)));
    }

    @Test
    public void testMarkAllNotificationsAsReadForUser() throws Exception {
        mockMvc.perform(put("/api/notifications/user/3/read-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.userId", is(3)));

        // Verify all are read
        mockMvc.perform(get("/api/notifications/user/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].read", everyItem(is(true))));
    }

    @Test
    public void testApplicationSubmissionGeneratesNotifications() throws Exception {
        // Candidate 3 applies to job 2 (posted by recruiter 2)
        ApplicationRequest appReq = new ApplicationRequest(3, 2, "Excited about this role!");
        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appReq)))
                .andExpect(status().isCreated());

        // Check candidate 3 received APPLICATION_SUBMITTED notification
        mockMvc.perform(get("/api/notifications/user/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].type", hasItem("APPLICATION_SUBMITTED")));

        // Check recruiter 2 received NEW_APPLICANT notification
        mockMvc.perform(get("/api/notifications/user/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].type", hasItem("NEW_APPLICANT")));
    }

    @Test
    public void testApplicationStatusChangeGeneratesNotification() throws Exception {
        // Recruiter 2 updates application 1 to SHORTLISTED
        ApplicationStatusUpdateRequest req = new ApplicationStatusUpdateRequest("SHORTLISTED", 2);
        mockMvc.perform(put("/api/applications/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        // Check candidate 3 received status changed notification
        mockMvc.perform(get("/api/notifications/user/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].type", hasItem("APPLICATION_STATUS_CHANGED")))
                .andExpect(jsonPath("$[*].message", hasItem(containsString("SHORTLISTED"))));
    }

    @Test
    public void testInterviewSchedulingAndUpdatingGeneratesNotifications() throws Exception {
        // 1. Schedule Interview for Application 1
        InterviewRequest schedReq = new InterviewRequest(
                1,
                "2026-10-10",
                "03:30 PM",
                "ONLINE",
                "https://meet.google.com/test-meet"
        );
        schedReq.setCandidateId(3);
        schedReq.setRecruiterId(2);

        mockMvc.perform(post("/api/interviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(schedReq)))
                .andExpect(status().isCreated());

        // Check INTERVIEW_SCHEDULED notification
        mockMvc.perform(get("/api/notifications/user/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].type", hasItem("INTERVIEW_SCHEDULED")));

        // 2. Update Interview
        InterviewUpdateRequest updateReq = new InterviewUpdateRequest(
                "2026-10-12",
                "05:00 PM",
                "ONLINE",
                "https://meet.google.com/test-meet",
                "SCHEDULED"
        );

        mockMvc.perform(put("/api/interviews/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk());

        // Check INTERVIEW_UPDATED notification
        mockMvc.perform(get("/api/notifications/user/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].type", hasItem("INTERVIEW_UPDATED")));
    }
}
