package com.dissertation.analysisservice.detection.features;

import com.dissertation.analysisservice.detection.model.CommentSample;

import java.util.List;

public class GenericPraiseFeature implements FeatureExtractor {

    private static final List<String> PHRASES = List.of(
            "nice video", "great video", "awesome", "amazing", "well done", "good job", "super"
    );

    @Override
    public FeatureResult extract(CommentSample sample) {
        String t = sample.getNormalized();
        boolean shortText = t.length() <= 30;
        boolean generic = PHRASES.stream().anyMatch(t::contains);

        if (shortText && generic) {
            return new FeatureResult("generic_praise", 0.7, "generic short template");
        }
        return new FeatureResult("generic_praise", 0.0, null);
    }
}
