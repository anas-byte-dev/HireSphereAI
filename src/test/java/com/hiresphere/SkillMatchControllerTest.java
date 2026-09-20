package com.hiresphere;

import com.hiresphere.dto.SkillMatchRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = HireSphereApplication.class)
@AutoConfigureMockMvc
public class SkillMatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testUserPromptSkillMatchExample() throws Exception {
        // Candidate: Java, SQL, HTML, CSS
        // Job: Java, SQL, Spring Boot, React
        // Matching: Java, SQL
        // Percentage: 50.0%
        SkillMatchRequest request = new SkillMatchRequest(
                Arrays.asList("Java", "SQL", "HTML", "CSS"),
                Arrays.asList("Java", "SQL", "Spring Boot", "React")
        );

        mockMvc.perform(post("/api/skill-match/compare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.candidateSkills", contains("Java", "SQL", "HTML", "CSS")))
                .andExpect(jsonPath("$.requiredJobSkills", contains("Java", "SQL", "Spring Boot", "React")))
                .andExpect(jsonPath("$.matchingSkills", containsInAnyOrder("Java", "SQL")))
                .andExpect(jsonPath("$.missingSkills", containsInAnyOrder("Spring Boot", "React")))
                .andExpect(jsonPath("$.matchPercentage", is(50.0)))
                .andExpect(jsonPath("$.matchSummary", containsString("2 out of 4 required skills matched (50.0%)")));
    }

    @Test
    public void testCaseInsensitiveSkillMatching() throws Exception {
        SkillMatchRequest request = new SkillMatchRequest(
                Arrays.asList("java", "sql", "spring boot"),
                Arrays.asList("JAVA", "SQL", "Spring Boot")
        );

        mockMvc.perform(post("/api/skill-match/compare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matchingSkills", hasSize(3)))
                .andExpect(jsonPath("$.missingSkills", empty()))
                .andExpect(jsonPath("$.matchPercentage", is(100.0)));
    }

    @Test
    public void testZeroSkillMatching() throws Exception {
        SkillMatchRequest request = new SkillMatchRequest(
                Arrays.asList("Python", "Django"),
                Arrays.asList("Rust", "Go", "C++")
        );

        mockMvc.perform(post("/api/skill-match/compare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matchingSkills", empty()))
                .andExpect(jsonPath("$.missingSkills", hasSize(3)))
                .andExpect(jsonPath("$.matchPercentage", is(0.0)));
    }

    @Test
    public void testMatchStoredCandidateWithJob() throws Exception {
        // Candidate 3: [Java, Spring Boot, React, MySQL, REST API, Git]
        // Job 1: [Java, Spring Boot, REST API, MySQL]
        // All 4 required skills matched -> 100.0%
        mockMvc.perform(get("/api/skill-match")
                        .param("candidateId", "3")
                        .param("jobId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.candidateId", is(3)))
                .andExpect(jsonPath("$.jobId", is(1)))
                .andExpect(jsonPath("$.jobTitle", is("Java Backend Developer")))
                .andExpect(jsonPath("$.matchingSkills", containsInAnyOrder("Java", "Spring Boot", "REST API", "MySQL")))
                .andExpect(jsonPath("$.missingSkills", empty()))
                .andExpect(jsonPath("$.matchPercentage", is(100.0)));

        // Job 2: [React, JavaScript, HTML, CSS]
        // Matching: React (1 out of 4 -> 25.0%)
        mockMvc.perform(get("/api/skill-match")
                        .param("candidateId", "3")
                        .param("jobId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.candidateId", is(3)))
                .andExpect(jsonPath("$.jobId", is(2)))
                .andExpect(jsonPath("$.jobTitle", is("React Frontend Intern")))
                .andExpect(jsonPath("$.matchingSkills", contains("React")))
                .andExpect(jsonPath("$.missingSkills", containsInAnyOrder("JavaScript", "HTML", "CSS")))
                .andExpect(jsonPath("$.matchPercentage", is(25.0)));
    }

    @Test
    public void testMatchStoredCandidateWithJobByPath() throws Exception {
        mockMvc.perform(get("/api/skill-match/candidate/3/job/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.candidateId", is(3)))
                .andExpect(jsonPath("$.jobId", is(1)));
    }

    @Test
    public void testCandidateNotFoundReturns404() throws Exception {
        mockMvc.perform(get("/api/skill-match")
                        .param("candidateId", "9999")
                        .param("jobId", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Candidate not found")));
    }

    @Test
    public void testJobNotFoundReturns404() throws Exception {
        mockMvc.perform(get("/api/skill-match")
                        .param("candidateId", "3")
                        .param("jobId", "9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Job not found")));
    }
}
