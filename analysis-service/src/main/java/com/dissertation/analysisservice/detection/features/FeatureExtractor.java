package com.dissertation.analysisservice.detection.features;

import com.dissertation.analysisservice.detection.model.CommentSample;

public interface FeatureExtractor {
    FeatureResult extract(CommentSample sample);
}
