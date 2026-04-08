package com.nexusbank.creditflow.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.Queue;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_STATUT_CHANGE = "creditflow.statut.change";
 
    @Bean
    public Queue statutChangeQueue() {
        return new Queue(QUEUE_STATUT_CHANGE, true);
    }
    
}
