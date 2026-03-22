package com.dissertation.analysisservice.detection.features;

import com.dissertation.analysisservice.detection.model.CommentSample;

import com.dissertation.analysisservice.detection.model.CommentSample;

public class DuplicateFeature implements FeatureExtractor {

    @Override
    public FeatureResult extract(CommentSample sample) {
        int count = sample.normalizedCounts().getOrDefault(sample.normalized(), 0);

        if (count <= 1) {
            return new FeatureResult("duplicate", 0.0, null);
        }

        double value;
        if (count == 2) {
            value = 0.5;
        } else if (count <= 5) {
            value = 0.8;
        } else {
            value = 1.0;
        }

        return new FeatureResult(
                "duplicate",
                value,
                "copy-paste duplicate (" + count + " times)"
        );
    }
}
