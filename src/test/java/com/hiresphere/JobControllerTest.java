package com.hiresphere;

import com.hiresphere.dto.JobRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = HireSphereApplication.class)
@AutoConfigureMockMvc
public class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCandidateCanViewActiveJobs() throws Exception {
        // GET /api/jobs
        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].title", notNullValue()))
                .andExpect(jsonPath("$[0].company", notNullValue()))
                .andExpect(jsonPath("$[0].active", is(true)));
    }

    @Test
    public void testRecruiterCanViewTheirJobs() throws Exception {
        // GET /api/jobs?recruiterId=2
        mockMvc.perform(get("/api/jobs").param("recruiterId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].recruiterId", is(2)));
    }

    @Test
    public void testViewJobDetails() throws Exception {
        // GET /api/jobs/1
        mockMvc.perform(get("/api/jobs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Java Backend Developer")))
                .andExpect(jsonPath("$.company", is("TechCorp Solutions")))
                .andExpect(jsonPath("$.location", is("Bangalore, India")))
                .andExpect(jsonPath("$.employmentType", is("FULL_TIME")))
                .andExpect(jsonPath("$.salaryRange", is("₹4-6 LPA")))
                .andExpect(jsonPath("$.experience", is("Fresher / 0-1 year")))
                .andExpect(jsonPath("$.skills", hasItem("Java")))
                .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    public void testViewJobNotFound() throws Exception {
        // GET /api/jobs/9999
        mockMvc.perform(get("/api/jobs/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("not found")));
    }

    @Test
    public void testSearchByKeyword() throws Exception {
        // GET /api/jobs?keyword=java
        mockMvc.perform(get("/api/jobs").param("keyword", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[*].title", hasItem(containsString("Java"))));
    }

    @Test
    public void testSearchByLocation() throws Exception {
        // GET /api/jobs?location=bangalore
        mockMvc.perform(get("/api/jobs").param("location", "bangalore"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].location", containsStringIgnoringCase("Bangalore")));
    }

    @Test
    public void testSearchByJobType() throws Exception {
        // GET /api/jobs?jobType=INTERNSHIP
        mockMvc.perform(get("/api/jobs").param("jobType", "INTERNSHIP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].employmentType", is("INTERNSHIP")));
    }

    @Test
    public void testSearchByExperience() throws Exception {
        // GET /api/jobs?experience=fresher
        mockMvc.perform(get("/api/jobs").param("experience", "fresher"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].experience", containsStringIgnoringCase("fresher")));
    }

    @Test
    public void testSearchBySkills() throws Exception {
        // GET /api/jobs?skills=react
        mockMvc.perform(get("/api/jobs").param("skills", "react"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].skills", hasItem("React")));
    }

    @Test
    public void testSearchCombinedFilters() throws Exception {
        // GET /api/jobs?keyword=java&location=bangalore
        mockMvc.perform(get("/api/jobs")
                        .param("keyword", "java")
                        .param("location", "bangalore"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].title", containsStringIgnoringCase("Java")))
                .andExpect(jsonPath("$[0].location", containsStringIgnoringCase("Bangalore")));
    }

    @Test
    public void testInactiveJobsExcludedFromSearch() throws Exception {
        // Create an inactive job
        JobRequest inactiveJob = new JobRequest();
        inactiveJob.setRecruiterId(2);
        inactiveJob.setTitle("Hidden Inactive Position");
        inactiveJob.setLocation("Delhi, India");
        inactiveJob.setEmploymentType("FULL_TIME");
        inactiveJob.setActive(false);

        String inactiveJson = objectMapper.writeValueAsString(inactiveJob);

        String resp = mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inactiveJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        int inactiveId = objectMapper.readTree(resp).get("id").asInt();

        // Search by keyword "Hidden"
        mockMvc.perform(get("/api/jobs").param("keyword", "Hidden"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", empty()));

        // Clean up
        mockMvc.perform(delete("/api/jobs/" + inactiveId).param("recruiterId", "2"))
                .andExpect(status().isOk());
    }

    @Test
    public void testJobCrudFlow() throws Exception {
        // 1. Create a job: POST /api/jobs
        JobRequest createReq = new JobRequest();
        createReq.setRecruiterId(2);
        createReq.setTitle("Junior DevOps Engineer");
        createReq.setCompany("TechCorp Cloud");
        createReq.setLocation("Hyderabad, India");
        createReq.setEmploymentType("FULL_TIME");
        createReq.setSalaryRange("₹6-8 LPA");
        createReq.setExperience("0-1 years");
        createReq.setSkills(Arrays.asList("Linux", "Docker", "CI/CD", "AWS"));
        createReq.setDescription("Looking for a junior DevOps engineer to maintain CI/CD pipelines.");
        createReq.setRequirements("Basic understanding of Linux, Docker, Git, Cloud concepts.");
        createReq.setActive(true);

        String createJson = objectMapper.writeValueAsString(createReq);

        String responseString = mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title", is("Junior DevOps Engineer")))
                .andExpect(jsonPath("$.company", is("TechCorp Cloud")))
                .andExpect(jsonPath("$.location", is("Hyderabad, India")))
                .andExpect(jsonPath("$.employmentType", is("FULL_TIME")))
                .andExpect(jsonPath("$.salaryRange", is("₹6-8 LPA")))
                .andExpect(jsonPath("$.experience", is("0-1 years")))
                .andExpect(jsonPath("$.skills", hasItem("Docker")))
                .andExpect(jsonPath("$.recruiterId", is(2)))
                .andExpect(jsonPath("$.active", is(true)))
                .andReturn().getResponse().getContentAsString();

        int createdId = objectMapper.readTree(responseString).get("id").asInt();

        // 2. View created job details: GET /api/jobs/{id}
        mockMvc.perform(get("/api/jobs/" + createdId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(createdId)))
                .andExpect(jsonPath("$.title", is("Junior DevOps Engineer")));

        // 3. Update job: PUT /api/jobs/{id}
        JobRequest updateReq = new JobRequest();
        updateReq.setRecruiterId(2);
        updateReq.setTitle("Lead DevOps Engineer");
        updateReq.setSalaryRange("₹8-10 LPA");
        updateReq.setActive(true);

        String updateJson = objectMapper.writeValueAsString(updateReq);

        mockMvc.perform(put("/api/jobs/" + createdId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(createdId)))
                .andExpect(jsonPath("$.title", is("Lead DevOps Engineer")))
                .andExpect(jsonPath("$.salaryRange", is("₹8-10 LPA")));

        // 4. Delete job: DELETE /api/jobs/{id}?recruiterId=2
        mockMvc.perform(delete("/api/jobs/" + createdId)
                        .param("recruiterId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("deleted successfully")));

        // 5. Verify deleted job is not found
        mockMvc.perform(get("/api/jobs/" + createdId))
                .andExpect(status().isNotFound());
    }
}
