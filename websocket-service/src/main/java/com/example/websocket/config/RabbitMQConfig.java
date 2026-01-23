package com.example.websocket.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

    @Value("${rabbitmq.exchange.energy}")
    private String energyExchange;

    @Value("${rabbitmq.queue.energy}")
    private String energyQueue;

    @Value("${rabbitmq.routing.key.energy}")
    private String energyRoutingKey;

    @Value("${rabbitmq.queue.alert}")
    private String alertQueue;

    @Value("${rabbitmq.routing.key.alert}")
    private String alertRoutingKey;

    // Chat messaging
    @Value("${rabbitmq.exchange.chat}")
    private String chatExchange;

    @Value("${rabbitmq.queue.chat}")
    private String chatQueue;

    @Value("${rabbitmq.routing.key.chat}")
    private String chatRoutingKey;

    @Bean
    public Queue energyQueue() {
        return new Queue(energyQueue, true);
    }

    @Bean
    public TopicExchange energyExchange() {
        return new TopicExchange(energyExchange, true, false);
    }

    @Bean
    public Binding energyBinding() {
        return BindingBuilder
                .bind(energyQueue())
                .to(energyExchange())
                .with(energyRoutingKey);
    }

    @Bean
    public Queue alertQueue() { return new Queue(alertQueue, true); }

    @Bean
    public Binding alertBinding() {
        return BindingBuilder
                .bind(alertQueue())
                .to(energyExchange())
                .with(alertRoutingKey);
    }

    // Chat queue/exchange/binding
    @Bean
    public Queue chatQueue() { return new Queue(chatQueue, true); }

    @Bean
    public TopicExchange chatExchange() { return new TopicExchange(chatExchange, true, false); }

    @Bean
    public Binding chatBinding() {
        return BindingBuilder
                .bind(chatQueue())
                .to(chatExchange())
                .with(chatRoutingKey);
    }

    @Bean
    public MessageConverter messageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}
