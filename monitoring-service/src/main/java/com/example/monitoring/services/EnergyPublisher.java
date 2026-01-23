package com.example.monitoring.services;

import com.example.monitoring.dto.HourlyEnergyMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EnergyPublisher {

    private static final Logger logger = LoggerFactory.getLogger(EnergyPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.energy:energy.hourly.exchange}")
    private String energyExchange;

    @Value("${rabbitmq.routing.key.energy:energy.hourly.key}")
    private String energyRoutingKey;

    public EnergyPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishHourlyEnergy(HourlyEnergyMessage message) {
        try {
            rabbitTemplate.convertAndSend(energyExchange, energyRoutingKey, message);
            logger.info("Published hourly energy data: device={}, hour={}, consumption={}",
                    message.getDeviceId(), message.getHourTimestamp(), message.getTotalConsumption());
        } catch (Exception e) {
            logger.error("Failed to publish hourly energy data: {}", message, e);
        }
    }
}
