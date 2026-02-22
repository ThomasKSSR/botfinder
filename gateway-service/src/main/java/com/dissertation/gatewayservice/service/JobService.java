package com.dissertation.gatewayservice.service;


import com.dissertation.gatewayservice.dto.JobResultResponse;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JobService {
    private final Map<String, JobResultResponse> store = new ConcurrentHashMap<>();

    public String createJob(String url) {
        String jobId = UUID.randomUUID().toString();

        // For now: pretend processing is instant and return a mocked result
        List<Map<String, Object>> mocked = List.of(
                Map.of("user", "user123", "score", 0.82, "label", "bot-like", "reason", "repetitive comments"),
                Map.of("user", "john_doe", "score", 0.15, "label", "normal", "reason", "looks organic")
        );

        store.put(jobId, new JobResultResponse(jobId, "DONE", mocked));
        return jobId;
    }

    public JobResultResponse getJob(String jobId) {
        return store.getOrDefault(jobId, new JobResultResponse(jobId, "NOT_FOUND", List.of()));
    }
}
