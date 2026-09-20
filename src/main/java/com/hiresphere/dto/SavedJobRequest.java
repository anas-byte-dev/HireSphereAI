package com.hiresphere.dto;

public class SavedJobRequest {

    private int candidateId;
    private int jobId;

    public SavedJobRequest() {}

    public SavedJobRequest(int candidateId, int jobId) {
        this.candidateId = candidateId;
        this.jobId = jobId;
    }

    public int getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(int candidateId) {
        this.candidateId = candidateId;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }
}
