package com.example.demo.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeviceSyncConsumer {

    private static final Logger logger = LoggerFactory.getLogger(DeviceSyncConsumer.class);

    public DeviceSyncConsumer() {
    }

    @RabbitListener(queues = "${rabbitmq.queue.user.sync.device}")
    @Transactional
    public void handleDeviceSync(SyncMessage message) {
        logger.info("Received device sync message: {}", message);

        try {
            switch (message.getEventType()) {
                case "DEVICE_CREATED":
                    logger.info("Device created: {} - {}", message.getEntityId(), message.getDeviceName());
                    // User-service can maintain a denormalized view of devices if needed
                    // For now, just log the event
                    break;

                case "DEVICE_UPDATED":
                    logger.info("Device updated: {} - {}", message.getEntityId(), message.getDeviceName());
                    break;

                case "DEVICE_DELETED":
                    logger.info("Device deleted: {}", message.getEntityId());
                    // Optionally clean up any user-device relationships
                    break;

                case "DEVICE_ASSIGNED":
                    if (message.getAssignedUserId() != null) {
                        logger.info("Device {} assigned to user: {}", message.getEntityId(), message.getAssignedUserId());
                        // User-service can track which devices are assigned to which users
                        // This enables queries like "get all devices for a user" without calling device-service
                    } else {
                        logger.warn("DEVICE_ASSIGNED event missing assigned_user_id");
                    }
                    break;

                case "DEVICE_UNASSIGNED":
                    logger.info("Device {} unassigned", message.getEntityId());
                    // Clean up assignment tracking
                    break;

                default:
                    logger.warn("Unknown device event type: {}", message.getEventType());
            }
        } catch (Exception e) {
            logger.error("Error processing device sync message: {}", message, e);
            throw e;
        }
    }
}
