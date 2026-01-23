package com.example.monitoring.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.device.data}")
    private String deviceDataQueue;

    @Value("${rabbitmq.queue.sync}")
    private String syncQueue;

    @Value("${rabbitmq.exchange.device.data}")
    private String deviceDataExchange;

    @Value("${rabbitmq.exchange.sync}")
    private String syncExchange;

    @Value("${rabbitmq.routing.key.device.data}")
    private String deviceDataRoutingKey;

    @Value("${rabbitmq.routing.key.sync.user}")
    private String syncUserRoutingKey;

    @Value("${rabbitmq.routing.key.sync.device}")
    private String syncDeviceRoutingKey;

    // Device Data Queue Configuration
    @Bean
    public Queue deviceDataQueue() {
        return new Queue(deviceDataQueue, true);
    }

    @Bean
    public TopicExchange deviceDataExchange() {
        return new TopicExchange(deviceDataExchange);
    }

    @Bean
    public Binding deviceDataBinding() {
        return BindingBuilder
                .bind(deviceDataQueue())
                .to(deviceDataExchange())
                .with(deviceDataRoutingKey);
    }

    // Synchronization Queue Configuration
    @Bean
    public Queue syncQueue() {
        return new Queue(syncQueue, true);
    }

    @Bean
    public TopicExchange syncExchange() {
        return new TopicExchange(syncExchange);
    }

    @Bean
    public Binding syncUserBinding() {
        return BindingBuilder
                .bind(syncQueue())
                .to(syncExchange())
                .with(syncUserRoutingKey);
    }

    @Bean
    public Binding syncDeviceBinding() {
        return BindingBuilder
                .bind(syncQueue())
                .to(syncExchange())
                .with(syncDeviceRoutingKey);
    }

    // Message Converter
    @Bean
    public MessageConverter messageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(mapper);
    }

    // RabbitTemplate
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}

