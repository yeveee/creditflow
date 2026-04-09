package com.nexusbank.creditflow.service.credit;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.nexusbank.creditflow.api.config.RabbitMQConfig;

@ExtendWith(MockitoExtension.class)
public class NotificationPublisherTest {
    
    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private NotificationPublisher notificationPublisher;

    @Test
    void shouldSendMessageToQueue() {
        // When
        notificationPublisher.publierChangementStatut(1L, "EN_INSTRUCTION");
 
        // Then
        verify(rabbitTemplate).convertAndSend(
                RabbitMQConfig.QUEUE_STATUT_CHANGE,
                "{\"demandeId\": 1, \"nouveauStatut\": \"EN_INSTRUCTION\"}");
    }
}
