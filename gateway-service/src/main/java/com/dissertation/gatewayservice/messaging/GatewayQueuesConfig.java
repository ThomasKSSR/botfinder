package com.dissertation.gatewayservice.messaging;

import com.dissertation.contracts.messaging.MessagingConstants;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayQueuesConfig {

    @Bean
    public Queue gatewayResultsQueue() {
        return QueueBuilder.durable(MessagingConstants.Q_GATEWAY_RESULTS).build();
    }

    @Bean
    public Binding gatewayResultsBinding(Queue gatewayResultsQueue, TopicExchange trollExchange) {
        return BindingBuilder.bind(gatewayResultsQueue)
                .to(trollExchange)
                .with(MessagingConstants.RK_ANALYSIS_COMPLETED);
    }
}
