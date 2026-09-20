package com.hiresphere.dto;

/**
 * InterviewRequest - DTO for scheduling an interview.
 *
 * Fields:
 *   applicationId (required)
 *   candidateId (optional, derived from application if not provided)
 *   recruiterId (optional, derived from application/job if not provided)
 *   date (required, e.g. "2026-09-25")
 *   time (required, e.g. "11:00 AM")
 *   mode (e.g. "ONLINE" or "OFFLINE")
 *   meetingLink (link or venue)
 *   notes (instructions)
 *   status (default "SCHEDULED")
 */
public class InterviewRequest {

    private Integer applicationId;
    private Integer candidateId;
    private Integer recruiterId;
    private String date;
    private String time;
    private String mode;
    private String meetingLink;
    private String notes;
    private String status;

    public InterviewRequest() {}

    public InterviewRequest(Integer applicationId, String date, String time, String mode, String meetingLink) {
        this.applicationId = applicationId;
        this.date = date;
        this.time = time;
        this.mode = mode;
        this.meetingLink = meetingLink;
    }

    public Integer getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Integer applicationId) {
        this.applicationId = applicationId;
    }

    public Integer getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Integer candidateId) {
        this.candidateId = candidateId;
    }

    public Integer getRecruiterId() {
        return recruiterId;
    }

    public void setRecruiterId(Integer recruiterId) {
        this.recruiterId = recruiterId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getMeetingLink() {
        return meetingLink;
    }

    public void setMeetingLink(String meetingLink) {
        this.meetingLink = meetingLink;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
