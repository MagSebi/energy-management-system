package com.example.websocket.services;

import com.example.websocket.dto.ChatMessage;
import com.example.websocket.handlers.ChatWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class ChatMessageConsumer {
    private static final Logger logger = LoggerFactory.getLogger(ChatMessageConsumer.class);

    private final ChatWebSocketHandler chatHandler;

    public ChatMessageConsumer(ChatWebSocketHandler chatHandler) {
        this.chatHandler = chatHandler;
    }

    @RabbitListener(queues = "${rabbitmq.queue.chat}")
    public void consume(ChatMessage message) {
        logger.info("[CHAT-CONSUMER] Received message - userId: {}, fromUser: {}, toAdmin: {}, toUser: {}, content: {}", 
                message.getUserId(), message.isFromUser(), message.isToAdmin(), message.isToUser(), message.getContent());
        try {
            chatHandler.broadcast(message);
            logger.info("[CHAT-CONSUMER] Successfully broadcasted message");
        } catch (Exception e) {
            logger.error("[CHAT-CONSUMER] Failed broadcasting chat message: {}", message, e);
        }
    }
}
