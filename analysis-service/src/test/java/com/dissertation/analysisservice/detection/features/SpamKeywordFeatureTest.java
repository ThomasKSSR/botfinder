package com.dissertation.analysisservice.detection.features;

import com.dissertation.analysisservice.detection.model.CommentSample;
import com.dissertation.contracts.events.IngestedComment;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SpamKeywordFeatureTest {

    @Test
    void extract_counts_keywords_and_caps_at_one() {
        IngestedComment ic = new IngestedComment("1","a","n","subscribe free giveaway click", Instant.now(), 0L);
        CommentSample sample = new CommentSample(0, ic, "subscribe free giveaway click", Map.of(), Map.of(), List.of(), List.of());
        SpamKeywordFeature f = new SpamKeywordFeature();
        FeatureResult r = f.extract(sample);
        assertTrue(r.value() <= 1.0 && r.value() > 0.0);
        assertEquals("spam_keywords", r.name());
        assertTrue(r.reason().contains("spam keywords"));
    }

    @Test
    void extract_returns_zero_when_no_keywords() {
        IngestedComment ic = new IngestedComment("1","a","n","hello world", Instant.now(), 0L);
        CommentSample sample = new CommentSample(0, ic, "hello world", Map.of(), Map.of(), List.of(), List.of());
        SpamKeywordFeature f = new SpamKeywordFeature();
        FeatureResult r = f.extract(sample);
        assertEquals(0.0, r.value());
    }
}
