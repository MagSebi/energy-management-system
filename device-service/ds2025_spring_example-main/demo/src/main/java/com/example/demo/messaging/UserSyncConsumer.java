package com.example.demo.messaging;

import com.example.demo.entities.User;
import com.example.demo.repositories.DeviceRepository;
import com.example.demo.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserSyncConsumer {

    private static final Logger logger = LoggerFactory.getLogger(UserSyncConsumer.class);

    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;

    public UserSyncConsumer(UserRepository userRepository, DeviceRepository deviceRepository) {
        this.userRepository = userRepository;
        this.deviceRepository = deviceRepository;
    }

    @RabbitListener(queues = "${rabbitmq.queue.device.sync.user}")
    @Transactional
    public void handleUserSync(SyncMessage message) {
        logger.info("Received user sync message in device-service: {}", message);
        try {
            switch (message.getEventType()) {
                case "USER_CREATED":
                    if (message.getEntityId() == null || message.getUsername() == null) {
                        logger.error("USER_CREATED missing fields");
                        return;
                    }
                    if (userRepository.existsById(message.getEntityId())) {
                        logger.warn("User already exists: {}", message.getEntityId());
                        return;
                    }
                    userRepository.save(new User(message.getEntityId(), message.getUsername()));
                    logger.info("Stored user {} in device-db", message.getEntityId());
                    break;

                case "USER_DELETED":
                    if (message.getEntityId() == null) {
                        logger.error("USER_DELETED missing entity_id");
                        return;
                    }
                    // unassign devices for this user
                    deviceRepository.findByAssignedUserId(message.getEntityId()).forEach(device -> {
                        device.setAssignedUserId(null);
                    });
                    deviceRepository.flush();
                    userRepository.findById(message.getEntityId()).ifPresent(user -> {
                        userRepository.deleteById(message.getEntityId());
                        logger.info("Deleted user {} and unassigned their devices", message.getEntityId());
                    });
                    break;

                case "USER_UPDATED":
                    if (message.getEntityId() == null || message.getUsername() == null) {
                        logger.warn("USER_UPDATED missing fields, skipping");
                        return;
                    }
                    userRepository.findById(message.getEntityId()).ifPresentOrElse(user -> {
                        user.setUsername(message.getUsername());
                        userRepository.save(user);
                        logger.info("Updated user {} username to {}", message.getEntityId(), message.getUsername());
                    }, () -> {
                        // if not present, create it to stay in sync
                        userRepository.save(new User(message.getEntityId(), message.getUsername()));
                        logger.info("Created missing user {} on USER_UPDATED", message.getEntityId());
                    });
                    break;

                default:
                    logger.warn("Unknown user event type: {}", message.getEventType());
            }
        } catch (Exception e) {
            logger.error("Error handling user sync message: {}", message, e);
            throw e;
        }
    }
}
