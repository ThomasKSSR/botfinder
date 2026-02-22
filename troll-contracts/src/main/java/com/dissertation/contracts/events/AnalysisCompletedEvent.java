package com.dissertation.contracts.events;

import java.time.Instant;
import java.util.List;

public class AnalysisCompletedEvent {
    private String jobId;
    private Instant completedAt;
    private List<ResultItem> results;

    public AnalysisCompletedEvent() {}

    public AnalysisCompletedEvent(String jobId, Instant completedAt, List<ResultItem> results) {
        this.jobId = jobId;
        this.completedAt = completedAt;
        this.results = results;
    }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public List<ResultItem> getResults() { return results; }
    public void setResults(List<ResultItem> results) { this.results = results; }

    public static class ResultItem {
        private String user;
        private double score;
        private String label;
        private String reason;

        public ResultItem() {}

        public ResultItem(String user, double score, String label, String reason) {
            this.user = user;
            this.score = score;
            this.label = label;
            this.reason = reason;
        }

        public String getUser() { return user; }
        public void setUser(String user) { this.user = user; }

        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
