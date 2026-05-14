package com.dissertation.gatewayservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;

public class AnalyzeRequest {
    @NotBlank
    private String url;
    
    @Min(value = 1, message = "maxComments must be at least 1")
    private Integer maxComments;

    public AnalyzeRequest() {}

    public AnalyzeRequest(String url) {
        this.url = url;
    }

    public AnalyzeRequest(String url, Integer maxComments) {
        this.url = url;
        this.maxComments = maxComments;
    }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    
    public Integer getMaxComments() { return maxComments; }
    public void setMaxComments(Integer maxComments) { this.maxComments = maxComments; }
}
