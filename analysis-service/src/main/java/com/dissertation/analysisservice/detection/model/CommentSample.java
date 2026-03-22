package com.dissertation.analysisservice.detection.model;
import com.dissertation.contracts.events.IngestedComment;

import java.util.List;
import java.util.Map;

public record CommentSample(
        int index,
        IngestedComment original,
        String normalized,
        Map<String, Integer> normalizedCounts,
        Map<String, Integer> authorCounts,
        List<String> allNormalizedTexts,
        List<IngestedComment> allComments
) {}
