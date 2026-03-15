package com.dissertation.analysisservice.messaging;

import com.dissertation.contracts.messaging.MessagingConstants;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AnalysisQueuesConfig {

    @Bean
    public Queue analysisQueue() {
        return QueueBuilder.durable(MessagingConstants.Q_ANALYSIS).build();
    }

    @Bean
    public Binding analysisBinding(Queue analysisQueue, TopicExchange trollExchange) {
        return BindingBuilder.bind(analysisQueue)
                .to(trollExchange)
                .with(MessagingConstants.RK_COMMENTS_INGESTED);
    }
}
