package com.ecommerce.orderservice.controller;

import com.ecommerce.orderservice.dto.OrderRequestDTO;
import com.ecommerce.orderservice.dto.OrderResponseDTO;
import com.ecommerce.orderservice.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@AutoConfigureJson
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private OrderResponseDTO mockResponse;

    @BeforeEach
    void setUp() {
        mockResponse = new OrderResponseDTO();
        mockResponse.setOrderId(1L);
        mockResponse.setCustomerId(1L);
        mockResponse.setProductId(10L);
        mockResponse.setProductName("Test Product");
        mockResponse.setQuantity(2);
        mockResponse.setTotalPrice(new BigDecimal("100.00"));
        mockResponse.setOrderDate(LocalDateTime.now());
        mockResponse.setStatus("PENDING");
    }

    @Test
    void createOrder_ShouldReturn201() throws Exception {
        OrderRequestDTO requestDTO = new OrderRequestDTO();
        requestDTO.setCustomerId(1L);
        requestDTO.setProductId(10L);
        requestDTO.setQuantity(2);

        when(orderService.createOrder(any(OrderRequestDTO.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(1L))
                .andExpect(jsonPath("$.productName").value("Test Product"))
                .andExpect(jsonPath("$.totalPrice").value(100.00));
    }

    @Test
    void createOrder_WithMissingFields_ShouldReturn400() throws Exception {
        OrderRequestDTO invalidRequest = new OrderRequestDTO();

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
