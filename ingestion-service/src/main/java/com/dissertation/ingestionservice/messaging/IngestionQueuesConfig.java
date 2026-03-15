package com.dissertation.ingestionservice.messaging;

import com.dissertation.contracts.messaging.MessagingConstants;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IngestionQueuesConfig {

    @Bean
    public Queue ingestionQueue() {
        return QueueBuilder.durable(MessagingConstants.Q_INGESTION).build();
    }

    @Bean
    public Binding ingestionBinding(Queue ingestionQueue, TopicExchange trollExchange) {
        return BindingBuilder.bind(ingestionQueue)
                .to(trollExchange)
                .with(MessagingConstants.RK_ANALYSIS_REQUESTED);
    }
}
