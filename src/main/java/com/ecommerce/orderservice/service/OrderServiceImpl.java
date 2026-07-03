package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.client.ProductServiceClient;
import com.ecommerce.orderservice.dto.*;
import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.messaging.OrderEventPublisher;
import com.ecommerce.orderservice.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;
    private final OrderEventPublisher orderEventPublisher;

    public OrderServiceImpl(OrderRepository orderRepository,
                             ProductServiceClient productServiceClient,
                             OrderEventPublisher orderEventPublisher) {
        this.orderRepository = orderRepository;
        this.productServiceClient = productServiceClient;
        this.orderEventPublisher = orderEventPublisher;
    }

    @Override
    public OrderResponseDTO createOrder(OrderRequestDTO requestDTO) {
        // Step 1: Fetch product from Product Service
        logger.info("Fetching product details for productId: {}", requestDTO.getProductId());
        ProductResponseDTO product = productServiceClient.getProductById(requestDTO.getProductId());

        // Step 2: Calculate total price
        BigDecimal totalPrice = product.getUnitPrice()
                .multiply(BigDecimal.valueOf(requestDTO.getQuantity()));

        // Step 3: Save order to DB
        Order order = new Order();
        order.setCustomerId(requestDTO.getCustomerId());
        order.setProductId(requestDTO.getProductId());
        order.setProductName(product.getName());
        order.setQuantity(requestDTO.getQuantity());
        order.setTotalPrice(totalPrice);

        Order savedOrder = orderRepository.save(order);
        logger.info("Order saved with ID: {}", savedOrder.getOrderId());

        // Step 4: Publish event to RabbitMQ
        OrderEventDTO event = new OrderEventDTO(
                savedOrder.getOrderId(),
                savedOrder.getCustomerId(),
                savedOrder.getProductId(),
                savedOrder.getProductName(),
                savedOrder.getQuantity(),
                savedOrder.getTotalPrice(),
                savedOrder.getOrderDate()
        );
        orderEventPublisher.publishOrderEvent(event);

        return mapToResponseDTO(savedOrder);
    }

    private OrderResponseDTO mapToResponseDTO(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setOrderId(order.getOrderId());
        dto.setCustomerId(order.getCustomerId());
        dto.setProductId(order.getProductId());
        dto.setProductName(order.getProductName());
        dto.setQuantity(order.getQuantity());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus());
        return dto;
    }
}
