package com.dissertation.contracts.events;

import java.time.Instant;
import java.util.List;

public record AnalysisCompletedEvent(
        String jobId,
        Instant completedAt,
        List<ResultItem> results
) {
    public record ResultItem(
            String commentId,
            String authorName,
            double score,
            String label,
            String reason,
            String commentPreview
    ) {}
}
