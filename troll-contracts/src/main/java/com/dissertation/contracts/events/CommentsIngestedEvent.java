package com.dissertation.contracts.events;

import java.util.List;

public record CommentsIngestedEvent(
        String jobId,
        String platform,
        List<IngestedComment> comments
) {}
