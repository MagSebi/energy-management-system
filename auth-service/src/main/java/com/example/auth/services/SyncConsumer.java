package com.example.auth.services;

import com.example.auth.dto.SyncMessage;
import com.example.auth.entities.User;
import com.example.auth.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SyncConsumer {

    private static final Logger logger = LoggerFactory.getLogger(SyncConsumer.class);

    private final UserRepository userRepository;

    public SyncConsumer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @RabbitListener(queues = "${rabbitmq.queue.auth.sync.user}")
    @Transactional
    public void handleUserSync(SyncMessage message) {
        logger.info("Received sync message: {}", message);

        try {
            switch (message.getEventType()) {
                case "USER_CREATED":
                    // Auth service already created the user during registration;
                    // this event is informational; we can log or validate consistency.
                    logger.info("User created event for user: {}", message.getEntityId());
                    break;

                case "USER_DELETED":
                    // Remove user from auth DB if present
                    if (userRepository.existsById(message.getEntityId())) {
                        userRepository.deleteById(message.getEntityId());
                        logger.info("Deleted user from auth DB: {}", message.getEntityId());
                    } else {
                        logger.warn("User not found in auth DB for deletion: {}", message.getEntityId());
                    }
                    break;

                case "USER_UPDATED":
                    // Update username or other denormalized fields if needed
                    if (message.getUsername() != null) {
                        User user = userRepository.findById(message.getEntityId()).orElse(null);
                        if (user != null) {
                            // In this architecture, username changes typically happen in auth-service first,
                            // so this is informational. Log it for consistency.
                            logger.info("User updated event for user: {} - username: {}", message.getEntityId(), message.getUsername());
                        }
                    }
                    break;

                default:
                    logger.warn("Unknown event type: {}", message.getEventType());
            }
        } catch (Exception e) {
            logger.error("Error processing sync message: {}", message, e);
            throw e;
        }
    }
}
