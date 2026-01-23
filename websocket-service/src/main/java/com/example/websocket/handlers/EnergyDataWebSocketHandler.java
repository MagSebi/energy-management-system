package com.example.websocket.handlers;

import com.example.websocket.dto.OverconsumptionAlert;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class EnergyDataWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(EnergyDataWebSocketHandler.class);

    // Map: deviceId -> Set of WebSocket sessions subscribed to that device
    private final Map<UUID, CopyOnWriteArraySet<WebSocketSession>> deviceSubscriptions = new ConcurrentHashMap<>();
    
    // Set of sessions subscribed to all devices
    private final CopyOnWriteArraySet<WebSocketSession> allDevicesSessions = new CopyOnWriteArraySet<>();

        private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("WebSocket connection established: {}", session.getId());

        // Extract device_id from query parameters
        String query = session.getUri().getQuery();
        if (query != null && query.contains("device_id=")) {
            String deviceIdStr = query.split("device_id=")[1].split("&")[0];
            try {
                UUID deviceId = UUID.fromString(deviceIdStr);

                // Subscribe this session to the specific device
                deviceSubscriptions
                        .computeIfAbsent(deviceId, k -> new CopyOnWriteArraySet<>())
                        .add(session);

                logger.info("Session {} subscribed to device {}", session.getId(), deviceId);
            } catch (IllegalArgumentException e) {
                logger.error("Invalid device_id in WebSocket connection: {}", deviceIdStr);
                session.close(CloseStatus.BAD_DATA);
            }
        } else {
            // No device_id provided, subscribe to all devices
            allDevicesSessions.add(session);
            logger.info("Session {} subscribed to all devices", session.getId());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info("WebSocket connection closed: {} with status {}", session.getId(), status);

        // Remove session from all subscriptions
        deviceSubscriptions.values().forEach(sessions -> sessions.remove(session));
        allDevicesSessions.remove(session);

        // Clean up empty subscription sets
        deviceSubscriptions.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        logger.debug("Received message from session {}: {}", session.getId(), message.getPayload());
        // We don't expect messages from clients, but log them if received
    }

    /**
     * Broadcast hourly energy data to all sessions subscribed to that device or all devices
     */
    public void broadcastEnergyData(UUID deviceId, Object energyData) {
        CopyOnWriteArraySet<WebSocketSession> specificSessions = deviceSubscriptions.get(deviceId);

        try {
            String jsonMessage = objectMapper.writeValueAsString(energyData);
            TextMessage textMessage = new TextMessage(jsonMessage);

            // Send to device-specific subscribers
            if (specificSessions != null && !specificSessions.isEmpty()) {
                for (WebSocketSession session : specificSessions) {
                    sendToSession(session, textMessage, deviceId);
                }
            }

            // Send to all-devices subscribers
            for (WebSocketSession session : allDevicesSessions) {
                sendToSession(session, textMessage, deviceId);
            }

        } catch (Exception e) {
            logger.error("Error broadcasting energy data for device {}: {}", deviceId, e.getMessage());
        }
    }

    /**
     * Broadcast overconsumption alert to all energy subscribers
     */
    public void broadcastOverconsumptionAlert(OverconsumptionAlert alert) {
        UUID deviceId = alert.getDeviceId();

        try {
            String jsonMessage = objectMapper.writeValueAsString(alert);
            TextMessage textMessage = new TextMessage(jsonMessage);

            // Send to device-specific subscribers
            CopyOnWriteArraySet<WebSocketSession> specificSessions = deviceSubscriptions.get(deviceId);
            if (specificSessions != null && !specificSessions.isEmpty()) {
                for (WebSocketSession session : specificSessions) {
                    sendToSession(session, textMessage, deviceId);
                }
            }

            // Send to all-devices subscribers (they should see all alerts)
            for (WebSocketSession session : allDevicesSessions) {
                sendToSession(session, textMessage, deviceId);
            }

            logger.info("Broadcasted overconsumption alert for device: {}", deviceId);
        } catch (Exception e) {
            logger.error("Error broadcasting overconsumption alert for device {}: {}", deviceId, e.getMessage());
        }
    }

    private void sendToSession(WebSocketSession session, TextMessage message, UUID deviceId) {
        if (session.isOpen()) {
            try {
                session.sendMessage(message);
                logger.debug("Sent message to session {} for device {}", session.getId(), deviceId);
            } catch (IOException e) {
                logger.error("Error sending message to session {}: {}", session.getId(), e.getMessage());
            }
        }
    }
}
