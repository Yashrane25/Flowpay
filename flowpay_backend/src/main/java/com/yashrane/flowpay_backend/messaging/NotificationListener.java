package com.yashrane.flowpay_backend.messaging;

import com.yashrane.flowpay_backend.config.RabbitMQConfig;
import com.yashrane.flowpay_backend.event.TransferCompletedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener{

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleTransferCompleted(TransferCompletedEvent event){
        System.out.println(
                "[NOTIFICATION] Transfer completed: wallet " + event.getFromWalletId() +
                        " -> wallet " + event.getToWalletId() +
                        ", amount ₹" + event.getAmount()
        );
    }
}