package com.yashrane.flowpay_backend.messaging;

import com.yashrane.flowpay_backend.config.RabbitMQConfig;
import com.yashrane.flowpay_backend.event.TransferCompletedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class TransferEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public TransferEventPublisher(RabbitTemplate rabbitTemplate){
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishTransferCompleted(TransferCompletedEvent event){
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                event
        );
    }
}