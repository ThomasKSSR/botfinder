package com.dissertation.analysisservice.detection.features;

import com.dissertation.analysisservice.detection.model.CommentSample;
import com.dissertation.contracts.events.IngestedComment;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DuplicateFeatureTest {

    @Test
    void returns_zero_for_unique() {
        IngestedComment ic = new IngestedComment("1","a","n","text", Instant.now(), 0L);
        CommentSample sample = new CommentSample(0, ic, "text", Map.of("text", 1), Map.of(), List.of(), List.of());
        DuplicateFeature f = new DuplicateFeature();
        FeatureResult r = f.extract(sample);
        assertEquals(0.0, r.value());
    }

    @Test
    void scales_with_count() {
        IngestedComment ic = new IngestedComment("1","a","n","dup", Instant.now(), 0L);
        CommentSample sample2 = new CommentSample(0, ic, "dup", Map.of("dup", 2), Map.of(), List.of(), List.of());
        DuplicateFeature f = new DuplicateFeature();
        FeatureResult r2 = f.extract(sample2);
        assertEquals(0.5, r2.value());

        CommentSample sample4 = new CommentSample(0, ic, "dup", Map.of("dup", 4), Map.of(), List.of(), List.of());
        FeatureResult r4 = f.extract(sample4);
        assertEquals(0.8, r4.value());

        CommentSample sample6 = new CommentSample(0, ic, "dup", Map.of("dup", 6), Map.of(), List.of(), List.of());
        FeatureResult r6 = f.extract(sample6);
        assertEquals(1.0, r6.value());
    }
}
