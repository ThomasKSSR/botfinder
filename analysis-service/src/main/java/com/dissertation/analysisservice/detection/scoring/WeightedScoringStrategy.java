package com.dissertation.analysisservice.detection.scoring;

import com.dissertation.analysisservice.detection.features.FeatureResult;

import java.util.*;

public class WeightedScoringStrategy {

    // You can tune these weights later (and evaluate)
    private final Map<String, Double> weights = Map.of(
            "duplicate", 0.40,
            "url", 0.30,
            "spam_keywords", 0.20,
            "generic_praise", 0.10
    );

    public ScoreOutput score(List<FeatureResult> features) {
        double sum = 0.0;
        for (FeatureResult f : features) {
            double w = weights.getOrDefault(f.getName(), 0.0);
            sum += w * clamp01(f.getValue());
        }
        double s = clamp01(sum);

        // Build short reason from the top 2 triggered features
        List<FeatureResult> triggered = features.stream()
                .filter(fr -> fr.getReason() != null && fr.getValue() > 0.0)
                .sorted(Comparator.comparingDouble(FeatureResult::getValue).reversed())
                .toList();

        String reason = triggered.isEmpty()
                ? "no strong automation signals"
                : String.join("; ", triggered.stream().limit(2).map(FeatureResult::getReason).toList());

        String label = (s >= 0.60) ? "bot-like" : "normal";
        return new ScoreOutput(s, label, reason);
    }

    private static double clamp01(double x) {
        return Math.max(0.0, Math.min(1.0, x));
    }

    public record ScoreOutput(double score, String label, String reason) {}
}
