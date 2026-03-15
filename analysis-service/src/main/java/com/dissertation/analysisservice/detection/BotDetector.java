package com.dissertation.analysisservice.detection;

import com.dissertation.analysisservice.detection.features.*;
import com.dissertation.analysisservice.detection.model.CommentSample;
import com.dissertation.analysisservice.detection.scoring.WeightedScoringStrategy;
import com.dissertation.analysisservice.detection.text.TextPreprocessor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BotDetector {

    private final TextPreprocessor preprocessor = new TextPreprocessor();
    private final List<FeatureExtractor> extractors = List.of(
            new DuplicateFeature(),
            new UrlFeature(),
            new SpamKeywordFeature(),
            new GenericPraiseFeature()
    );
    private final WeightedScoringStrategy scoring = new WeightedScoringStrategy();

    public List<DetectionResult> detect(List<String> comments) {
        if (comments == null) return List.of();

        List<String> normalized = comments.stream()
                .map(preprocessor::normalize)
                .toList();

        Map<String, Integer> counts = normalized.stream()
                .collect(Collectors.toMap(s -> s, s -> 1, Integer::sum));

        List<DetectionResult> out = new ArrayList<>();
        for (int i = 0; i < comments.size(); i++) {
            CommentSample sample = new CommentSample(i, comments.get(i), normalized.get(i), counts);

            List<FeatureResult> feats = new ArrayList<>();
            for (FeatureExtractor ex : extractors) {
                feats.add(ex.extract(sample));
            }

            var scored = scoring.score(feats);
            out.add(new DetectionResult(
                    i,
                    comments.get(i),
                    normalized.get(i),
                    scored.score(),
                    scored.label(),
                    scored.reason()
            ));
        }
        return out;
    }

    public record DetectionResult(
            int index,
            String raw,
            String normalized,
            double score,
            String label,
            String reason
    ) {}
}
