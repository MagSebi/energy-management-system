package com.example.auth.config;

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

    @Value("${rabbitmq.queue.auth.sync.user}")
    private String authSyncUserQueue;

    @Value("${rabbitmq.routing.key.sync.user}")
    private String syncUserRoutingKey;

    @Bean
    public TopicExchange syncExchange() {
        return new TopicExchange(syncExchange, true, false);
    }

    @Bean
    public Queue authSyncUserQueue() {
        return new Queue(authSyncUserQueue, true);
    }

    @Bean
    public Binding authSyncUserBinding() {
        return BindingBuilder
                .bind(authSyncUserQueue())
                .to(syncExchange())
                .with(syncUserRoutingKey);
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
