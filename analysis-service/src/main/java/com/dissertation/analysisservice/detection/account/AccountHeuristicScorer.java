package com.dissertation.analysisservice.detection.account;

import com.dissertation.contracts.events.IngestedComment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AccountHeuristicScorer {

    public double score(IngestedComment current, List<IngestedComment> allComments) {
        if (current == null) {
            return 0.0;
        }

        double score = 0.0;

        String authorId = current.authorId();
        if (authorId == null || authorId.isBlank()) {
            return 0.0;
        }

        List<IngestedComment> sameAuthorComments = allComments.stream()
                .filter(comment -> authorId.equals(comment.authorId()))
                .toList();

        int sameAuthorCount = sameAuthorComments.size();

        if (sameAuthorCount >= 2) {
            score += 0.25;
        }
        if (sameAuthorCount >= 3) {
            score += 0.20;
        }

        String normalizedCurrent = normalize(current.text());

        long sameAuthorDuplicateCount = sameAuthorComments.stream()
                .map(comment -> normalize(comment.text()))
                .filter(normalizedCurrent::equals)
                .count();

        if (sameAuthorDuplicateCount >= 2) {
            score += 0.25;
        }

        int mentionCount = countOccurrences(current.text(), "@");
        if (mentionCount >= 2) {
            score += 0.05;
        }

        int hashtagCount = countOccurrences(current.text(), "#");
        if (hashtagCount >= 3) {
            score += 0.05;
        }

        int urlCount = countUrls(current.text());
        if (urlCount >= 1) {
            score += 0.10;
        }

        double uppercaseRatio = uppercaseRatio(current.text());
        if (uppercaseRatio > 0.5 && current.text() != null && current.text().length() > 8) {
            score += 0.05;
        }

        int punctuationBurst = repeatedPunctuationCount(current.text());
        if (punctuationBurst >= 3) {
            score += 0.05;
        }

        if (current.likeCount() == 0 && current.text() != null && current.text().length() < 8) {
            score += 0.03;
        }

        return clamp01(score);
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }

        return text.toLowerCase()
                .replaceAll("https?://\\S+", " ")
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private int countOccurrences(String text, String token) {
        if (text == null || token == null || token.isEmpty()) {
            return 0;
        }

        int count = 0;
        int index = 0;
        while ((index = text.indexOf(token, index)) != -1) {
            count++;
            index += token.length();
        }
        return count;
    }

    private int countUrls(String text) {
        if (text == null) {
            return 0;
        }

        return text.split("https?://", -1).length - 1;
    }

    private double uppercaseRatio(String text) {
        if (text == null || text.isBlank()) {
            return 0.0;
        }

        int letters = 0;
        int uppercase = 0;

        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                letters++;
                if (Character.isUpperCase(c)) {
                    uppercase++;
                }
            }
        }

        if (letters == 0) {
            return 0.0;
        }

        return (double) uppercase / letters;
    }

    private int repeatedPunctuationCount(String text) {
        if (text == null) {
            return 0;
        }

        int maxRun = 0;
        int currentRun = 0;
        char previous = '\0';

        for (char c : text.toCharArray()) {
            if ((c == '!' || c == '?' || c == '.') && c == previous) {
                currentRun++;
            } else {
                currentRun = 1;
            }

            if (c == '!' || c == '?' || c == '.') {
                maxRun = Math.max(maxRun, currentRun);
            }

            previous = c;
        }

        return maxRun;
    }

    private double clamp01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}