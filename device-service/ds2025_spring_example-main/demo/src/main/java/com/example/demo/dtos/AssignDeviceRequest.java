package com.example.demo.dtos;

import java.util.UUID;

public class AssignDeviceRequest {
    private UUID deviceId;
    private UUID userId;

    public UUID getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(UUID deviceId) {
        this.deviceId = deviceId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}
