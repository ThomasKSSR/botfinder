package com.dissertation.analysisservice.detection.features;

public class FeatureResult {
    private final String name;
    private final double value;
    private final String reason;

    public FeatureResult(String name, double value, String reason) {
        this.name = name;
        this.value = value;
        this.reason = reason;
    }

    public String getName() { return name; }
    public double getValue() { return value; }
    public String getReason() { return reason; }
}
