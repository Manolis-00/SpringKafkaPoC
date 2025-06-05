package org.example.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.OrderEventDTO;
import org.example.dto.OrderRequestDTO;
import org.example.entity.Order;
import org.example.enums.OrderStatus;
import org.example.kafka.producer.OrderProducer;
import org.example.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service layer for order management
 * Handles business logic and coordinates between repository and kafka
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderProducer orderProducer;

    /**
     * Creates a new order and publishes an event to Kafka
     * {@code @Transactional} ensures that data operations are atomic
     *
     * @return - Returns the new {@link Order}
     */
    public Order createOrder(OrderRequestDTO requestDTO) {
        log.info("Create");

        // Create and save the order entity
        Order order = new Order();
        order.setCustomerName(requestDTO.getCustomerName());
        order.setProductName(requestDTO.getProductName());
        order.setQuantity(requestDTO.getQuantity());
        order.setPrice(requestDTO.getPrice());
        order.setOrderStatus(OrderStatus.PENDING);

        Order savedOrder = orderRepository.save(order);
        log.info("Order saved with ID: {}", savedOrder.getId());

        // Publish the order created event to kafka
        OrderEventDTO eventDTO = OrderEventDTO.fromOrder(savedOrder, "CREATE");
        orderProducer.sendOrderEvent(eventDTO);

        return savedOrder;
    }

    /**
     * Retrieves all orders from the db
     *
     * @return - A {@link List} of all the {@link Order}s
     */
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    /**
     * Retrieves orders by status
     *
     * @param status - The orderStatus of the {@link Order}
     * @return - A {@link List} of the {@link Order}s that match the {@link OrderStatus} provided.
     */
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByOrderStatus(status);
    }

    /**
     * Retrieves orders for a specific customer.
     *
     * @param customerName - The name of the customer
     * @return - Returns a {@link List} of the {@link Order}s made by the provided customer
     */
    public List<Order> getOrdersByCustomer(String customerName) {
        return orderRepository.findByCustomerName(customerName);
    }

    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        order.setOrderStatus(newStatus);
        Order updateOrder = orderRepository.save(order);

        // Publish Update Event
        OrderEventDTO eventDTO = OrderEventDTO.fromOrder(updateOrder, "UPDATE");
        orderProducer.sendOrderEvent(eventDTO);

        return updateOrder;
    }
}
