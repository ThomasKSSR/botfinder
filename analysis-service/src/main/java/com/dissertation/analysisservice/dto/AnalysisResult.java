package com.dissertation.analysisservice.dto;

public class AnalysisResult {
    private String user;
    private double score;
    private String label;
    private String reason;

    public AnalysisResult(String user, double score, String label, String reason) {
        this.user = user;
        this.score = score;
        this.label = label;
        this.reason = reason;
    }

    public String getUser() { return user; }
    public double getScore() { return score; }
    public String getLabel() { return label; }
    public String getReason() { return reason; }
}
