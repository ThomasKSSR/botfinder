package com.dissertation.analysisservice.detection.account;

import com.dissertation.contracts.events.IngestedComment;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AccountHeuristicScorerTest {

    @Test
    void score_increases_with_multiple_author_comments_and_features() {
        IngestedComment c1 = new IngestedComment("id1","author1","name","CHECK THIS!!! https://x @a @b #a #b #c", Instant.now(), 0L);
        IngestedComment c2 = new IngestedComment("id2","author1","name","CHECK THIS!!! https://x @a @b #a #b #c", Instant.now(), 0L);
        IngestedComment c3 = new IngestedComment("id3","author1","name","short", Instant.now(), 0L);

        AccountHeuristicScorer scorer = new AccountHeuristicScorer();
        double s = scorer.score(c1, List.of(c1, c2, c3));

        assertTrue(s > 0.5, "score was " + s);
    }

    @Test
    void null_current_returns_zero() {
        AccountHeuristicScorer scorer = new AccountHeuristicScorer();
        double s = scorer.score(null, List.of());
        assertTrue(s == 0.0);
    }
}
