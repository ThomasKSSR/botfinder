package com.dissertation.analysisservice.detection.model;
import java.util.Map;

public class CommentSample {
    private final int index;
    private final String raw;
    private final String normalized;
    private final Map<String, Integer> normalizedCounts;

    public CommentSample(int index, String raw, String normalized, Map<String, Integer> normalizedCounts) {
        this.index = index;
        this.raw = raw;
        this.normalized = normalized;
        this.normalizedCounts = normalizedCounts;
    }

    public int getIndex() { return index; }
    public String getRaw() { return raw; }
    public String getNormalized() { return normalized; }
    public Map<String, Integer> getNormalizedCounts() { return normalizedCounts; }
}
