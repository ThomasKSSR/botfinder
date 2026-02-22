package com.dissertation.gatewayservice.controller;


import com.dissertation.gatewayservice.dto.AnalyzeRequest;
import com.dissertation.gatewayservice.dto.AnalyzeResponse;
import com.dissertation.gatewayservice.dto.JobResultResponse;
import com.dissertation.gatewayservice.service.JobService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AnalyzeController {

    private final JobService jobService;

    public AnalyzeController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<AnalyzeResponse> analyze(@Valid @RequestBody AnalyzeRequest request) {
        String jobId = jobService.createJob(request.getUrl());
        return ResponseEntity.ok(new AnalyzeResponse(jobId, "DONE"));
    }

    @GetMapping("/analyze/{jobId}")
    public ResponseEntity<JobResultResponse> getResult(@PathVariable String jobId) {
        return ResponseEntity.ok(jobService.getJob(jobId));
    }
}
