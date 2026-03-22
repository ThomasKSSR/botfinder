package com.dissertation.analysisservice.detection.features;

import com.dissertation.analysisservice.detection.model.CommentSample;
import com.dissertation.contracts.events.IngestedComment;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class TemporalSynchronizationFeature implements FeatureExtractor {

    private static final long WINDOW_SECONDS = 60;

    @Override
    public FeatureResult extract(CommentSample sample) {
        Instant currentTime = sample.original().publishedAt();
        if (currentTime == null) {
            return new FeatureResult("temporal_sync", 0.0, null);
        }

        int nearbyCount = 0;
        List<IngestedComment> allComments = sample.allComments();

        for (int i = 0; i < allComments.size(); i++) {
            if (i == sample.index()) {
                continue;
            }

            Instant otherTime = allComments.get(i).publishedAt();
            if (otherTime == null) {
                continue;
            }

            long seconds = Math.abs(Duration.between(currentTime, otherTime).getSeconds());
            if (seconds <= WINDOW_SECONDS) {
                nearbyCount++;
            }
        }

        if (nearbyCount == 0) {
            return new FeatureResult("temporal_sync", 0.0, null);
        }

        double value = Math.min(1.0, nearbyCount / 5.0);
        return new FeatureResult(
                "temporal_sync",
                value,
                "burst posting window (" + nearbyCount + " nearby comments in 60s)"
        );
    }
}
