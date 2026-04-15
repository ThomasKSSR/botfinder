package com.dissertation.analysisservice.messaging;

import com.dissertation.analysisservice.detection.BotDetector;
import com.dissertation.analysisservice.detection.account.AccountHeuristicScorer;
import com.dissertation.analysisservice.dto.PredictionItem;
import com.dissertation.analysisservice.ml.MlServiceClient;
import com.dissertation.contracts.events.AnalysisCompletedEvent;
import com.dissertation.contracts.events.CommentsIngestedEvent;
import com.dissertation.contracts.events.IngestedComment;
import com.dissertation.contracts.messaging.MessagingConstants;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class AnalysisListener {

    private final RabbitTemplate rabbit;
    private final MlServiceClient mlServiceClient;

    private final AccountHeuristicScorer accountHeuristicScorer;

    public AnalysisListener(RabbitTemplate rabbit,
                            MlServiceClient mlServiceClient,
                            AccountHeuristicScorer accountHeuristicScorer) {
        this.rabbit = rabbit;
        this.mlServiceClient = mlServiceClient;
        this.accountHeuristicScorer = accountHeuristicScorer;
    }

    @RabbitListener(queues = MessagingConstants.Q_ANALYSIS)
    public void onComments(CommentsIngestedEvent event) {
        System.out.println("[ANALYSIS] got jobId=" + event.jobId() + " comments=" + event.comments().size());
        // spam-ham comments
        //https://archive.ics.uci.edu/dataset/380/youtube+spam+collection
        // https://www.kaggle.com/datasets/lakshmi25npathi/images
        // https://www.kaggle.com/datasets/fivethirtyeight/russian-troll-tweets

        //troll
        // https://www.kaggle.com/datasets/dataturks/dataset-for-detection-of-cybertrolls
        // https://www.kaggle.com/datasets/amirmeymandi/troll-detection

        //account
        // https://www.kaggle.com/datasets/goyaladi/twitter-bot-detection-dataset
        // https://www.kaggle.com/datasets/davidmartngutirrez/twitter-bots-accounts


        BotDetector detector = new BotDetector();

        List<BotDetector.DetectionResult> detections = detector.detect(event.comments());

        List<String> texts = event.comments().stream()
                .map(IngestedComment::text)
                .toList();

        List<PredictionItem> spamPredictions = mlServiceClient.predictSpam(texts);
        List<PredictionItem> trollPredictions = mlServiceClient.predictTroll(texts);

        List<AnalysisCompletedEvent.ResultItem> results = new ArrayList<>();

        for (int i = 0; i < event.comments().size(); i++) {
            IngestedComment original = event.comments().get(i);
            BotDetector.DetectionResult ruleResult = detections.get(i);

            double ruleScore = ruleResult.score();
            double spamScore = 0.0;
            double trollScore = 0.0;

            String spamLabel = "unknown";
            String trollLabel = "unknown";

            if (i < spamPredictions.size()) {
                PredictionItem spamPrediction = spamPredictions.get(i);
                spamLabel = spamPrediction.label();

                if ("spam".equalsIgnoreCase(spamPrediction.label())) {
                    spamScore = spamPrediction.score();
                } else {
                    spamScore = 1.0 - spamPrediction.score();
                }
            }

            if (i < trollPredictions.size()) {
                PredictionItem trollPrediction = trollPredictions.get(i);
                trollLabel = trollPrediction.label();

                if ("troll".equalsIgnoreCase(trollPrediction.label())) {
                    trollScore = trollPrediction.score();
                } else {
                    trollScore = 1.0 - trollPrediction.score();
                }
            }
            double accountHeuristicScore = accountHeuristicScorer.score(original, event.comments());

            double finalScore =
                    (0.35 * ruleScore) +
                            (0.25 * spamScore) +
                            (0.20 * trollScore) +
                            (0.20 * accountHeuristicScore);
            String finalLabel = finalScore >= 0.50 ? "bot-like" : "normal";

            String finalReason = ruleResult.reason()
                    + "; spam-ml=" + spamLabel
                    + "; troll-ml=" + trollLabel
                    + "; account-heuristic=" + String.format("%.2f", accountHeuristicScore);

            results.add(new AnalysisCompletedEvent.ResultItem(
                    original.commentId(),
                    original.authorName(),
                    finalScore,
                    ruleScore,
                    spamScore,
                    trollScore,
                    accountHeuristicScore,
                    finalLabel,
                    finalReason,
                    shorten(original.text())
            ));
        }

        for (AnalysisCompletedEvent.ResultItem result : results) {
            System.out.println(
                    "[ANALYSIS-ML] id=" + result.commentId()
                            + " author=" + result.authorName()
                            + " score=" + result.score()
                            + " ruleScore=" + result.ruleScore()
                            + " spamMlScore=" + result.spamMlScore()
                            + " trollMlScore=" + result.trollMlScore()
                            + " accountHeuristicScore=" + result.accountHeuristicScore()
                            + " label=" + result.label()
                            + " reason=" + result.reason()
            );
        }

        rabbit.convertAndSend(
                MessagingConstants.EXCHANGE,
                MessagingConstants.RK_ANALYSIS_COMPLETED,
                new AnalysisCompletedEvent(event.jobId(), Instant.now(), results)
        );
    }

    private String shorten(String text) {
        if (text == null) return "";
        return text.length() <= 120 ? text : text.substring(0, 117) + "...";
    }
}
