package com.example.demo.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.sync}")
    private String syncExchange;

    @Value("${rabbitmq.routing.key.sync.user}")
    private String syncUserRoutingKey;

    @Value("${rabbitmq.queue.user.sync.auth}")
    private String userSyncAuthQueue;

    @Value("${rabbitmq.routing.key.sync.device}")
    private String syncDeviceRoutingKey;

    @Value("${rabbitmq.queue.user.sync.device}")
    private String userSyncDeviceQueue;

    @Bean
    public TopicExchange syncExchange() {
        return new TopicExchange(syncExchange, true, false);
    }

    @Bean
    public Queue userSyncAuthQueue() {
        return new Queue(userSyncAuthQueue, true);
    }

    @Bean
    public Binding userSyncAuthBinding() {
        return BindingBuilder
                .bind(userSyncAuthQueue())
                .to(syncExchange())
                .with(syncUserRoutingKey);
    }

    @Bean
    public Queue userSyncDeviceQueue() {
        return new Queue(userSyncDeviceQueue, true);
    }

    @Bean
    public Binding userSyncDeviceBinding() {
        return BindingBuilder
                .bind(userSyncDeviceQueue())
                .to(syncExchange())
                .with(syncDeviceRoutingKey);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}

