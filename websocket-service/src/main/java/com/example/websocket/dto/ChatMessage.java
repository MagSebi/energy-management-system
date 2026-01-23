package com.example.websocket.dto;

import java.util.UUID;
import java.time.LocalDateTime;

public class ChatMessage {
    private UUID userId;
    private String content;
    private boolean fromUser; // true if message authored by user
    private boolean toAdmin;  // deliver to admin sessions
    private boolean toUser;   // deliver to specific user
    private LocalDateTime timestamp;

    public ChatMessage() {}

    public ChatMessage(UUID userId, String content, boolean fromUser, boolean toAdmin, boolean toUser, LocalDateTime timestamp) {
        this.userId = userId;
        this.content = content;
        this.fromUser = fromUser;
        this.toAdmin = toAdmin;
        this.toUser = toUser;
        this.timestamp = timestamp;
    }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isFromUser() { return fromUser; }
    public void setFromUser(boolean fromUser) { this.fromUser = fromUser; }

    public boolean isToAdmin() { return toAdmin; }
    public void setToAdmin(boolean toAdmin) { this.toAdmin = toAdmin; }

    public boolean isToUser() { return toUser; }
    public void setToUser(boolean toUser) { this.toUser = toUser; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
