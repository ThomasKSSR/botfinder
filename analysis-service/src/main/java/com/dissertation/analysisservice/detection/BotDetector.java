package com.dissertation.analysisservice.detection;

import com.dissertation.analysisservice.detection.features.*;
import com.dissertation.analysisservice.detection.model.CommentSample;
import com.dissertation.analysisservice.detection.scoring.WeightedScoringStrategy;
import com.dissertation.analysisservice.detection.text.TextPreprocessor;
import com.dissertation.contracts.events.IngestedComment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BotDetector {

    private final TextPreprocessor preprocessor = new TextPreprocessor();

    private final List<FeatureExtractor> extractors = List.of(
            new DuplicateFeature(),
            new NearDuplicateFeature(),
            new RepeatedAuthorFeature(),
            new TemporalSynchronizationFeature(),
            new UrlFeature(),
            new SpamKeywordFeature(),
            new GenericPraiseFeature()
    );

    private final WeightedScoringStrategy scoring = new WeightedScoringStrategy();

    public List<DetectionResult> detect(List<IngestedComment> comments) {
        if (comments == null || comments.isEmpty()) {
            return List.of();
        }

        List<String> normalizedTexts = comments.stream()
                .map(comment -> preprocessor.normalize(comment.text()))
                .toList();

        Map<String, Integer> normalizedCounts = normalizedTexts.stream()
                .collect(Collectors.toMap(text -> text, text -> 1, Integer::sum));

        Map<String, Integer> authorCounts = comments.stream()
                .map(IngestedComment::authorId)
                .filter(authorId -> authorId != null && !authorId.isBlank())
                .collect(Collectors.toMap(authorId -> authorId, authorId -> 1, Integer::sum));

        List<DetectionResult> results = new ArrayList<>();

        for (int i = 0; i < comments.size(); i++) {
            CommentSample sample = new CommentSample(
                    i,
                    comments.get(i),
                    normalizedTexts.get(i),
                    normalizedCounts,
                    authorCounts,
                    normalizedTexts,
                    comments
            );

            List<FeatureResult> features = new ArrayList<>();
            for (FeatureExtractor extractor : extractors) {
                features.add(extractor.extract(sample));
            }

            WeightedScoringStrategy.ScoreOutput scoreOutput = scoring.score(features);

            results.add(new DetectionResult(
                    i,
                    scoreOutput.score(),
                    scoreOutput.label(),
                    scoreOutput.reason()
            ));
        }

        System.out.println("[DETECTOR] total comments=" + comments.size());
        System.out.println("[DETECTOR] unique normalized texts=" + normalizedCounts.size());
        System.out.println("[DETECTOR] repeated authors=" + authorCounts.size());

        return results;
    }

    public record DetectionResult(
            int index,
            double score,
            String label,
            String reason
    ) {}
}
