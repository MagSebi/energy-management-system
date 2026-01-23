package com.example.websocket.config;

import com.example.websocket.handlers.EnergyDataWebSocketHandler;
import com.example.websocket.handlers.ChatWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final EnergyDataWebSocketHandler energyDataWebSocketHandler;
    private final ChatWebSocketHandler chatWebSocketHandler;

    public WebSocketConfig(EnergyDataWebSocketHandler energyDataWebSocketHandler,
                           ChatWebSocketHandler chatWebSocketHandler) {
        this.energyDataWebSocketHandler = energyDataWebSocketHandler;
        this.chatWebSocketHandler = chatWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(energyDataWebSocketHandler, "/ws/energy")
                .setAllowedOrigins("*");
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .setAllowedOrigins("*");
    }
}
