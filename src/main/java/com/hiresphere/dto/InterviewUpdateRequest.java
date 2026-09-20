package com.hiresphere.dto;

/**
 * InterviewUpdateRequest - DTO for updating an interview's schedule, link, or status.
 */
public class InterviewUpdateRequest {

    private String date;
    private String time;
    private String mode;
    private String meetingLink;
    private String notes;
    private String status;

    public InterviewUpdateRequest() {}

    public InterviewUpdateRequest(String date, String time, String mode, String meetingLink, String status) {
        this.date = date;
        this.time = time;
        this.mode = mode;
        this.meetingLink = meetingLink;
        this.status = status;
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
