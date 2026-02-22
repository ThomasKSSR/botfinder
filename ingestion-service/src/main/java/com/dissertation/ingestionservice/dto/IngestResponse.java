package com.dissertation.ingestionservice.dto;

import java.util.List;

public class IngestResponse {
    private String platform;
    private List<String> comments;

    public IngestResponse(String platform, List<String> comments) {
        this.platform = platform;
        this.comments = comments;
    }

    public String getPlatform() { return platform; }
    public List<String> getComments() { return comments; }
}
