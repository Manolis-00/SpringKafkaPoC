package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.OrderRequestDTO;
import org.example.entity.Order;
import org.example.enums.OrderStatus;
import org.example.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

// Mockito static imports
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

// Spring MockMvc static imports
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Hamcrest matchers for advanced assertions
import static org.hamcrest.Matchers.*;

/**
 * Controller layer tests.
 * @WebMvcTest focuses only on the web layer, making tests fast.
 */
@WebMvcTest(OrderController.class)
@ActiveProfiles("test")
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    void testCreateOrder() throws Exception {
        // Given
        OrderRequestDTO requestDTO = new OrderRequestDTO("John Doe", "Laptop", 2,
                new BigDecimal("999.99"));

        Order createOrder = new Order();
        createOrder.setId(1L);
        createOrder.setCustomerName(requestDTO.getCustomerName());
        createOrder.setProductName(requestDTO.getProductName());
        createOrder.setQuantity(requestDTO.getQuantity());
        createOrder.setPrice(requestDTO.getPrice());
        createOrder.setOrderStatus(OrderStatus.PENDING);

        when(orderService.createOrder(any(OrderRequestDTO.class))).thenReturn(createOrder);

        // When / Then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerName").value("John Doe"))
                .andExpect(jsonPath("$.orderStatus").value("PENDING"));
    }

    @Test
    void testCreateOrderValidation() throws Exception {
        // Given - invalid req with missing fields
        OrderRequestDTO invalidRequest = new OrderRequestDTO();

        // When/Then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAllOrders() throws Exception {
        // Given
        List<Order> orderList = Arrays.asList(
                createTestOrder(1L, "Customer1"),
                createTestOrder(2L, "Customer2")
        );

        when(orderService.getAllOrders()).thenReturn(orderList);

        // When
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].customerName").value("Customer1"))
                .andExpect(jsonPath("$[1].customerName").value("Customer2"));
    }

    @Test
    void testGetOrdersByStatus() throws Exception {
        // Given
        List<Order> pendingOrders = Arrays.asList(createTestOrder(1L, "Customer1"));

        when(orderService.getOrdersByStatus(OrderStatus.PENDING)).thenReturn(pendingOrders);

        // When
        mockMvc.perform(get("/api/orders/status/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].orderStatus").value("PENDING"));
    }

    @Test
    void testUpdateOrderStatus() throws Exception {
        // Given
        Long orderId = 1L;
        Order updatedOrder = createTestOrder(orderId, "John Doe");
        updatedOrder.setOrderStatus(OrderStatus.COMPLETED);

        when(orderService.updateOrderStatus(orderId, OrderStatus.COMPLETED)).thenReturn(updatedOrder);

        // When/Then
        mockMvc.perform(patch("/api/orders/{orderId}/status", orderId)
                .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus").value("COMPLETED"));
    }

    private Order createTestOrder(Long id, String customerName) {
        Order order = new Order();
        order.setId(id);
        order.setCustomerName(customerName);
        order.setProductName("Test Product");
        order.setQuantity(1);
        order.setPrice(new BigDecimal("100.0"));
        order.setOrderStatus(OrderStatus.PENDING);
        return order;
    }
}
