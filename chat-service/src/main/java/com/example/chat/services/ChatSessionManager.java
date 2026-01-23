package com.example.chat.services;

import com.example.chat.dto.ChatMessage;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ChatSessionManager {
    private final Map<UUID, List<ChatMessage>> userSessions = new HashMap<>();

    public void addMessageToSession(UUID userId, ChatMessage message) {
        userSessions.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(message);
    }

    public List<ChatMessage> getSessionMessages(UUID userId) {
        return userSessions.getOrDefault(userId, List.of());
    }

    public Map<UUID, List<ChatMessage>> getAllActiveSessions() {
        return new HashMap<>(userSessions);
    }

    public void clearSession(UUID userId) {
        userSessions.remove(userId);
    }
}
