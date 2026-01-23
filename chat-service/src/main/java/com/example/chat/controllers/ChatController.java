package com.example.chat.controllers;

import com.example.chat.dto.ChatMessage;
import com.example.chat.services.ChatPublisher;
import com.example.chat.services.AIService;
import com.example.chat.services.AdminDetectionService;
import com.example.chat.services.RuleEngine;
import com.example.chat.services.ChatSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/message")
@CrossOrigin(origins = "*")
public class ChatController {
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final ChatPublisher publisher;
    private final AdminDetectionService adminDetectionService;
    private final RuleEngine ruleEngine;
    private final AIService aiService;
    private final ChatSessionManager chatSessionManager;

    public ChatController(ChatPublisher publisher, AdminDetectionService adminDetectionService,
                         RuleEngine ruleEngine, AIService aiService, ChatSessionManager chatSessionManager) {
        this.publisher = publisher;
        this.adminDetectionService = adminDetectionService;
        this.ruleEngine = ruleEngine;
        this.aiService = aiService;
        this.chatSessionManager = chatSessionManager;
    }

    @PostMapping("")
    public ResponseEntity<?> postMessage(@RequestBody Map<String, String> payload) {
        try {
            logger.info("[CHAT-CONTROLLER] Received payload: {}", payload);
            String content = payload.getOrDefault("content", "");
            String userIdStr = payload.get("userId");
            String adminFlag = payload.getOrDefault("isAdmin", "false");
            
            if (userIdStr == null) {
                logger.warn("[CHAT-CONTROLLER] Missing userId in payload");
                return ResponseEntity.badRequest().body(Map.of("error", "missing userId"));
            }
            
            UUID userId = UUID.fromString(userIdStr);
            logger.info("[CHAT-CONTROLLER] Processing message - userId: {}, isAdmin: {}, content: {}", userId, adminFlag, content);

            if ("true".equalsIgnoreCase(adminFlag)) {
                logger.info("[CHAT-CONTROLLER] Admin sending direct message to user {}", userId);
                ChatMessage adminMsg = new ChatMessage(userId, content, false, false, true, LocalDateTime.now());
                publisher.publish(adminMsg);
                chatSessionManager.addMessageToSession(userId, adminMsg);
                logger.info("[CHAT-CONTROLLER] Published admin->user message to RabbitMQ");
                return ResponseEntity.ok(Map.of("status", "sent-to-user"));
            }

            logger.info("[CHAT-CONTROLLER] User {} message - checking for explicit admin request...", userId);
            boolean requestsAdmin = adminDetectionService.isAdminRequestExplicit(content);
            
            if (requestsAdmin) {
                logger.info("[CHAT-CONTROLLER] User explicitly requested admin - routing to admin");
                ChatMessage adminRequest = new ChatMessage(userId, "[CLIENT: " + userId + "] " + content, true, true, false, LocalDateTime.now());
                publisher.publish(adminRequest);
                chatSessionManager.addMessageToSession(userId, adminRequest);
                return ResponseEntity.ok(Map.of(
                    "status", "escalated-to-admin",
                    "message", "Mesajul dvs. a fost transmis catre un administrator. Va multumim pentru rabdare!"
                ));
            }

            Optional<String> ruleResponse = ruleEngine.matchResponse(content);
            if (ruleResponse.isPresent()) {
                logger.info("[CHAT-CONTROLLER] Rule matched - sending predefined response");
                String response = ruleResponse.get();
                ChatMessage replyMessage = new ChatMessage(userId, response, false, false, true, LocalDateTime.now());
                publisher.publish(replyMessage);
                chatSessionManager.addMessageToSession(userId, replyMessage);
                return ResponseEntity.ok(Map.of(
                    "status", "auto-replied-rule",
                    "reply", response,
                    "type", "rule-based"
                ));
            }

            logger.info("[CHAT-CONTROLLER] No rule matched - forwarding to Gemini AI");
            Optional<String> aiResponse = aiService.generateResponse(content);
            
            if (aiResponse.isPresent()) {
                logger.info("[CHAT-CONTROLLER] Gemini generated response");
                String response = aiResponse.get();
                ChatMessage replyMessage = new ChatMessage(userId, response, false, false, true, LocalDateTime.now());
                publisher.publish(replyMessage);
                chatSessionManager.addMessageToSession(userId, replyMessage);
                return ResponseEntity.ok(Map.of(
                    "status", "ai-replied",
                    "reply", response,
                    "type", "ai-driven"
                ));
            }

            logger.warn("[CHAT-CONTROLLER] AI failed - sending fallback message");
            String fallbackMessage = "Nu am putut genera un raspuns. Te rog sa reformulezi sau contacteaza un administrator.";
            return ResponseEntity.ok(Map.of(
                "status", "fallback",
                "message", fallbackMessage
            ));
        } catch (Exception e) {
            logger.error("[CHAT-CONTROLLER] Error processing message", e);
            return ResponseEntity.status(500).body(Map.of("error", "internal"));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}

