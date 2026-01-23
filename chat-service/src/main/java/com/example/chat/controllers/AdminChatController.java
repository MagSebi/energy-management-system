package com.example.chat.controllers;

import com.example.chat.services.ChatSessionManager;
import com.example.chat.dto.ChatMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin/chat")
@CrossOrigin(origins = "*")
public class AdminChatController {
    private final ChatSessionManager chatSessionManager;

    public AdminChatController(ChatSessionManager chatSessionManager) {
        this.chatSessionManager = chatSessionManager;
    }

    @GetMapping("/sessions")
    public ResponseEntity<?> getAllActiveSessions() {
        Map<UUID, List<ChatMessage>> sessions = chatSessionManager.getAllActiveSessions();
        
        Map<String, Object> response = new HashMap<>();
        response.put("totalSessions", sessions.size());
        response.put("sessions", sessions.entrySet().stream().map(entry -> 
            Map.of(
                "userId", entry.getKey(),
                "messageCount", entry.getValue().size(),
                "lastMessage", entry.getValue().isEmpty() ? null : entry.getValue().get(entry.getValue().size() - 1).getTimestamp()
            )
        ).toList());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/conversations/{userId}")
    public ResponseEntity<?> getUserConversation(@PathVariable UUID userId) {
        List<ChatMessage> messages = chatSessionManager.getSessionMessages(userId);
        
        return ResponseEntity.ok(Map.of(
            "userId", userId,
            "messageCount", messages.size(),
            "messages", messages
        ));
    }

    @DeleteMapping("/sessions/{userId}")
    public ResponseEntity<?> clearSession(@PathVariable UUID userId) {
        chatSessionManager.clearSession(userId);
        return ResponseEntity.ok(Map.of("status", "Session cleared for user " + userId));
    }
}
