package com.example.demo.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SyncPublisher {

    private static final Logger logger = LoggerFactory.getLogger(SyncPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.sync}")
    private String syncExchange;

    @Value("${rabbitmq.routing.key.sync.user}")
    private String syncUserRoutingKey;

    public SyncPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishUserCreated(UUID userId, String username) {
        SyncMessage message = new SyncMessage("USER_CREATED", userId);
        message.setUsername(username);

        try {
            rabbitTemplate.convertAndSend(syncExchange, syncUserRoutingKey, message);
            logger.info("Published USER_CREATED event for user: {}", userId);
        } catch (Exception e) {
            logger.error("Failed to publish USER_CREATED event: {}", e.getMessage());
        }
    }

    public void publishUserDeleted(UUID userId) {
        SyncMessage message = new SyncMessage("USER_DELETED", userId);

        try {
            rabbitTemplate.convertAndSend(syncExchange, syncUserRoutingKey, message);
            logger.info("Published USER_DELETED event for user: {}", userId);
        } catch (Exception e) {
            logger.error("Failed to publish USER_DELETED event: {}", e.getMessage());
        }
    }

    public void publishUserUpdated(UUID userId, String username) {
        SyncMessage message = new SyncMessage("USER_UPDATED", userId);
        message.setUsername(username);

        try {
            rabbitTemplate.convertAndSend(syncExchange, syncUserRoutingKey, message);
            logger.info("Published USER_UPDATED event for user: {}", userId);
        } catch (Exception e) {
            logger.error("Failed to publish USER_UPDATED event: {}", e.getMessage());
        }
    }
}

