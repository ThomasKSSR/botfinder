package com.dissertation.analysisservice.detection.features;

import com.dissertation.analysisservice.detection.model.CommentSample;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NearDuplicateFeature implements FeatureExtractor {

    @Override
    public FeatureResult extract(CommentSample sample) {
        String current = sample.normalized();
        if (current == null || current.isBlank()) {
            return new FeatureResult("near_duplicate", 0.0, null);
        }

        int similarCount = 0;

        List<String> allTexts = sample.allNormalizedTexts();
        for (int i = 0; i < allTexts.size(); i++) {
            if (i == sample.index()) {
                continue;
            }

            String other = allTexts.get(i);
            double similarity = jaccardSimilarity(current, other);

            if (similarity >= 0.80 && similarity < 1.0) {
                similarCount++;
            }
        }

        if (similarCount == 0) {
            return new FeatureResult("near_duplicate", 0.0, null);
        }

        double value = Math.min(1.0, similarCount / 3.0);
        return new FeatureResult(
                "near_duplicate",
                value,
                "template-like variation found (" + similarCount + " similar comments)"
        );
    }

    private double jaccardSimilarity(String a, String b) {
        Set<String> aTokens = tokenize(a);
        Set<String> bTokens = tokenize(b);

        if (aTokens.isEmpty() && bTokens.isEmpty()) {
            return 1.0;
        }

        Set<String> intersection = new HashSet<>(aTokens);
        intersection.retainAll(bTokens);

        Set<String> union = new HashSet<>(aTokens);
        union.addAll(bTokens);

        if (union.isEmpty()) {
            return 0.0;
        }

        return (double) intersection.size() / union.size();
    }

    private Set<String> tokenize(String text) {
        String[] parts = text.split("\\s+");
        Set<String> tokens = new HashSet<>();
        for (String part : parts) {
            String token = part.trim();
            if (!token.isBlank()) {
                tokens.add(token);
            }
        }
        return tokens;
    }
}
