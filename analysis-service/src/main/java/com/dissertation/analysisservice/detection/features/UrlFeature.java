package com.dissertation.analysisservice.detection.features;

import com.dissertation.analysisservice.detection.model.CommentSample;

public class UrlFeature implements FeatureExtractor {
    @Override
    public FeatureResult extract(CommentSample sample) {
        boolean hasUrl = sample.getNormalized().contains("<url>");
        return hasUrl
                ? new FeatureResult("url", 1.0, "contains link")
                : new FeatureResult("url", 0.0, null);
    }
}
