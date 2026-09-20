package com.hiresphere.model;

import java.time.LocalDateTime;

/**
 * AiChatMessage - Represents a conversational turn in an AI Mock Interview session.
 * Saved in real-time into the persistent database.
 */
public class AiChatMessage {

    private int id;
    private String sessionId;
    private int candidateId;
    private String jobRole;
    private String sender; // "AI" or "CANDIDATE"
    private String message;
    private String feedback; // Real-time feedback / coaching tip from AI
    private int score; // 0-100 rating for candidate's answer
    private LocalDateTime timestamp = LocalDateTime.now();

    public AiChatMessage() {
        this.timestamp = LocalDateTime.now();
    }

    public AiChatMessage(String sessionId, int candidateId, String jobRole, String sender, String message) {
        this.sessionId = sessionId;
        this.candidateId = candidateId;
        this.jobRole = jobRole;
        this.sender = sender;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public int getCandidateId() { return candidateId; }
    public void setCandidateId(int candidateId) { this.candidateId = candidateId; }

    public String getJobRole() { return jobRole; }
    public void setJobRole(String jobRole) { this.jobRole = jobRole; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
