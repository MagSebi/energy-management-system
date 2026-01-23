package com.example.demo.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class SyncMessage {

    @JsonProperty("event_type")
    private String eventType;

    @JsonProperty("entity_id")
    private UUID entityId;

    @JsonProperty("username")
    private String username;

    @JsonProperty("device_name")
    private String deviceName;

    @JsonProperty("max_hourly_consumption")
    private Double maxHourlyConsumption;

    @JsonProperty("assigned_user_id")
    private UUID assignedUserId;

    public SyncMessage() {
    }

    public SyncMessage(String eventType, UUID entityId) {
        this.eventType = eventType;
        this.entityId = entityId;
    }

    // Getters and Setters
    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public Double getMaxHourlyConsumption() {
        return maxHourlyConsumption;
    }

    public void setMaxHourlyConsumption(Double maxHourlyConsumption) {
        this.maxHourlyConsumption = maxHourlyConsumption;
    }

    public UUID getAssignedUserId() {
        return assignedUserId;
    }

    public void setAssignedUserId(UUID assignedUserId) {
        this.assignedUserId = assignedUserId;
    }
}

