package org.example.service;

import org.example.dto.OrderEventDTO;
import org.example.dto.OrderRequestDTO;
import org.example.entity.Order;
import org.example.enums.OrderStatus;
import org.example.kafka.producer.OrderProducer;
import org.example.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderProducer orderProducer;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, orderProducer);
    }

    @Test
    void testCreateOrder() {
        // Given
        OrderRequestDTO requestDTO = new OrderRequestDTO("John Doe", "Laptop", 2,
                new BigDecimal("999.99"));

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order orderToSave = invocation.getArgument(0);
            // Simulate ID generation
            orderToSave.setId(1L);

            if (orderToSave.getCreatedAt() == null) {
                orderToSave.setCreatedAt(LocalDateTime.now());
            }
            return orderToSave;
        });

        //TODO - Kafka Producer
        // Mock void method
        //doNothing().when(orderProducer).sendOrderEvent()

        // When
        Order result = orderService.createOrder(requestDTO);

        // Then - Verify saved order
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCustomerName()).isEqualTo("John Doe");
        assertThat(result.getProductName()).isEqualTo("Laptop");
        assertThat(result.getQuantity()).isEqualTo(2);
        assertThat(result.getPrice()).isEqualTo(new BigDecimal("999.99"));
        assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.getCreatedAt()).isNotNull();

        // Verify interactions
        verify(orderRepository).save(any(Order.class));

        // Verify event was published with correct data
        ArgumentCaptor<OrderEventDTO> eventDTOArgumentCaptor = ArgumentCaptor.forClass(OrderEventDTO.class);
        //TODO - Verify order producer for Kafka
    }

    @Test
    void testGetAllOrders() {
        // Given
        List<Order> orders = Arrays.asList(
                createTestOrder(1L, "FirstCustomer"),
                createTestOrder(2L, "SecondCustomer")
        );
        when(orderRepository.findAll()).thenReturn(orders);

        // When
        List<Order> result = orderService.getAllOrders();

        // Then
        assertThat(result).hasSize(2);
        verify(orderRepository).findAll();
    }

    @Test
    void testUpdateOrderStatus() {
        // Given
        Long orderId = 1L;
        Order existingOrder = createTestOrder(orderId, "John Doe");
        existingOrder.setOrderStatus(OrderStatus.PENDING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(existingOrder);


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
