package com.dissertation.contracts.events;

import java.time.Instant;

public record IngestedComment(
        String commentId,
        String authorId,
        String authorName,
        String text,
        Instant publishedAt,
        long likeCount
) {}
