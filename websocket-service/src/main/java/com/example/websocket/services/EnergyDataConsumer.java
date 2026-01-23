package com.example.websocket.services;

import com.example.websocket.dto.HourlyEnergyMessage;
import com.example.websocket.dto.OverconsumptionAlert;
import com.example.websocket.handlers.EnergyDataWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class EnergyDataConsumer {

    private static final Logger logger = LoggerFactory.getLogger(EnergyDataConsumer.class);

    private final EnergyDataWebSocketHandler webSocketHandler;

    public EnergyDataConsumer(EnergyDataWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @RabbitListener(queues = "${rabbitmq.queue.energy}")
    public void consumeEnergyData(HourlyEnergyMessage message) {
        logger.info("Received hourly energy data: device={}, hour={}, consumption={}",
                message.getDeviceId(), message.getHourTimestamp(), message.getTotalConsumption());

        try {
            // Broadcast to WebSocket subscribers
            webSocketHandler.broadcastEnergyData(message.getDeviceId(), message);
            logger.debug("Broadcasted energy data via WebSocket for device: {}", message.getDeviceId());
        } catch (Exception e) {
            logger.error("Error processing energy data: {}", message, e);
            throw e;
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.alert}")
    public void consumeOverconsumptionAlert(Map<String, Object> alertData) {
        logger.warn("Received overconsumption alert: {}", alertData);

        try {
            // Convert map to alert object
            OverconsumptionAlert alert = new OverconsumptionAlert();
            if (alertData.containsKey("deviceId")) {
                alert.setDeviceId(java.util.UUID.fromString(alertData.get("deviceId").toString()));
            }
            if (alertData.containsKey("totalConsumption")) {
                alert.setTotalConsumption(Double.parseDouble(alertData.get("totalConsumption").toString()));
            }
            if (alertData.containsKey("threshold")) {
                alert.setThreshold(Double.parseDouble(alertData.get("threshold").toString()));
            }
            if (alertData.containsKey("message")) {
                alert.setMessage(alertData.get("message").toString());
            }
            if (alertData.containsKey("timestamp")) {
                alert.setTimestamp(java.time.LocalDateTime.parse(alertData.get("timestamp").toString()));
            }

            // Broadcast alert to all energy subscribers
            if (alert.getDeviceId() != null) {
                webSocketHandler.broadcastOverconsumptionAlert(alert);
                logger.info("Broadcasted overconsumption alert for device: {}", alert.getDeviceId());
            }
        } catch (Exception e) {
            logger.error("Error processing overconsumption alert: {}", alertData, e);
            // Don't throw - we want to continue processing other messages
        }
    }
}
