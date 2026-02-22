package com.dissertation.gatewayservice.dto;

import java.util.List;
import java.util.Map;

public class JobResultResponse {
    private String jobId;
    private String status;
    private List<Map<String, Object>> results;

    public JobResultResponse() {}

    public JobResultResponse(String jobId, String status, List<Map<String, Object>> results) {
        this.jobId = jobId;
        this.status = status;
        this.results = results;
    }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<Map<String, Object>> getResults() { return results; }
    public void setResults(List<Map<String, Object>> results) { this.results = results; }
}
