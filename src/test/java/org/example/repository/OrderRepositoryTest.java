package org.example.repository;

import org.example.entity.Order;
import org.example.enums.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository layer tests.
 * DataJpaTest provides a lightweight test context with just JPA components
 */
@DataJpaTest
@ActiveProfiles("test")
public class OrderRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrderRepository orderRepository;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        // Clear all existing data
        orderRepository.deleteAll();

        // Create the test order
        testOrder = new Order();
        testOrder.setCustomerName("Test Customer");
        testOrder.setProductName("Test Product");
        testOrder.setQuantity(1);
        testOrder.setPrice(new BigDecimal("100.0"));
        testOrder.setOrderStatus(OrderStatus.PENDING);
    }

    @Test
    void testSaveAndFind() {
        // When
        Order saveOrder = orderRepository.save(testOrder);
        entityManager.flush();
        entityManager.clear();

        // Then
        Order foundOrder = orderRepository.findById(saveOrder.getId()).orElse(null);
        assertThat(foundOrder).isNotNull();
        assertThat(foundOrder.getCustomerName()).isEqualTo("Test Customer");
        assertThat(foundOrder.getCreatedAt()).isNotNull();
    }

    @Test
    void testFindByOrderStatus() {
        // Many orders with different statuses are provided
        Order pendingOrder = createOrder("Pending Customer", OrderStatus.PENDING);
        Order processingOrder = createOrder("Processing Customer", OrderStatus.PROCESSING);
        Order completedOrder = createOrder("Completed Customer", OrderStatus.COMPLETED);

        orderRepository.saveAll(List.of(pendingOrder, processingOrder, completedOrder));

        // When
        List<Order> pendingOrders = orderRepository.findByOrderStatus(OrderStatus.PENDING);

        // Then
        assertThat(pendingOrders).hasSize(1);
        assertThat(pendingOrders.get(0).getCustomerName()).isEqualTo("Pending Customer");
    }

    @Test
    void testFindOrdersByCustomerName() {
        // Given
        Order firstOrder = createOrder("John Doe", OrderStatus.PENDING);
        Order secondOrder = createOrder("Jane Doe", OrderStatus.COMPLETED);
        Order thirdOrder = createOrder("Jana Smith", OrderStatus.FAILED);

        orderRepository.saveAll(List.of(firstOrder, secondOrder, thirdOrder));

        // When
        List<Order> johnOrders = orderRepository.findByCustomerName("John Doe");

        // Then
        assertThat(johnOrders).hasSize(1);
        assertThat(johnOrders).extracting(Order::getCustomerName)
                .containsExactly("John Doe");
    }

    @Test
    void testFindExpensiveOrdersByStatus() {
        // Given
        Order cheapOrder = createOrder("Johnie Walker", OrderStatus.COMPLETED, new BigDecimal("50.0"));
        Order mediumOrder = createOrder("Cutty Sark", OrderStatus.COMPLETED, new BigDecimal("200.0"));
        Order expensiveOrder = createOrder("Robert McAllan", OrderStatus.COMPLETED, new BigDecimal("400.0"));

        orderRepository.saveAll(List.of(cheapOrder, mediumOrder, expensiveOrder));

        // When
        List<Order> expensiveCompletedOrders = orderRepository.findExpensiveOrdersByStatus(new BigDecimal("250.0"),
                OrderStatus.COMPLETED);

        // Then
        assertThat(expensiveCompletedOrders).hasSize(1);
        assertThat(expensiveCompletedOrders.get(0).getPrice()).isEqualTo(new BigDecimal("400.0"));
    }

    private Order createOrder(String customerName, OrderStatus orderStatus) {
        return createOrder(customerName, orderStatus, new BigDecimal("100.0"));
    }

    private Order createOrder(String customerName, OrderStatus orderStatus, BigDecimal price) {
        Order order = new Order();
        order.setCustomerName(customerName);
        order.setProductName("Product");
        order.setQuantity(1);
        order.setPrice(price);
        order.setOrderStatus(orderStatus);
        return order;
    }
}
