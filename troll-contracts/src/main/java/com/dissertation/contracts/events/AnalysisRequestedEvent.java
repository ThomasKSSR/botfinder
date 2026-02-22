package com.dissertation.contracts.events;

import java.time.Instant;

public class AnalysisRequestedEvent {
    private String jobId;
    private String url;
    private Instant requestedAt;

    public AnalysisRequestedEvent() {}

    public AnalysisRequestedEvent(String jobId, String url, Instant requestedAt) {
        this.jobId = jobId;
        this.url = url;
        this.requestedAt = requestedAt;
    }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public Instant getRequestedAt() { return requestedAt; }
    public void setRequestedAt(Instant requestedAt) { this.requestedAt = requestedAt; }
}
