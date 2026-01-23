package com.example.chat.services;

import com.example.chat.dto.ChatMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ChatPublisher {
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.chat}")
    private String chatExchange;

    @Value("${rabbitmq.routing.key.chat}")
    private String chatRoutingKey;

    public ChatPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(ChatMessage message) {
        rabbitTemplate.convertAndSend(chatExchange, chatRoutingKey, message);
    }
}
