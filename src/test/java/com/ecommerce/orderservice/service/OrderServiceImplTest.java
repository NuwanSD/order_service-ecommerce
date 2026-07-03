package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.client.ProductServiceClient;
import com.ecommerce.orderservice.dto.*;
import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.messaging.OrderEventPublisher;
import com.ecommerce.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductServiceClient productServiceClient;

    @Mock
    private OrderEventPublisher orderEventPublisher;

    @InjectMocks
    private OrderServiceImpl orderService;

    private OrderRequestDTO requestDTO;
    private ProductResponseDTO mockProduct;
    private Order mockSavedOrder;

    @BeforeEach
    void setUp() {
        requestDTO = new OrderRequestDTO();
        requestDTO.setCustomerId(1L);
        requestDTO.setProductId(10L);
        requestDTO.setQuantity(2);

        mockProduct = new ProductResponseDTO();
        mockProduct.setProductId(10L);
        mockProduct.setName("Test Product");
        mockProduct.setUnitPrice(new BigDecimal("50.00"));

        mockSavedOrder = new Order();
        mockSavedOrder.setOrderId(1L);
        mockSavedOrder.setCustomerId(1L);
        mockSavedOrder.setProductId(10L);
        mockSavedOrder.setProductName("Test Product");
        mockSavedOrder.setQuantity(2);
        mockSavedOrder.setTotalPrice(new BigDecimal("100.00"));
        mockSavedOrder.setOrderDate(LocalDateTime.now());
        mockSavedOrder.setStatus("PENDING");
    }

    @Test
    void createOrder_ShouldFetchProductCalculatePriceAndPublishEvent() {
        when(productServiceClient.getProductById(10L)).thenReturn(mockProduct);
        when(orderRepository.save(any(Order.class))).thenReturn(mockSavedOrder);
        doNothing().when(orderEventPublisher).publishOrderEvent(any(OrderEventDTO.class));

        OrderResponseDTO result = orderService.createOrder(requestDTO);

        assertNotNull(result);
        assertEquals(1L, result.getOrderId());
        assertEquals("Test Product", result.getProductName());
        assertEquals(new BigDecimal("100.00"), result.getTotalPrice());

        verify(productServiceClient, times(1)).getProductById(10L);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderEventPublisher, times(1)).publishOrderEvent(any(OrderEventDTO.class));
    }

    @Test
    void createOrder_WhenProductServiceFails_ShouldThrowException() {
        when(productServiceClient.getProductById(99L)).thenThrow(
                new com.ecommerce.orderservice.exception.ProductServiceException("Product not found"));

        requestDTO.setProductId(99L);

        assertThrows(com.ecommerce.orderservice.exception.ProductServiceException.class,
                () -> orderService.createOrder(requestDTO));

        verify(orderRepository, never()).save(any());
        verify(orderEventPublisher, never()).publishOrderEvent(any());
    }
}
