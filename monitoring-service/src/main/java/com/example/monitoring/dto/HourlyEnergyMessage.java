package com.example.monitoring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

public class HourlyEnergyMessage {

    @JsonProperty("device_id")
    private UUID deviceId;

    @JsonProperty("hour_timestamp")
    private LocalDateTime hourTimestamp;

    @JsonProperty("total_consumption")
    private Double totalConsumption;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    public HourlyEnergyMessage() {
    }

    public HourlyEnergyMessage(UUID deviceId, LocalDateTime hourTimestamp, Double totalConsumption, LocalDateTime timestamp) {
        this.deviceId = deviceId;
        this.hourTimestamp = hourTimestamp;
        this.totalConsumption = totalConsumption;
        this.timestamp = timestamp;
    }

    public UUID getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(UUID deviceId) {
        this.deviceId = deviceId;
    }

    public LocalDateTime getHourTimestamp() {
        return hourTimestamp;
    }

    public void setHourTimestamp(LocalDateTime hourTimestamp) {
        this.hourTimestamp = hourTimestamp;
    }

    public Double getTotalConsumption() {
        return totalConsumption;
    }

    public void setTotalConsumption(Double totalConsumption) {
        this.totalConsumption = totalConsumption;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "HourlyEnergyMessage{" +
                "deviceId=" + deviceId +
                ", hourTimestamp=" + hourTimestamp +
                ", totalConsumption=" + totalConsumption +
                ", timestamp=" + timestamp +
                '}';
    }
}
