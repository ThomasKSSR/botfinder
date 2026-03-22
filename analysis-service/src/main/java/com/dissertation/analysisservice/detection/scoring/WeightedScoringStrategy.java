package com.dissertation.analysisservice.detection.scoring;

import com.dissertation.analysisservice.detection.features.FeatureResult;

import java.util.*;

public class WeightedScoringStrategy {

    private final Map<String, Double> weights = Map.of(
            "duplicate", 0.35,
            "near_duplicate", 0.25,
            "repeated_author", 0.20,
            "temporal_sync", 0.15,
            "url", 0.20,
            "spam_keywords", 0.20,
            "generic_praise", 0.10
    );

    public ScoreOutput score(List<FeatureResult> features) {
        double sum = 0.0;

        for (FeatureResult f : features) {
            double w = weights.getOrDefault(f.name(), 0.0);
            sum += w * clamp01(f.value());
        }

        double s = clamp01(sum);

        List<FeatureResult> triggered = features.stream()
                .filter(fr -> fr.reason() != null && fr.value() > 0.0)
                .sorted(Comparator.comparingDouble(FeatureResult::value).reversed())
                .toList();

        String reason = triggered.isEmpty()
                ? "no strong automation signals"
                : String.join("; ", triggered.stream().limit(3).map(FeatureResult::reason).toList());

        String label = (s >= 0.50) ? "bot-like" : "normal";

        return new ScoreOutput(s, label, reason);
    }

    private static double clamp01(double x) {
        return Math.max(0.0, Math.min(1.0, x));
    }

    public record ScoreOutput(double score, String label, String reason) {}
}
