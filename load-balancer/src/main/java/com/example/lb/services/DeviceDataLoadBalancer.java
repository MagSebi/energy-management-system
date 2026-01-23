package com.example.lb.services;

import com.example.lb.strategy.ConsistentHashingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DeviceDataLoadBalancer {
    private static final Logger logger = LoggerFactory.getLogger(DeviceDataLoadBalancer.class);

    private final RabbitTemplate rabbitTemplate;
    private final ConsistentHashingStrategy strategy;

    @Value("${rabbitmq.exchange.device}")
    private String deviceExchange;

    public DeviceDataLoadBalancer(RabbitTemplate rabbitTemplate,
                                  @Value("${rabbitmq.routing.keys.ingest}") List<String> ingestKeys) {
        this.rabbitTemplate = rabbitTemplate;
        this.strategy = new ConsistentHashingStrategy(ingestKeys);
    }

    @RabbitListener(queues = "${rabbitmq.queue.central}")
    public void consumeCentral(Map<String, Object> payload) {
        try {
            String deviceId = String.valueOf(payload.get("deviceId"));
            String key = strategy.selectKey(deviceId);
            rabbitTemplate.convertAndSend(deviceExchange, key, payload);
            logger.info("Forwarded device {} to {}", deviceId, key);
        } catch (Exception e) {
            logger.error("Load balancer failed for payload {}", payload, e);
            throw e;
        }
    }
}
