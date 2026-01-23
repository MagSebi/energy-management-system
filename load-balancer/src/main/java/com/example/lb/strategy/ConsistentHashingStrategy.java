package com.example.lb.strategy;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

public class ConsistentHashingStrategy {
    private final List<String> routingKeys; // e.g., device.data.ingest.1, device.data.ingest.2

    public ConsistentHashingStrategy(List<String> routingKeys) {
        this.routingKeys = routingKeys;
    }

    public String selectKey(String deviceId) {
        int idx = Math.abs(hash(deviceId)) % routingKeys.size();
        return routingKeys.get(idx);
    }

    private int hash(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] h = md.digest(s.getBytes(StandardCharsets.UTF_8));
            int acc = 0;
            for (int i = 0; i < 4 && i < h.length; i++) acc = (acc << 8) | (h[i] & 0xFF);
            return acc;
        } catch (Exception e) {
            return s.hashCode();
        }
    }
}
