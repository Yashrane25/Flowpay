package com.yashrane.flowpay_backend.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

@Configuration
public class RabbitMQConfig{
    public static final String EXCHANGE_NAME = "flowpay.exchange";
    public static final String QUEUE_NAME = "flowpay.transfer.completed.queue";
    public static final String ROUTING_KEY = "transfer.completed";

    @Bean
    public TopicExchange exchange(){
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue transferCompletedQueue(){
        // durable = true: the queue survives a RabbitMQ restart, so
        // undelivered messages aren't silently lost if the broker
        // itself goes down and comes back up.
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public Binding binding(Queue transferCompletedQueue, TopicExchange exchange){
        return BindingBuilder.bind(transferCompletedQueue)
                .to(exchange)
                .with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }
}