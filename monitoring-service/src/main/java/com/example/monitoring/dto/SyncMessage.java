package com.example.monitoring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class SyncMessage {

    @JsonProperty("event_type")
    private String eventType; // USER_CREATED, USER_DELETED, DEVICE_CREATED, DEVICE_DELETED, DEVICE_UPDATED

    @JsonProperty("entity_id")
    private UUID entityId;

    @JsonProperty("username")
    private String username; // For user events

    @JsonProperty("device_name")
    private String deviceName; // For device events

    @JsonProperty("max_hourly_consumption")
    private Double maxHourlyConsumption; // For device events

    @JsonProperty("assigned_user_id")
    private UUID assignedUserId; // For device assignment events

    public SyncMessage() {
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

    @Override
    public String toString() {
        return "SyncMessage{" +
                "eventType='" + eventType + '\'' +
                ", entityId=" + entityId +
                ", username='" + username + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", maxHourlyConsumption=" + maxHourlyConsumption +
                ", assignedUserId=" + assignedUserId +
                '}';
    }
}

