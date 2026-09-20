package com.hiresphere;

import com.hiresphere.dto.LoginRequest;
import com.hiresphere.dto.RegisterRequest;
import com.hiresphere.store.DataStore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = HireSphereApplication.class)
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataStore dataStore;

    @Test
    public void testCandidateRegistrationSuccess() throws Exception {
        RegisterRequest req = new RegisterRequest("Bob Builder", "bob.builder@example.com", "secret123", "CANDIDATE");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", notNullValue()))
                .andExpect(jsonPath("$.email", is("bob.builder@example.com")))
                .andExpect(jsonPath("$.role", is("CANDIDATE")))
                .andExpect(jsonPath("$.token", notNullValue()));
    }

    @Test
    public void testRecruiterRegistrationSuccess() throws Exception {
        RegisterRequest req = new RegisterRequest("Maya HR", "maya.recruiter@example.com", "recruit123", "RECRUITER");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", notNullValue()))
                .andExpect(jsonPath("$.email", is("maya.recruiter@example.com")))
                .andExpect(jsonPath("$.role", is("RECRUITER")))
                .andExpect(jsonPath("$.token", notNullValue()));
    }

    @Test
    public void testDuplicateEmailRegistrationFails() throws Exception {
        // alice@example.com already exists in sample data
        RegisterRequest req = new RegisterRequest("Alice Duplicate", "alice@example.com", "anotherpass", "CANDIDATE");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("already exists")));
    }

    @Test
    public void testAdminSelfRegistrationForbidden() throws Exception {
        RegisterRequest req = new RegisterRequest("Hacker Admin", "hacker@example.com", "adminpass", "ADMIN");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Role must be CANDIDATE or RECRUITER")));
    }

    @Test
    public void testInvalidEmailFormatRegistrationFails() throws Exception {
        RegisterRequest req = new RegisterRequest("Bad Email", "not-an-email", "pass123", "CANDIDATE");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("valid email address")));
    }

    @Test
    public void testShortPasswordRegistrationFails() throws Exception {
        RegisterRequest req = new RegisterRequest("Short Pass", "short.pass@example.com", "12", "CANDIDATE");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("at least 4 characters")));
    }

    @Test
    public void testLoginSuccessAndCurrentUserMe() throws Exception {
        LoginRequest req = new LoginRequest("alice@example.com", "candidate123");
        String resJson = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("alice@example.com")))
                .andExpect(jsonPath("$.token", notNullValue()))
                .andReturn().getResponse().getContentAsString();

        JsonNode node = objectMapper.readTree(resJson);
        String token = node.get("token").asText();

        // Check GET /api/auth/me with Bearer token
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("alice@example.com")))
                .andExpect(jsonPath("$.role", is("CANDIDATE")));

        // Logout
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("Logged out successfully")));

        // Try /api/auth/me again after logout -> 401
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", containsString("Invalid or expired session token")));
    }

    @Test
    public void testLoginIncorrectPasswordFails() throws Exception {
        LoginRequest req = new LoginRequest("alice@example.com", "wrong-password");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", containsString("Incorrect password")));
    }

    @Test
    public void testLoginNonExistentEmailFails() throws Exception {
        LoginRequest req = new LoginRequest("nobody@example.com", "secret123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", containsString("No account found")));
    }

    @Test
    public void testDeactivatedUserLoginFailsWithForbidden() throws Exception {
        // Create deactivated user directly
        com.hiresphere.model.User deactivated = new com.hiresphere.model.User("Banned", "banned@example.com", "banned123", "CANDIDATE");
        deactivated.setActive(false);
        dataStore.addUser(deactivated);

        LoginRequest req = new LoginRequest("banned@example.com", "banned123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", containsString("deactivated")));
    }
}
