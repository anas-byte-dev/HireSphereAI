package com.hiresphere;

import com.hiresphere.dto.SavedJobRequest;
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
public class SavedJobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testViewCandidateSavedJobs() throws Exception {
        // Candidate 3 has saved job 2 in sample data
        mockMvc.perform(get("/api/saved-jobs?candidateId=3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].candidateId", is(3)))
                .andExpect(jsonPath("$[0].jobId", is(2)))
                .andExpect(jsonPath("$[0].jobTitle", notNullValue()));
    }

    @Test
    public void testViewCandidateSavedJobsByPath() throws Exception {
        mockMvc.perform(get("/api/saved-jobs/candidate/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].candidateId", is(3)));
    }

    @Test
    public void testCandidateCanSaveJobSuccessfully() throws Exception {
        // Candidate 3 saves job 1 (Java Backend Developer)
        SavedJobRequest req = new SavedJobRequest(3, 1);
        String json = objectMapper.writeValueAsString(req);

        mockMvc.perform(post("/api/saved-jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.candidateId", is(3)))
                .andExpect(jsonPath("$.jobId", is(1)))
                .andExpect(jsonPath("$.jobTitle", is("Java Backend Developer")))
                .andExpect(jsonPath("$.company", is("TechCorp Solutions")));
    }

    @Test
    public void testPreventDuplicateSavedJob() throws Exception {
        // Candidate 3 has already saved job 2 in sample data
        SavedJobRequest req = new SavedJobRequest(3, 2);
        String json = objectMapper.writeValueAsString(req);

        mockMvc.perform(post("/api/saved-jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("already saved")));
    }

    @Test
    public void testSaveNonExistentJobReturnsNotFound() throws Exception {
        SavedJobRequest req = new SavedJobRequest(3, 9999);
        String json = objectMapper.writeValueAsString(req);

        mockMvc.perform(post("/api/saved-jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Job not found")));
    }

    @Test
    public void testCheckIsJobSaved() throws Exception {
        // Job 2 is saved for candidate 3
        mockMvc.perform(get("/api/saved-jobs/check")
                        .param("candidateId", "3")
                        .param("jobId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.candidateId", is(3)))
                .andExpect(jsonPath("$.jobId", is(2)))
                .andExpect(jsonPath("$.isSaved", is(true)));

        // Job 999 is not saved
        mockMvc.perform(get("/api/saved-jobs/check")
                        .param("candidateId", "3")
                        .param("jobId", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSaved", is(false)));
    }

    @Test
    public void testRemoveSavedJobById() throws Exception {
        // Sample saved job has ID 1
        mockMvc.perform(delete("/api/saved-jobs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("removed successfully")));

        // Verify it is gone
        mockMvc.perform(get("/api/saved-jobs/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testRemoveSavedJobByCandidateAndJob() throws Exception {
        // Candidate 3 has saved job 2
        mockMvc.perform(delete("/api/saved-jobs")
                        .param("candidateId", "3")
                        .param("jobId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        // Verify check returns false now
        mockMvc.perform(get("/api/saved-jobs/check")
                        .param("candidateId", "3")
                        .param("jobId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSaved", is(false)));
    }
}
