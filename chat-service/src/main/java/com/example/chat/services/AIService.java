package com.example.chat.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AIService {
    private static final Logger logger = LoggerFactory.getLogger(AIService.class);

    private static final String SYSTEM_PROMPT =
        "Tu esti un asistent suport pentru clientii platformei de management energetic. " +
        "Raspunde in limba utilizatorului, prietenos si concis (sub 200 caractere). " +
        "Daca nu poti raspunde, sugereaza contactarea suportului.";

    @Value("${ai.gemini.key:}")
    private String geminiKey;

    @Value("${ai.gemini.model:gemini-2.5-flash}")
    private String geminiModel;

    private final WebClient webClient;

    public AIService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Optional<String> generateResponse(String userMessage) {
        if (geminiKey == null || geminiKey.isBlank()) {
            logger.warn("[GEMINI] Missing API key; AI disabled");
            return Optional.empty();
        }

        try {
            return generateGeminiResponse(userMessage);
        } catch (Exception e) {
            logger.error("[GEMINI] Error generating response: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    private Optional<String> generateGeminiResponse(String userMessage) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + geminiModel + ":generateContent?key=" + geminiKey;

        Map<String, Object> request = new HashMap<>();
        Map<String, Object> systemPart = Map.of("text", SYSTEM_PROMPT);
        request.put("systemInstruction", Map.of("parts", java.util.List.of(systemPart)));

        Map<String, Object> userPart = Map.of("text", userMessage);
        request.put("contents", java.util.List.of(Map.of("parts", java.util.List.of(userPart))));

        Map<String, Object> response = webClient.post()
                .uri(url)
                .header("Content-Type", "application/json")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response != null) {
            Object candidates = response.get("candidates");
            if (candidates instanceof java.util.List && !((java.util.List<?>) candidates).isEmpty()) {
                Map<String, Object> first = (Map<String, Object>) ((java.util.List<?>) candidates).get(0);
                Object contentObj = first.get("content");
                if (contentObj instanceof Map<?, ?> contentMap) {
                    Object partsObj = contentMap.get("parts");
                    if (partsObj instanceof java.util.List && !((java.util.List<?>) partsObj).isEmpty()) {
                        Map<String, Object> part = (Map<String, Object>) ((java.util.List<?>) partsObj).get(0);
                        Object textObj = part.get("text");
                        if (textObj instanceof String text && !text.isBlank()) {
                            logger.info("[GEMINI] Generated response: {}", text);
                            return Optional.of(text.trim());
                        }
                    }
                }
            }
        }

        logger.warn("[GEMINI] Empty or invalid response from API");
        return Optional.empty();
    }
}
