package com.example.monitoring.services;

import com.example.monitoring.dto.SyncMessage;
import com.example.monitoring.entities.Device;
import com.example.monitoring.entities.User;
import com.example.monitoring.repositories.DeviceRepository;
import com.example.monitoring.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SyncConsumer {

    private static final Logger logger = LoggerFactory.getLogger(SyncConsumer.class);

    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;

    public SyncConsumer(UserRepository userRepository, DeviceRepository deviceRepository) {
        this.userRepository = userRepository;
        this.deviceRepository = deviceRepository;
    }

    @RabbitListener(queues = "${rabbitmq.queue.sync}")
    @Transactional
    public void consumeSyncMessage(SyncMessage message) {
        logger.info("Received sync message: {}", message);

        try {
            switch (message.getEventType()) {
                case "USER_CREATED":
                    handleUserCreated(message);
                    break;
                case "USER_DELETED":
                    handleUserDeleted(message);
                    break;
                case "DEVICE_CREATED":
                    handleDeviceCreated(message);
                    break;
                case "DEVICE_UPDATED":
                    handleDeviceUpdated(message);
                    break;
                case "DEVICE_DELETED":
                    handleDeviceDeleted(message);
                    break;
                case "DEVICE_ASSIGNED":
                    handleDeviceAssigned(message);
                    break;
                case "DEVICE_UNASSIGNED":
                    handleDeviceUnassigned(message);
                    break;
                default:
                    logger.warn("Unknown event type: {}", message.getEventType());
            }
        } catch (Exception e) {
            logger.error("Error processing sync message: {}", message, e);
            throw e;
        }
    }

    private void handleUserCreated(SyncMessage message) {
        User user = new User(message.getEntityId(), message.getUsername());
        userRepository.save(user);
        logger.info("Synced user creation: {}", message.getEntityId());
    }

    private void handleUserDeleted(SyncMessage message) {
        userRepository.deleteById(message.getEntityId());
        logger.info("Synced user deletion: {}", message.getEntityId());
    }

    private void handleDeviceCreated(SyncMessage message) {
        Device device = new Device(
                message.getEntityId(),
                message.getDeviceName(),
                message.getMaxHourlyConsumption()
        );
        deviceRepository.save(device);
        logger.info("Synced device creation: {}", message.getEntityId());
    }

    private void handleDeviceUpdated(SyncMessage message) {
        deviceRepository.findById(message.getEntityId()).ifPresent(device -> {
            device.setName(message.getDeviceName());
            device.setMaxHourlyConsumption(message.getMaxHourlyConsumption());
            deviceRepository.save(device);
            logger.info("Synced device update: {}", message.getEntityId());
        });
    }

    private void handleDeviceDeleted(SyncMessage message) {
        deviceRepository.deleteById(message.getEntityId());
        logger.info("Synced device deletion: {}", message.getEntityId());
    }

    private void handleDeviceAssigned(SyncMessage message) {
        deviceRepository.findById(message.getEntityId()).ifPresentOrElse(device -> {
            device.setAssignedUserId(message.getAssignedUserId());
            deviceRepository.save(device);
            logger.info("Synced device assignment: device {} assigned to user {}", 
                message.getEntityId(), message.getAssignedUserId());
        }, () -> {
            // Device not yet synced, create it with assignment
            Device device = new Device(
                message.getEntityId(),
                message.getDeviceName(),
                null // maxHourlyConsumption not in assignment event
            );
            device.setAssignedUserId(message.getAssignedUserId());
            deviceRepository.save(device);
            logger.warn("Device {} not found during assignment, created with partial data", message.getEntityId());
        });
    }

    private void handleDeviceUnassigned(SyncMessage message) {
        deviceRepository.findById(message.getEntityId()).ifPresent(device -> {
            device.setAssignedUserId(null);
            deviceRepository.save(device);
            logger.info("Synced device unassignment: device {} unassigned", message.getEntityId());
        });
    }
}

