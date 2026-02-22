package com.dissertation.analysisservice.controller;

import com.dissertation.analysisservice.dto.AnalysisResult;
import com.dissertation.analysisservice.dto.AnalyzeCommentsRequest;
import com.dissertation.analysisservice.dto.AnalyzeCommentsResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AnalysisController {

    @PostMapping("/analyze-comments")
    public AnalyzeCommentsResponse analyze(@Valid @RequestBody AnalyzeCommentsRequest req) {

        var results = List.of(
                new AnalysisResult("user123", 0.82, "bot-like", "repetitive comments"),
                new AnalysisResult("john_doe", 0.15, "normal", "looks organic")
        );
        return new AnalyzeCommentsResponse(results);
    }
}
