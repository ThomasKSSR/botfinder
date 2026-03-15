package com.dissertation.ingestionservice.messaging;

import com.dissertation.contracts.events.AnalysisRequestedEvent;
import com.dissertation.contracts.events.CommentsIngestedEvent;
import com.dissertation.contracts.messaging.MessagingConstants;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IngestionListener {

    private final RabbitTemplate rabbit;

    public IngestionListener(RabbitTemplate rabbit) {
        this.rabbit = rabbit;
    }

    @RabbitListener(queues = MessagingConstants.Q_INGESTION)
    public void onRequested(AnalysisRequestedEvent event) {
        List<String> comments = List.of(
                "Nice video!",
                "Nice video!",
                "Subscribe to my channel!!!",
                "Check my profile for free gifts http://spam.com"
        );

        System.out.println("[INGESTION] got jobId=" + event.getJobId() + " url=" + event.getUrl());
        rabbit.convertAndSend(
                MessagingConstants.EXCHANGE,
                MessagingConstants.RK_COMMENTS_INGESTED,
                new CommentsIngestedEvent(event.getJobId(), "youtube", comments)
        );
    }
}
