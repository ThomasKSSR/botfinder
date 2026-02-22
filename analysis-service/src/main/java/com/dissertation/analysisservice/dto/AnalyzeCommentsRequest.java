package com.dissertation.analysisservice.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class AnalyzeCommentsRequest {
    @NotEmpty
    private List<String> comments;

    public List<String> getComments() { return comments; }
    public void setComments(List<String> comments) { this.comments = comments; }
}
