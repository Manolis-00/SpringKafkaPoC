package org.example.service;

import org.example.dto.OrderEventDTO;
import org.example.entity.Order;
import org.example.enums.OrderStatus;
import org.example.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test for OrderProcessingService
 * This service handles the business logic for processing order events
 * received from Kafka. Testing this ensures the event processing logic
 * works correctly in isolation from Kafka infrastructure.
 */
@ExtendWith(MockitoExtension.class)
public class OrderProcessingServiceTest {

    @Mock
    private OrderRepository orderRepository;

    private OrderProcessingService orderProcessingService;

    @BeforeEach
    void setup() {
        orderProcessingService = new OrderProcessingService(orderRepository);
    }

    @Test
    void testProcessNewOrderEvent() {
        // Given
        OrderEventDTO createEvent = OrderEventDTO.builder()
                .orderId(123L)
                .customerName("Test Customer")
                .productName("Test Product")
                .quantity(2)
                .price(new BigDecimal("99.99"))
                .eventType("CREATE")
                .timestamp(LocalDateTime.now())
                .build();

        // Create mock order
        Order existingOrder = new Order();
        existingOrder.setId(123L);
        existingOrder.setCustomerName("Test Customer");
        existingOrder.setProductName("Test Product");
        existingOrder.setQuantity(2);
        existingOrder.setPrice(new BigDecimal("99.99"));
        existingOrder.setOrderStatus(OrderStatus.PENDING);
        existingOrder.setCreatedAt(LocalDateTime.now());

        // Mock repository
        when(orderRepository.findById(123L)).thenReturn(Optional.of(existingOrder));

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order orderToSave = invocation.getArgument(0);
            return orderToSave.copy();
        });

        // When - Process the event
        orderProcessingService.processOrderEvent(createEvent);

        List<Order> savedStates = new ArrayList<>();
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            savedStates.add(order.copy());
            return order;
        });

        // When
        orderProcessingService.processOrderEvent(createEvent);

        // Then
        assertThat(savedStates).hasSize(2);
        assertThat(savedStates.get(0).getOrderStatus()).isEqualTo(OrderStatus.PROCESSING);
        assertThat(savedStates.get(1).getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    void testProcessOrderEventWhenOrderNotFound() {
        OrderEventDTO eventDTO = OrderEventDTO.builder()
                .orderId(999L)
                .eventType("CREATE")
                .build();

        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> orderProcessingService.processOrderEvent(eventDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Order not found: 999");

        // Verify save was never called since order was not found
        verify(orderRepository, never()).save(any());
    }

    @Test
    void testProcessUnknownEventType() {
        // Given
        OrderEventDTO unknownEvent = OrderEventDTO.builder()
                .orderId(123L)
                .eventType("UNKNOWN_type")
                .build();

        // When
        orderProcessingService.processOrderEvent(unknownEvent);

        // Then
        verify(orderRepository, never()).findById(any());
        verify(orderRepository, never()).save(any());
    }
}
