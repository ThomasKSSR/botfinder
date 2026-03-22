package com.dissertation.analysisservice.messaging;

import com.dissertation.analysisservice.detection.BotDetector;
import com.dissertation.contracts.events.AnalysisCompletedEvent;
import com.dissertation.contracts.events.CommentsIngestedEvent;
import com.dissertation.contracts.events.IngestedComment;
import com.dissertation.contracts.messaging.MessagingConstants;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class AnalysisListener {

    private final RabbitTemplate rabbit;

    public AnalysisListener(RabbitTemplate rabbit) {
        this.rabbit = rabbit;
    }

    @RabbitListener(queues = MessagingConstants.Q_ANALYSIS)
    public void onComments(CommentsIngestedEvent event) {
        System.out.println("[ANALYSIS] got jobId=" + event.jobId() + " comments=" + event.comments().size());
        //https://archive.ics.uci.edu/dataset/380/youtube+spam+collection
        // https://www.kaggle.com/datasets/lakshmi25npathi/images
        // https://www.kaggle.com/datasets/fivethirtyeight/russian-troll-tweets


        BotDetector detector = new BotDetector();

        List<BotDetector.DetectionResult> detections = detector.detect(event.comments());

        List<BotDetector.DetectionResult> top = detections.stream()
                .sorted((left, right) -> Double.compare(right.score(), left.score()))
                .limit(10)
                .toList();

        List<AnalysisCompletedEvent.ResultItem> results = top.stream()
                .map(detectionResult -> {
                    IngestedComment original = event.comments().get(detectionResult.index());
                    return new AnalysisCompletedEvent.ResultItem(
                            original.commentId(),
                            original.authorName(),
                            detectionResult.score(),
                            detectionResult.label(),
                            detectionResult.reason(),
                            shorten(original.text())
                    );
                })
                .toList();

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
