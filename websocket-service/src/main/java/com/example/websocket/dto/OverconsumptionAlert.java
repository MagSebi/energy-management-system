package com.example.websocket.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class OverconsumptionAlert {
    private UUID deviceId;
    private Double totalConsumption;
    private Double threshold;
    private LocalDateTime timestamp;
    private String message;

    public OverconsumptionAlert() {}

    public OverconsumptionAlert(UUID deviceId, Double totalConsumption, Double threshold, LocalDateTime timestamp, String message) {
        this.deviceId = deviceId;
        this.totalConsumption = totalConsumption;
        this.threshold = threshold;
        this.timestamp = timestamp;
        this.message = message;
    }

    public UUID getDeviceId() { return deviceId; }
    public void setDeviceId(UUID deviceId) { this.deviceId = deviceId; }

    public Double getTotalConsumption() { return totalConsumption; }
    public void setTotalConsumption(Double totalConsumption) { this.totalConsumption = totalConsumption; }

    public Double getThreshold() { return threshold; }
    public void setThreshold(Double threshold) { this.threshold = threshold; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
