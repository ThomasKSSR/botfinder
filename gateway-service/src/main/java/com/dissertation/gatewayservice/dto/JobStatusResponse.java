package com.dissertation.gatewayservice.dto;

public class JobStatusResponse {
    private String jobId;
    private String status;

    public JobStatusResponse() {}

    public JobStatusResponse(String jobId, String status) {
        this.jobId = jobId;
        this.status = status;
    }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
