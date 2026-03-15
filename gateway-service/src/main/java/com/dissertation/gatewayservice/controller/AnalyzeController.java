package com.dissertation.gatewayservice.controller;


import com.dissertation.contracts.events.AnalysisRequestedEvent;
import com.dissertation.contracts.messaging.MessagingConstants;
import com.dissertation.gatewayservice.service.JobStore;
import jakarta.validation.constraints.NotBlank;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class AnalyzeController {

    private final RabbitTemplate rabbit;
    private final JobStore store;

    public AnalyzeController(RabbitTemplate rabbit, JobStore store) {
        this.rabbit = rabbit;
        this.store = store;
    }

    public static class AnalyzeRequest { @NotBlank public String url; }
    public static class AnalyzeResponse {
        public String jobId; public String status;
        public AnalyzeResponse(String jobId, String status) { this.jobId = jobId; this.status = status; }
    }

    @PostMapping("/analyze")
    public AnalyzeResponse analyze(@RequestBody AnalyzeRequest req) {
        String jobId = UUID.randomUUID().toString();
        store.create(jobId);

        rabbit.convertAndSend(
                MessagingConstants.EXCHANGE,
                MessagingConstants.RK_ANALYSIS_REQUESTED,
                new AnalysisRequestedEvent(jobId, req.url, Instant.now())
        );

        return new AnalyzeResponse(jobId, "QUEUED");
    }

    @GetMapping("/analyze/{jobId}")
    public Object get(@PathVariable String jobId) {
        var state = store.get(jobId);
        if (state.status == JobStore.Status.DONE) return state.completed;
        return new Object() { public final String id = jobId; public final String status = state.status.name(); };
    }
}
