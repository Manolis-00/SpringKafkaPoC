package org.example.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
     * @return
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
        //TODO

        return savedOrder;
    }

    /**
     * Retrieves all orders from the db
     * @return
     */
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    /**
     * Retrieves orders by status
     *
     * @param status
     * @return
     */
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByOrderStatus(status);
    }

    /**
     * Retrieves orders for a specific customer.
     *
     * @param customerName
     * @return
     */
    public List<Order> getOrdersByCustomer(String customerName) {
        return orderRepository.findByCustomerName(customerName);
    }

    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        order.setOrderStatus(newStatus);
        Order updateOrder = orderRepository.save(order);

        // TODO Kafka
        // Publish Update Event

        return updateOrder;
    }
}
