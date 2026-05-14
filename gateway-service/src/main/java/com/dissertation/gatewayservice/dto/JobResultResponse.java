package com.dissertation.gatewayservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class JobResultResponse {
    @JsonProperty("jobId")
    private String jobId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("completedAt")
    private String completedAt;

    @JsonProperty("results")
    private List<?> results;

    public JobResultResponse() {}

    public JobResultResponse(String jobId, String status, String completedAt, List<?> results) {
        this.jobId = jobId;
        this.status = status;
        this.completedAt = completedAt;
        this.results = results;
    }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCompletedAt() { return completedAt; }
    public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }

    public List<?> getResults() { return results; }
    public void setResults(List<?> results) { this.results = results; }
}