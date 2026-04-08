package com.nexusbank.creditflow.service.credit;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.nexusbank.creditflow.api.config.RabbitMQConfig;

@Component
public class NotificationPublisher {
    
    private final RabbitTemplate rabbitTemplate;

    public NotificationPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publierChangementStatut(Long demandeId, String nouveauStatut) {

        String message = String.format(
        "{\"demandeId\": %d, \"nouveauStatut\": \"%s\"}", 
        demandeId, nouveauStatut);

        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_STATUT_CHANGE, message);
    }
}
