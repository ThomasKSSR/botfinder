package com.dissertation.gatewayservice.service;

import com.dissertation.contracts.events.AnalysisCompletedEvent;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JobStore {
    public enum Status { QUEUED, DONE, NOT_FOUND }

    public static class JobState {
        public Status status;
        public AnalysisCompletedEvent completed;
        public JobState(Status status) { this.status = status; }
    }

    private final Map<String, JobState> jobs = new ConcurrentHashMap<>();

    public void create(String jobId) { jobs.put(jobId, new JobState(Status.QUEUED)); }

    public void complete(AnalysisCompletedEvent event) {
        JobState state = jobs.getOrDefault(event.jobId(), new JobState(Status.NOT_FOUND));
        state.status = Status.DONE;
        state.completed = event;
        jobs.put(event.jobId(), state);
    }

    public JobState get(String jobId) {
        return jobs.getOrDefault(jobId, new JobState(Status.NOT_FOUND));
    }
}
