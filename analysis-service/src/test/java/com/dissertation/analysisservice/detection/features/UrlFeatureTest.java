package com.dissertation.analysisservice.detection.features;

import com.dissertation.analysisservice.detection.model.CommentSample;
import com.dissertation.contracts.events.IngestedComment;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UrlFeatureTest {

    @Test
    void extract_returns_1_when_normalized_contains_url() {
        IngestedComment ic = new IngestedComment("1","a","name","text", Instant.now(), 0L);
        CommentSample sample = new CommentSample(0, ic, "has <url> here", Map.of(), Map.of(), List.of(), List.of());
        UrlFeature f = new UrlFeature();
        FeatureResult r = f.extract(sample);
        assertEquals(1.0, r.value());
        assertEquals("url", r.name());
    }

    @Test
    void extract_returns_0_when_no_url() {
        IngestedComment ic = new IngestedComment("1","a","name","text", Instant.now(), 0L);
        CommentSample sample = new CommentSample(0, ic, "no links", Map.of(), Map.of(), List.of(), List.of());
        UrlFeature f = new UrlFeature();
        FeatureResult r = f.extract(sample);
        assertEquals(0.0, r.value());
    }
}
