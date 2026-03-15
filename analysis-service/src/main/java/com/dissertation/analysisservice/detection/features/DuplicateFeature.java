package com.dissertation.analysisservice.detection.features;

import com.dissertation.analysisservice.detection.model.CommentSample;

public class DuplicateFeature implements FeatureExtractor {

    @Override
    public FeatureResult extract(CommentSample sample) {
        int count = sample.getNormalizedCounts().getOrDefault(sample.getNormalized(), 0);
        if (count <= 1) {
            return new FeatureResult("duplicate", 0.0, null);
        }

        double v = Math.min(1.0, (count - 1) / 9.0);
        String reason = "copy-paste duplicate (" + count + " times)";
        return new FeatureResult("duplicate", v, reason);
    }
}
