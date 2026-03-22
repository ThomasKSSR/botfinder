package com.dissertation.analysisservice.detection.features;

import com.dissertation.analysisservice.detection.model.CommentSample;

public class RepeatedAuthorFeature implements FeatureExtractor {

    @Override
    public FeatureResult extract(CommentSample sample) {
        String authorId = sample.original().authorId();
        if (authorId == null || authorId.isBlank()) {
            return new FeatureResult("repeated_author", 0.0, null);
        }

        int count = sample.authorCounts().getOrDefault(authorId, 0);

        if (count <= 1) {
            return new FeatureResult("repeated_author", 0.0, null);
        }

        double value;
        if (count == 2) {
            value = 0.4;
        } else if (count == 3) {
            value = 0.7;
        } else {
            value = 1.0;
        }

        return new FeatureResult(
                "repeated_author",
                value,
                "same author posted " + count + " times"
        );
    }
}
