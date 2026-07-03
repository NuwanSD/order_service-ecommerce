package com.ecommerce.orderservice.messaging;

import com.ecommerce.orderservice.dto.OrderEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OrderEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(OrderEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public OrderEventPublisher(RabbitTemplate rabbitTemplate,
                                @Value("${rabbitmq.exchange}") String exchange,
                                @Value("${rabbitmq.routing-key}") String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void publishOrderEvent(OrderEventDTO orderEvent) {
        logger.info("Publishing order event for orderId: {}", orderEvent.getOrderId());
        rabbitTemplate.convertAndSend(exchange, routingKey, orderEvent);
        logger.info("Order event published successfully for orderId: {}", orderEvent.getOrderId());
    }
}
