package com.example.monitoring.services;

import com.example.monitoring.dto.DeviceDataMessage;
import com.example.monitoring.dto.HourlyEnergyMessage;
import com.example.monitoring.entities.DeviceMeasurement;
import com.example.monitoring.entities.HourlyEnergyConsumption;
import com.example.monitoring.repositories.DeviceMeasurementRepository;
import com.example.monitoring.repositories.HourlyEnergyConsumptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
public class DeviceDataConsumer {

    private static final Logger logger = LoggerFactory.getLogger(DeviceDataConsumer.class);

    private final DeviceMeasurementRepository measurementRepository;
    private final HourlyEnergyConsumptionRepository hourlyConsumptionRepository;
    private final EnergyPublisher energyPublisher;
    private final RabbitTemplate rabbitTemplate;

    @Value("${monitoring.overconsumption.threshold.kwh:50}")
    private Double overconsumptionThreshold;

    @Value("${rabbitmq.exchange.energy:energy.hourly.exchange}")
    private String energyExchange;

    public DeviceDataConsumer(
            DeviceMeasurementRepository measurementRepository,
            HourlyEnergyConsumptionRepository hourlyConsumptionRepository,
            EnergyPublisher energyPublisher,
            RabbitTemplate rabbitTemplate) {
        this.measurementRepository = measurementRepository;
        this.hourlyConsumptionRepository = hourlyConsumptionRepository;
        this.energyPublisher = energyPublisher;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = "${rabbitmq.queue.device.data}")
    @Transactional
    public void consumeDeviceData(DeviceDataMessage message) {
        logger.info("Received device data: {}", message);

        try {
            // 1. Store the raw measurement
            DeviceMeasurement measurement = new DeviceMeasurement(
                    message.getDeviceId(),
                    message.getTimestamp(),
                    message.getMeasurementValue()
            );
            measurementRepository.save(measurement);
            logger.info("Saved device measurement for device: {}", message.getDeviceId());

            // 2. Update hourly consumption
            updateHourlyConsumption(message);

        } catch (Exception e) {
            logger.error("Error processing device data: {}", message, e);
            throw e; // Re-throw to let RabbitMQ handle retry logic
        }
    }

    private void updateHourlyConsumption(DeviceDataMessage message) {
        // Truncate timestamp to the start of the hour
        LocalDateTime hourTimestamp = message.getTimestamp().truncatedTo(ChronoUnit.HOURS);

        // Find or create hourly consumption record
        HourlyEnergyConsumption hourlyConsumption = hourlyConsumptionRepository
                .findByDeviceIdAndHourTimestamp(message.getDeviceId(), hourTimestamp)
                .orElse(new HourlyEnergyConsumption(message.getDeviceId(), hourTimestamp, 0.0));

        // Add the measurement value to the total
        hourlyConsumption.setTotalConsumption(
                hourlyConsumption.getTotalConsumption() + message.getMeasurementValue()
        );

        hourlyConsumptionRepository.save(hourlyConsumption);
        logger.info("Updated hourly consumption for device {} at {}: total = {}",
                message.getDeviceId(), hourTimestamp, hourlyConsumption.getTotalConsumption());
        
        // Publish hourly energy update to RabbitMQ
        HourlyEnergyMessage energyMessage = new HourlyEnergyMessage(
            message.getDeviceId(),
            hourTimestamp,
            hourlyConsumption.getTotalConsumption(),
            LocalDateTime.now()
        );
        energyPublisher.publishHourlyEnergy(energyMessage);

        // Check for overconsumption and emit alert if needed
        checkOverconsumption(message.getDeviceId(), hourlyConsumption.getTotalConsumption());
    }

    private void checkOverconsumption(java.util.UUID deviceId, Double totalConsumption) {
        if (totalConsumption > overconsumptionThreshold) {
            logger.warn("ALERT: Device {} exceeded consumption threshold! Consumption: {} kWh, Threshold: {} kWh",
                    deviceId, totalConsumption, overconsumptionThreshold);

            // Create and publish overconsumption alert
            Map<String, Object> alert = new HashMap<>();
            alert.put("deviceId", deviceId);
            alert.put("totalConsumption", totalConsumption);
            alert.put("threshold", overconsumptionThreshold);
            alert.put("timestamp", LocalDateTime.now());
            alert.put("message", "Supraconsum de energie detectat!");

            try {
                // Send alert via energy exchange (will be picked up by WebSocket)
                rabbitTemplate.convertAndSend(energyExchange, "alert.overconsumption", alert);
                logger.info("Published overconsumption alert for device: {}", deviceId);
            } catch (Exception e) {
                logger.error("Failed to publish overconsumption alert: {}", deviceId, e);
            }
        }
    }
}
