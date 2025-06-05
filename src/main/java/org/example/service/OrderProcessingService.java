package org.example.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.OrderEventDTO;
import org.example.entity.Order;
import org.example.enums.OrderStatus;
import org.example.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service for processing
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderProcessingService {
    
    private final OrderRepository orderRepository;

    /**
     * Processes order events based on event type.
     * 
     * @param eventDTO - The {@link OrderEventDTO}
     */
    public void processOrderEvent(OrderEventDTO eventDTO) {
       log.info("Processing order event: {}", eventDTO);
       
       switch (eventDTO.getEventType()) {
           case "CREATE":
               processNewOrder(eventDTO);
               break;
           case "UPDATE":
               processOrderUpdate(eventDTO);
               break;
           default:
               log.warn("Unknown event type: {}", eventDTO.getEventType());
       }
    }
    
    private void processNewOrder(OrderEventDTO eventDTO) {
        // Order processing logic
        Order order = orderRepository.findById(eventDTO.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + eventDTO.getOrderId()));
        
        // Update status to PROCESSING
        order.setOrderStatus(OrderStatus.PROCESSING);
        orderRepository.save(order);
        
        // Simulate processing time - Probably remove
        try {
            Thread.sleep(2000); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Mark as completed
        order.setOrderStatus(OrderStatus.COMPLETED);
        order.setProcessedAt(LocalDateTime.now());
        orderRepository.save(order);
        
        log.info("Order {} has been processed successfully", order.getId());
    }

    /**
     * Basic update
     * @param eventDTO - The {@link OrderEventDTO}
     */
    private void processOrderUpdate(OrderEventDTO eventDTO) {
        log.info("Processing order update for order: {}", eventDTO.getOrderId());
        Order order = orderRepository.findById(eventDTO.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + eventDTO.getOrderId()));

        // Update status to PROCESSING
        order.setOrderStatus(OrderStatus.PROCESSING);
        orderRepository.save(order);

        // Simulate processing time - Probably remove
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Mark as completed
        order.setOrderStatus(OrderStatus.COMPLETED);
        order.setProcessedAt(LocalDateTime.now());
        orderRepository.save(order);

        log.info("Order {} has been updated successfully", order.getId());
    }
}
