package com.dissertation.analysisservice.detection.features;

import com.dissertation.analysisservice.detection.model.CommentSample;

import java.util.List;

public class SpamKeywordFeature implements FeatureExtractor {

    private static double HIT_CAP = 3.0;

    private static final List<String> KEYWORDS = List.of(
            "subscribe", "follow", "free", "gift", "giveaway", "click",
            "whatsapp", "telegram", "dm me", "promo", "discount", "earn"
    );

    @Override
    public FeatureResult extract(CommentSample sample) {
        String t = sample.normalized();
        int hits = 0;
        for (String k : KEYWORDS) {
            if (t.contains(k)) hits++;
        }
        if (hits == 0) return new FeatureResult("spam_keywords", 0.0, null);

        double v = Math.min(1.0, hits / HIT_CAP);
        String reason = "spam keywords (" + hits + ")";
        return new FeatureResult("spam_keywords", v, reason);
    }
}
