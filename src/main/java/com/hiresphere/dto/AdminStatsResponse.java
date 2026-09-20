package com.hiresphere.dto;

/**
 * AdminStatsResponse - DTO for returning basic system statistics to Admin.
 *
 * Statistics:
 * - totalUsers: Count of all registered users
 * - totalCandidates: Count of candidate users
 * - totalRecruiters: Count of recruiter users
 * - totalJobs: Count of all job postings
 * - activeJobs: Count of active/open job postings
 * - totalApplications: Count of job applications submitted
 */
public class AdminStatsResponse {

    private long totalUsers;
    private long totalCandidates;
    private long totalRecruiters;
    private long totalJobs;
    private long activeJobs;
    private long totalApplications;

    public AdminStatsResponse() {}

    public AdminStatsResponse(long totalUsers, long totalCandidates, long totalRecruiters,
                              long totalJobs, long activeJobs, long totalApplications) {
        this.totalUsers = totalUsers;
        this.totalCandidates = totalCandidates;
        this.totalRecruiters = totalRecruiters;
        this.totalJobs = totalJobs;
        this.activeJobs = activeJobs;
        this.totalApplications = totalApplications;
    }

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public long getTotalCandidates() { return totalCandidates; }
    public void setTotalCandidates(long totalCandidates) { this.totalCandidates = totalCandidates; }

    public long getTotalRecruiters() { return totalRecruiters; }
    public void setTotalRecruiters(long totalRecruiters) { this.totalRecruiters = totalRecruiters; }

    public long getTotalJobs() { return totalJobs; }
    public void setTotalJobs(long totalJobs) { this.totalJobs = totalJobs; }

    public long getActiveJobs() { return activeJobs; }
    public void setActiveJobs(long activeJobs) { this.activeJobs = activeJobs; }

    public long getTotalApplications() { return totalApplications; }
    public void setTotalApplications(long totalApplications) { this.totalApplications = totalApplications; }
}
