package com.hiresphere.ai;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiresphere.model.*;
import com.hiresphere.realtime.RealtimeEventService;
import com.hiresphere.store.DataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

/**
 * GeminiAiService - Autonomous Agentic AI system for HireSphere.
 * Supports Google Gemini API (Free Tier) with intelligent built-in fallback engine.
 * 100% free and functional without any paid cloud dependencies.
 */
@Service
public class GeminiAiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiAiService.class);

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent}")
    private String geminiApiUrl;

    private final DataStore dataStore;
    private final RealtimeEventService realtimeEventService;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final RestTemplate restTemplate = new RestTemplate();

    public GeminiAiService(DataStore dataStore, RealtimeEventService realtimeEventService) {
        this.dataStore = dataStore;
        this.realtimeEventService = realtimeEventService;
    }

    /**
     * Reports live status and configuration of the Gemini AI engine.
     */
    public Map<String, Object> getAiStatus() {
        boolean hasKey = geminiApiKey != null && !geminiApiKey.isBlank();
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("configured", hasKey);
        status.put("engine", hasKey ? "Google Gemini 1.5 Flash (Live LLM)" : "HireSphere Autonomous Heuristic Engine");
        status.put("model", "gemini-1.5-flash");
        status.put("endpoint", geminiApiUrl);
        status.put("status", "HEALTHY");
        return status;
    }

    /**
     * Agent 1: Autonomous Resume & Candidate Screening Agent.
     */
    public AiAnalysis screenCandidate(int candidateId, int jobId) {
        User candidate = dataStore.findUserById(candidateId);
        CandidateProfile profile = dataStore.findCandidateProfileByUserId(candidateId);
        Job job = dataStore.findJobById(jobId);

        if (job == null || candidate == null) {
            // If candidate profile not in in-memory store, build a transient candidate
            if (candidate == null) {
                candidate = new User("Candidate #" + candidateId, "candidate" + candidateId + "@hiresphere.io", "", "CANDIDATE");
                candidate.setId(candidateId);
            }
            if (job == null) {
                throw new IllegalArgumentException("Job not found with ID: " + jobId);
            }
        }

        List<String> jobSkills = job.getSkills() != null ? job.getSkills() : Collections.emptyList();
        List<String> candidateSkills = (profile != null && profile.getSkills() != null)
                ? profile.getSkills()
                : Collections.emptyList();

        AiAnalysis analysis;

        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            analysis = callGeminiForScreening(candidate, profile, job, candidateSkills, jobSkills);
        } else {
            analysis = autonomousBuiltinScreening(candidate, profile, job, candidateSkills, jobSkills);
        }

        analysis.setCandidateId(candidateId);
        analysis.setJobId(jobId);

        // Save into persistent real-time database
        dataStore.addAiAnalysis(analysis);

        // Broadcast real-time event
        realtimeEventService.broadcast(
                "AI_ANALYSIS_COMPLETED",
                "AI_ANALYSIS",
                analysis.getId(),
                "AI screening completed for candidate " + candidate.getName() + " on job '" + job.getTitle() + "' (Score: " + analysis.getScore() + "%)",
                analysis
        );

        return analysis;
    }

    /**
     * Agent 2: Interactive AI Mock Interviewer & Coach.
     */
    public AiChatMessage handleInterviewTurn(String sessionId, int candidateId, String jobRole, String userMessage) {
        // Record Candidate message
        AiChatMessage candidateMsg = new AiChatMessage(sessionId, candidateId, jobRole, "CANDIDATE", userMessage);
        dataStore.addAiChatMessage(candidateMsg);

        // Generate AI Interviewer response & evaluation
        AiChatMessage aiResponse;
        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            aiResponse = callGeminiForInterviewTurn(sessionId, candidateId, jobRole, userMessage);
        } else {
            aiResponse = autonomousBuiltinInterviewTurn(sessionId, candidateId, jobRole, userMessage);
        }

        dataStore.addAiChatMessage(aiResponse);

        // Broadcast real-time event
        realtimeEventService.broadcast(
                "AI_MESSAGE_RECEIVED",
                "AI_INTERVIEW",
                aiResponse.getId(),
                "AI feedback generated for interview session " + sessionId,
                aiResponse
        );

        return aiResponse;
    }

    /**
     * Initiates a new mock interview session for a candidate with role-specific opening questions.
     */
    public AiChatMessage startInterviewSession(int candidateId, String jobRole) {
        String sessionId = UUID.randomUUID().toString();
        String greeting = "Hello! I am your HireSphere AI Interview Coach. I'll be conducting your mock interview for the "
                + jobRole + " position today. To kick things off: Could you briefly introduce yourself and highlight a project where you solved a difficult technical challenge?";

        AiChatMessage welcomeMsg = new AiChatMessage(sessionId, candidateId, jobRole, "AI", greeting);
        welcomeMsg.setFeedback("Tip: Use the STAR method (Situation, Task, Action, Result) when answering technical and behavioral questions.");
        welcomeMsg.setScore(100);

        dataStore.addAiChatMessage(welcomeMsg);
        return welcomeMsg;
    }

    /**
     * Agent 3: Autonomous Job Description & Rubric Generator for recruiters.
     */
    public Map<String, Object> generateJobDescription(String title, String experience, String location, List<String> targetSkills) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            try {
                String prompt = "You are an expert tech recruiter. Generate a compelling job description for: "
                        + "Title: " + title + ", Experience: " + experience + ", Location: " + location
                        + ", Target Skills: " + String.join(", ", targetSkills)
                        + ". Return valid JSON with keys: description (string), requirements (string), recommendedSalary (string), suggestedSkills (array of strings).";

                String rawResponse = callGeminiApi(prompt);
                String cleaned = extractJsonText(rawResponse);
                if (cleaned != null) {
                    Map<String, Object> parsed = objectMapper.readValue(cleaned, Map.class);
                    parsed.put("generatedBy", "Google Gemini 1.5 Flash");
                    return parsed;
                }
            } catch (Exception e) {
                log.warn("Gemini generation failed, falling back to built-in generator: {}", e.getMessage());
            }
        }

        // Built-in intelligent generator
        result.put("title", title);
        result.put("description", "We are looking for a passionate " + title + " to join our dynamic team in " + location + ". You will design, build, and scale mission-critical systems while collaborating with top-tier engineers.");
        result.put("requirements", "- Proven problem-solving skills and clean code practices.\n- Hands-on experience with " + (targetSkills.isEmpty() ? "modern technologies" : String.join(", ", targetSkills)) + ".\n- Ability to work effectively in cross-functional agile teams.\n- Strong communication and analytical mindset.");
        result.put("recommendedSalary", experience.equalsIgnoreCase("Fresher") ? "₹5 - 8 LPA" : "₹12 - 22 LPA");
        result.put("suggestedSkills", targetSkills.isEmpty() ? List.of("Java", "Spring Boot", "React", "REST API", "SQL", "Git") : targetSkills);
        result.put("generatedBy", "HireSphere Heuristic Engine (Offline)");

        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Internal Autonomous Heuristic & LLM Implementation
    // ─────────────────────────────────────────────────────────────────────────

    private AiAnalysis autonomousBuiltinScreening(User candidate, CandidateProfile profile, Job job, List<String> candidateSkills, List<String> jobSkills) {
        AiAnalysis analysis = new AiAnalysis();

        Set<String> cSkillsLower = new HashSet<>();
        for (String s : candidateSkills) cSkillsLower.add(s.trim().toLowerCase());

        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (String js : jobSkills) {
            if (cSkillsLower.contains(js.trim().toLowerCase())) {
                matched.add(js);
            } else {
                missing.add(js);
            }
        }

        int score;
        if (jobSkills.isEmpty()) {
            score = 75;
        } else {
            score = Math.min(100, (int) Math.round(((double) matched.size() / jobSkills.size()) * 100));
        }

        analysis.setScore(score);
        analysis.setStrengths(matched.isEmpty() ? List.of("Relevant educational foundation (" + (profile != null ? profile.getEducation() : "Engineering") + ")") : matched);
        analysis.setSkillGaps(missing.isEmpty() ? List.of("No major skill gaps identified against baseline requirements.") : missing);

        if (score >= 80) {
            analysis.setMatchVerdict("EXCELLENT_FIT");
            analysis.setRecommendedAction("Fast-track to technical interview.");
        } else if (score >= 50) {
            analysis.setMatchVerdict("GOOD_FIT");
            analysis.setRecommendedAction("Review portfolio and proceed to preliminary screening.");
        } else {
            analysis.setMatchVerdict("POTENTIAL_MATCH");
            analysis.setRecommendedAction("Consider for junior role or provide training opportunities.");
        }

        analysis.setReasoning("Candidate demonstrates competencies in " + (matched.isEmpty() ? "general engineering principles" : String.join(", ", matched))
                + ". Targeted interview suggested around missing competencies: " + (missing.isEmpty() ? "advanced architecture design" : String.join(", ", missing)) + ".");

        List<String> questions = new ArrayList<>();
        questions.add("How have you applied " + (matched.isEmpty() ? "your core technical stack" : matched.get(0)) + " in past production or academic projects?");
        if (!missing.isEmpty()) {
            questions.add("How would you quickly ramp up on " + missing.get(0) + " to meet this role's demands?");
        }
        questions.add("Describe a situation where you encountered a critical production bug and how you diagnosed it.");
        analysis.setSuggestedInterviewQuestions(questions);

        return analysis;
    }

    private AiChatMessage autonomousBuiltinInterviewTurn(String sessionId, int candidateId, String jobRole, String userMessage) {
        AiChatMessage msg = new AiChatMessage();
        msg.setSessionId(sessionId);
        msg.setCandidateId(candidateId);
        msg.setJobRole(jobRole);
        msg.setSender("AI");

        int wordCount = userMessage.trim().split("\\s+").length;
        int rating = Math.min(95, Math.max(50, 60 + wordCount * 2));
        msg.setScore(rating);

        String feedback;
        String nextQuestion;

        if (wordCount < 10) {
            feedback = "Your answer was very concise. Try expanding with specific technical decisions and outcomes using concrete examples.";
            nextQuestion = "Can you elaborate on the architecture of that solution? What tradeoffs did you consider?";
        } else if (userMessage.toLowerCase().contains("challenge") || userMessage.toLowerCase().contains("problem") || userMessage.toLowerCase().contains("bug")) {
            feedback = "Great problem-solving narrative! You clearly highlighted the complication and how you addressed it.";
            nextQuestion = "Excellent. Now switching gears to scalability: If your application experienced a 10x traffic surge tomorrow, which component would bottleneck first and how would you optimize it?";
        } else {
            feedback = "Solid explanation! You conveyed key concepts well. To make it even stronger, quantify the business or performance impact.";
            nextQuestion = "Thank you for that explanation. In a fast-moving team, how do you balance code quality, test coverage, and tight delivery deadlines?";
        }

        msg.setFeedback(feedback);
        msg.setMessage(nextQuestion);
        return msg;
    }

    private AiAnalysis callGeminiForScreening(User candidate, CandidateProfile profile, Job job, List<String> cSkills, List<String> jSkills) {
        try {
            String prompt = "You are an autonomous AI recruiter. Evaluate this candidate for this job:\n"
                    + "Candidate: " + candidate.getName() + ", Education: " + (profile != null ? profile.getEducation() : "N/A") + ", Skills: " + String.join(", ", cSkills) + "\n"
                    + "Job: " + job.getTitle() + ", Required Skills: " + String.join(", ", jSkills) + ", Description: " + job.getDescription() + "\n"
                    + "Output ONLY JSON with keys: score (integer 0-100), matchVerdict (string), strengths (array of strings), skillGaps (array of strings), reasoning (string), recommendedAction (string), suggestedInterviewQuestions (array of strings).";

            String raw = callGeminiApi(prompt);
            String jsonText = extractJsonText(raw);
            if (jsonText != null) {
                return objectMapper.readValue(jsonText, AiAnalysis.class);
            }
        } catch (Exception e) {
            log.warn("Gemini call failed for screening, falling back to autonomous heuristic: {}", e.getMessage());
        }
        return autonomousBuiltinScreening(candidate, profile, job, cSkills, jSkills);
    }

    private AiChatMessage callGeminiForInterviewTurn(String sessionId, int candidateId, String jobRole, String userMessage) {
        try {
            String prompt = "You are a senior hiring manager conducting an interactive technical mock interview for a '" + jobRole + "' position.\n"
                    + "The candidate just said: \"" + userMessage + "\"\n"
                    + "Respond in strict JSON with keys:\n"
                    + "1. score (integer 0-100 rating their answer)\n"
                    + "2. feedback (1-2 sentences of constructive coaching tip)\n"
                    + "3. message (your conversational reply and next insightful interview question)";

            String raw = callGeminiApi(prompt);
            String jsonText = extractJsonText(raw);
            if (jsonText != null) {
                JsonNode parsed = objectMapper.readTree(jsonText);
                AiChatMessage msg = new AiChatMessage();
                msg.setSessionId(sessionId);
                msg.setCandidateId(candidateId);
                msg.setJobRole(jobRole);
                msg.setSender("AI");
                msg.setScore(parsed.path("score").asInt(80));
                msg.setFeedback(parsed.path("feedback").asText());
                msg.setMessage(parsed.path("message").asText());
                return msg;
            }
        } catch (Exception e) {
            log.warn("Gemini interview turn failed, falling back: {}", e.getMessage());
        }
        return autonomousBuiltinInterviewTurn(sessionId, candidateId, jobRole, userMessage);
    }

    private String extractJsonText(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) return null;
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            String text = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            if (text == null || text.isBlank()) return null;

            // Strip markdown code fences if wrapped
            String cleaned = text.trim();
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.replaceFirst("^```(?:json)?\\n?", "").replaceFirst("\\n?```$", "").trim();
            }

            int start = cleaned.indexOf("{");
            int end = cleaned.lastIndexOf("}") + 1;
            if (start != -1 && end > start) {
                return cleaned.substring(start, end);
            }
            return cleaned;
        } catch (Exception e) {
            log.warn("Could not extract JSON from response: {}", e.getMessage());
            return null;
        }
    }

    private String callGeminiApi(String prompt) {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY is not configured.");
        }
        String endpoint = geminiApiUrl + "?key=" + geminiApiKey;

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(endpoint, entity, String.class);
        return response.getBody();
    }
}
