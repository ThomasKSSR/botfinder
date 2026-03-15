package com.dissertation.gatewayservice.messaging;

import com.dissertation.contracts.events.AnalysisCompletedEvent;
import com.dissertation.contracts.messaging.MessagingConstants;
import com.dissertation.gatewayservice.service.JobStore;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class GatewayResultListener {

    private final JobStore store;

    public GatewayResultListener(JobStore store) {
        this.store = store;
    }

    @RabbitListener(queues = MessagingConstants.Q_GATEWAY_RESULTS)
    public void onCompleted(AnalysisCompletedEvent event) {
        System.out.println("[GATEWAY] got jobId=" + event.getJobId() + " results=" + event.getResults().size());
        store.complete(event);
    }
}