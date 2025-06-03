package org.example.entity;

import org.example.enums.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the Order entity.
 * Tests entity behavior without Spring context
 */
public class OrderTest {

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
    }

    /**
     * Tests that the PrePersist method works as expected.
     */
    @Test
    void testPrePersist() {
        // Verify that the order does not have a createdAt
        assertThat(order.getCreatedAt()).isNull();

        // Call method under test
        order.onCreate();

        assertThat(order.getCreatedAt()).isNotNull();
        assertThat(order.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void testOrderCreation() {
        // Provided values
        Long id = 1L;
        String customerName = "John Doe";
        String productName = "Laptop";
        Integer quantity = 2;
        BigDecimal price = new BigDecimal("999.99");
        OrderStatus status = OrderStatus.PENDING;

        // WHEN
        order.setId(id);
        order.setCustomerName(customerName);
        order.setProductName(productName);
        order.setQuantity(quantity);
        order.setPrice(price);
        order.setOrderStatus(status);

        // THEN
        assertThat(order.getId()).isEqualTo(id);
        assertThat(order.getCustomerName()).isEqualTo(customerName);
        assertThat(order.getProductName()).isEqualTo(productName);
        assertThat(order.getQuantity()).isEqualTo(quantity);
        assertThat(order.getPrice()).isEqualTo(price);
        assertThat(order.getOrderStatus()).isEqualTo(status);
    }

    @Test
    void testOrderStatusTransitions() {
        // New order
        order.setOrderStatus(OrderStatus.PENDING);

        // Status Change
        order.setOrderStatus(OrderStatus.PROCESSING);

        // Then
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PROCESSING);

        // Can transition to completed
        order.setOrderStatus(OrderStatus.COMPLETED);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);
    }
}
