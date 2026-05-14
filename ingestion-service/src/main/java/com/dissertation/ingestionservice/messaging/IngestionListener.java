package com.dissertation.ingestionservice.messaging;

import com.dissertation.contracts.events.AnalysisRequestedEvent;
import com.dissertation.contracts.messaging.MessagingConstants;
import com.dissertation.ingestionservice.service.IngestionOrchestrator;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class IngestionListener {

    private final RabbitTemplate rabbit;
    private final IngestionOrchestrator orchestrator;

    public IngestionListener(RabbitTemplate rabbit, IngestionOrchestrator orchestrator) {
        this.rabbit = rabbit;
        this.orchestrator = orchestrator;
    }

    @RabbitListener(queues = MessagingConstants.Q_INGESTION)
    public void onRequested(AnalysisRequestedEvent event) {
        int maxComments = event.maxComments() != null ? event.maxComments() : 100;

        System.out.println(
                "[INGESTION] jobId=" + event.jobId() +
                        " url=" + event.url() +
                        " maxComments=" + maxComments
        );

        var ingested = orchestrator.ingest(event.jobId(), event.url(), maxComments);

        rabbit.convertAndSend(
                MessagingConstants.EXCHANGE,
                MessagingConstants.RK_COMMENTS_INGESTED,
                ingested
        );
    }
}
