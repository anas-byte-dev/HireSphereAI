package com.hiresphere.dto;

/**
 * ApplicationRequest - DTO for candidate submitting a job application.
 */
public class ApplicationRequest {

    private Integer candidateId;
    private Integer jobId;
    private String coverLetter;
    private String resumeUrl;

    public ApplicationRequest() {}

    public ApplicationRequest(Integer candidateId, Integer jobId, String coverLetter) {
        this.candidateId = candidateId;
        this.jobId = jobId;
        this.coverLetter = coverLetter;
    }

    public Integer getCandidateId() { return candidateId; }
    public void setCandidateId(Integer candidateId) { this.candidateId = candidateId; }

    public Integer getJobId() { return jobId; }
    public void setJobId(Integer jobId) { this.jobId = jobId; }

    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }

    public String getResumeUrl() { return resumeUrl; }
    public void setResumeUrl(String resumeUrl) { this.resumeUrl = resumeUrl; }
}
