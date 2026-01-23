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

    @Value("${rabbitmq.routing.key.sync.device}")
    private String syncDeviceRoutingKey;

    public SyncPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishDeviceCreated(UUID deviceId, String deviceName, Double maxHourlyConsumption) {
        SyncMessage message = new SyncMessage("DEVICE_CREATED", deviceId);
        message.setDeviceName(deviceName);
        message.setMaxHourlyConsumption(maxHourlyConsumption);

        try {
            rabbitTemplate.convertAndSend(syncExchange, syncDeviceRoutingKey, message);
            logger.info("Published DEVICE_CREATED event for device: {}", deviceId);
        } catch (Exception e) {
            logger.error("Failed to publish DEVICE_CREATED event: {}", e.getMessage());
        }
    }

    public void publishDeviceUpdated(UUID deviceId, String deviceName, Double maxHourlyConsumption) {
        SyncMessage message = new SyncMessage("DEVICE_UPDATED", deviceId);
        message.setDeviceName(deviceName);
        message.setMaxHourlyConsumption(maxHourlyConsumption);

        try {
            rabbitTemplate.convertAndSend(syncExchange, syncDeviceRoutingKey, message);
            logger.info("Published DEVICE_UPDATED event for device: {}", deviceId);
        } catch (Exception e) {
            logger.error("Failed to publish DEVICE_UPDATED event: {}", e.getMessage());
        }
    }

    public void publishDeviceDeleted(UUID deviceId) {
        SyncMessage message = new SyncMessage("DEVICE_DELETED", deviceId);

        try {
            rabbitTemplate.convertAndSend(syncExchange, syncDeviceRoutingKey, message);
            logger.info("Published DEVICE_DELETED event for device: {}", deviceId);
        } catch (Exception e) {
            logger.error("Failed to publish DEVICE_DELETED event: {}", e.getMessage());
        }
    }

    public void publishDeviceAssigned(UUID deviceId, UUID userId, String deviceName) {
        SyncMessage message = new SyncMessage("DEVICE_ASSIGNED", deviceId);
        message.setAssignedUserId(userId);
        message.setDeviceName(deviceName);

        try {
            rabbitTemplate.convertAndSend(syncExchange, syncDeviceRoutingKey, message);
            logger.info("Published DEVICE_ASSIGNED event for device: {} to user: {}", deviceId, userId);
        } catch (Exception e) {
            logger.error("Failed to publish DEVICE_ASSIGNED event: {}", e.getMessage());
        }
    }

    public void publishDeviceUnassigned(UUID deviceId) {
        SyncMessage message = new SyncMessage("DEVICE_UNASSIGNED", deviceId);

        try {
            rabbitTemplate.convertAndSend(syncExchange, syncDeviceRoutingKey, message);
            logger.info("Published DEVICE_UNASSIGNED event for device: {}", deviceId);
        } catch (Exception e) {
            logger.error("Failed to publish DEVICE_UNASSIGNED event: {}", e.getMessage());
        }
    }
}

