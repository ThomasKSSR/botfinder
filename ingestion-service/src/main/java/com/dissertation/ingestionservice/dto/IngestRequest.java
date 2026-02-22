package com.dissertation.ingestionservice.dto;

import jakarta.validation.constraints.NotBlank;

public class IngestRequest {
    @NotBlank
    private String url;

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
