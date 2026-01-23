package com.example.websocket.handlers;

import com.example.websocket.dto.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    private final ObjectMapper objectMapper;

    public ChatWebSocketHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    // Admin sessions (can be multiple)
    private final CopyOnWriteArraySet<WebSocketSession> adminSessions = new CopyOnWriteArraySet<>();

    // User sessions mapped by userId
    private final Map<UUID, CopyOnWriteArraySet<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String query = session.getUri() != null ? session.getUri().getQuery() : null;
        logger.info("[WS-CONNECT] Chat connection attempt - URI: {}, Query: {}", session.getUri(), query);
        String role = queryParam(query, "role");
        String userIdStr = queryParam(query, "user_id");
        logger.info("[WS-CONNECT] Parsed role: {}, userIdStr: {}", role, userIdStr);
        
        if (role != null && role.equalsIgnoreCase("ADMIN")) {
            adminSessions.add(session);
            logger.info("[WS-CONNECT] ✓ ADMIN connected. Total admin sessions: {}", adminSessions.size());
        } else if (userIdStr != null) {
            try {
                UUID userId = UUID.fromString(userIdStr);
                userSessions.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(session);
                logger.info("[WS-CONNECT] ✓ USER {} connected. Sessions for this user: {}", userId, userSessions.get(userId).size());
            } catch (IllegalArgumentException e) {
                logger.warn("[WS-CONNECT] Invalid user_id in WebSocket query: {}", userIdStr);
            }
        } else {
            logger.warn("[WS-CONNECT] Missing role or user_id in connection", session.getId());
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Echo is disabled; messages are delivered via RabbitMQ consumer
        logger.debug("Ignoring incoming text on chat WebSocket (server-side push only)");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        adminSessions.remove(session);
        userSessions.values().forEach(set -> set.remove(session));
        logger.info("WebSocket chat closed: {}", session.getId());
    }

    public void broadcast(ChatMessage msg) throws IOException {
        String json = objectMapper.writeValueAsString(msg);
        TextMessage tm = new TextMessage(json);
        
        logger.info("[BROADCAST] Message - toAdmin: {}, toUser: {}, userId: {}", msg.isToAdmin(), msg.isToUser(), msg.getUserId());
        logger.info("[BROADCAST] Current admin sessions: {}, user sessions keys: {}", adminSessions.size(), userSessions.keySet());
        
        if (msg.isToAdmin()) {
            logger.info("[BROADCAST] Routing to {} admin session(s)", adminSessions.size());
            for (WebSocketSession s : adminSessions) {
                if (s.isOpen()) {
                    s.sendMessage(tm);
                    logger.info("[BROADCAST] Sent to admin session {}", s.getId());
                } else {
                    logger.warn("[BROADCAST] Admin session {} is closed", s.getId());
                }
            }
        }
        
        if (msg.isToUser() && msg.getUserId() != null) {
            CopyOnWriteArraySet<WebSocketSession> sessions = userSessions.get(msg.getUserId());
            logger.info("[BROADCAST] Looking for user {} sessions. Found: {}", msg.getUserId(), sessions != null ? sessions.size() : 0);
            if (sessions != null && !sessions.isEmpty()) {
                for (WebSocketSession s : sessions) {
                    if (s.isOpen()) {
                        s.sendMessage(tm);
                        logger.info("[BROADCAST] Sent to user {} session {}", msg.getUserId(), s.getId());
                    } else {
                        logger.warn("[BROADCAST] User {} session {} is closed", msg.getUserId(), s.getId());
                    }
                }
            } else {
                logger.warn("[BROADCAST] No open sessions found for user {}", msg.getUserId());
            }
        }
    }

    private String queryParam(String query, String key) {
        if (query == null) return null;
        for (String part : query.split("&")) {
            String[] kv = part.split("=");
            if (kv.length == 2 && kv[0].equals(key)) return kv[1];
        }
        return null;
    }
}
