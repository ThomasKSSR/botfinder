package com.dissertation.contracts.events;

import java.time.Instant;

public record AnalysisRequestedEvent(
        String jobId,
        String url,
        Instant requestedAt,
        Integer maxComments
) {}

