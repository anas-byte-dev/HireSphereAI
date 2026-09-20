package com.hiresphere.controller;

import com.hiresphere.ai.GeminiAiService;
import com.hiresphere.model.AiAnalysis;
import com.hiresphere.model.AiChatMessage;
import com.hiresphere.store.DataStore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * AiController - REST endpoints for HireSphere Autonomous Agentic AI.
 */
@RestController
@RequestMapping("/api/ai")
@Tag(name = "Agentic AI", description = "Autonomous screening, mock interviews, and AI job generation")
public class AiController {

    private final GeminiAiService geminiAiService;
    private final DataStore dataStore;

    public AiController(GeminiAiService geminiAiService, DataStore dataStore) {
        this.geminiAiService = geminiAiService;
        this.dataStore = dataStore;
    }

    @PostMapping("/screen")
    @Operation(summary = "Autonomous Candidate Screening", description = "Analyzes candidate suitability for a job and saves structured evaluation to database.")
    public ResponseEntity<AiAnalysis> screenCandidate(@RequestParam int candidateId, @RequestParam int jobId) {
        AiAnalysis analysis = geminiAiService.screenCandidate(candidateId, jobId);
        return ResponseEntity.ok(analysis);
    }

    @GetMapping("/analysis/job/{jobId}")
    @Operation(summary = "Get AI analyses for a job")
    public ResponseEntity<List<AiAnalysis>> getAnalysesForJob(@PathVariable int jobId) {
        return ResponseEntity.ok(dataStore.findAiAnalysesByJobId(jobId));
    }

    @GetMapping("/analysis/candidate/{candidateId}")
    @Operation(summary = "Get AI analyses for a candidate")
    public ResponseEntity<List<AiAnalysis>> getAnalysesForCandidate(@PathVariable int candidateId) {
        return ResponseEntity.ok(dataStore.findAiAnalysesByCandidateId(candidateId));
    }

    @PostMapping("/interview/start")
    @Operation(summary = "Start AI Mock Interview", description = "Initiates an interactive mock interview with opening question and tips.")
    public ResponseEntity<AiChatMessage> startInterview(
            @RequestParam int candidateId,
            @RequestParam(defaultValue = "Full-Stack Software Engineer") String jobRole) {
        AiChatMessage opening = geminiAiService.startInterviewSession(candidateId, jobRole);
        return ResponseEntity.ok(opening);
    }

    @PostMapping("/interview/message")
    @Operation(summary = "Send Candidate Response in Interview", description = "Submits candidate answer, returns AI evaluation, score, and next question.")
    public ResponseEntity<AiChatMessage> sendInterviewMessage(@RequestBody Map<String, Object> req) {
        String sessionId = (String) req.get("sessionId");
        int candidateId = req.get("candidateId") instanceof Number ? ((Number) req.get("candidateId")).intValue() : 0;
        String jobRole = (String) req.getOrDefault("jobRole", "Software Engineer");
        String message = (String) req.getOrDefault("message", "");

        if (sessionId == null || message.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        AiChatMessage response = geminiAiService.handleInterviewTurn(sessionId, candidateId, jobRole, message);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/interview/session/{sessionId}")
    @Operation(summary = "Get Interview Transcript", description = "Fetches full conversation history and scores for an interview session.")
    public ResponseEntity<List<AiChatMessage>> getSessionTranscript(@PathVariable String sessionId) {
        return ResponseEntity.ok(dataStore.findAiChatMessagesBySessionId(sessionId));
    }

    @PostMapping("/generate-job")
    @Operation(summary = "Generate Job Spec with AI", description = "Generates high-converting description, requirements, and skills for recruiters.")
    public ResponseEntity<Map<String, Object>> generateJob(@RequestBody Map<String, Object> req) {
        String title = (String) req.getOrDefault("title", "Software Developer");
        String experience = (String) req.getOrDefault("experience", "Fresher");
        String location = (String) req.getOrDefault("location", "Remote");
        @SuppressWarnings("unchecked")
        List<String> targetSkills = req.get("targetSkills") instanceof List
                ? (List<String>) req.get("targetSkills")
                : Collections.emptyList();

        Map<String, Object> spec = geminiAiService.generateJobDescription(title, experience, location, targetSkills);
        return ResponseEntity.ok(spec);
    }
}
