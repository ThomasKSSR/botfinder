package com.dissertation.analysisservice.messaging;

import com.dissertation.analysisservice.detection.BotDetector;
import com.dissertation.contracts.events.AnalysisCompletedEvent;
import com.dissertation.contracts.events.CommentsIngestedEvent;
import com.dissertation.contracts.messaging.MessagingConstants;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AnalysisListener {

    private final RabbitTemplate rabbit;

    public AnalysisListener(RabbitTemplate rabbit) {
        this.rabbit = rabbit;
    }

    @RabbitListener(queues = MessagingConstants.Q_ANALYSIS)
    public void onComments(CommentsIngestedEvent event) {
        System.out.println("[ANALYSIS] got jobId=" + event.getJobId() + " comments=" + event.getComments().size());

        BotDetector detector = new BotDetector();
        var detections = detector.detect(event.getComments());

// take top suspicious items
        var top = detections.stream()
                .sorted((a,b) -> Double.compare(b.score(), a.score()))
                .limit(10)
                .toList();

        var results = top.stream()
                .map(d -> new com.dissertation.contracts.events.AnalysisCompletedEvent.ResultItem(
                        "comment#" + d.index(),
                        d.score(),
                        d.label(),
                        d.reason()
                ))
                .toList();

        rabbit.convertAndSend(
                MessagingConstants.EXCHANGE,
                MessagingConstants.RK_ANALYSIS_COMPLETED,
                new AnalysisCompletedEvent(event.getJobId(), Instant.now(), results)
        );


        rabbit.convertAndSend(
                MessagingConstants.EXCHANGE,
                MessagingConstants.RK_ANALYSIS_COMPLETED,
                new AnalysisCompletedEvent(event.getJobId(), Instant.now(), results)
        );
    }
}
