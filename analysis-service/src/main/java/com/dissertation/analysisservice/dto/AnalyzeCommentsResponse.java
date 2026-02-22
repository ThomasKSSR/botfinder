package com.dissertation.analysisservice.dto;

import java.util.List;
import java.util.Map;

public class AnalyzeCommentsResponse {
    private List<AnalysisResult> results;

    public AnalyzeCommentsResponse(List<AnalysisResult> results) {
        this.results = results;
    }

    public List<AnalysisResult> getResults() { return results; }
}
