package com.hiresphere.dto;

/**
 * ApplicationStatusUpdateRequest - DTO for recruiter updating application status.
 */
public class ApplicationStatusUpdateRequest {

    private String status;
    private Integer recruiterId;

    public ApplicationStatusUpdateRequest() {}

    public ApplicationStatusUpdateRequest(String status, Integer recruiterId) {
        this.status = status;
        this.recruiterId = recruiterId;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getRecruiterId() { return recruiterId; }
    public void setRecruiterId(Integer recruiterId) { this.recruiterId = recruiterId; }
}
