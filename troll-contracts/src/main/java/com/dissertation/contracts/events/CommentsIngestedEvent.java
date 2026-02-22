package com.dissertation.contracts.events;

import java.util.List;

public class CommentsIngestedEvent {
    private String jobId;
    private String platform;
    private List<String> comments;

    public CommentsIngestedEvent() {}

    public CommentsIngestedEvent(String jobId, String platform, List<String> comments) {
        this.jobId = jobId;
        this.platform = platform;
        this.comments = comments;
    }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public List<String> getComments() { return comments; }
    public void setComments(List<String> comments) { this.comments = comments; }
}
