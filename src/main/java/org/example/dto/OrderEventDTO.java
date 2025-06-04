package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.entity.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event object sent to Kafka.
 * Contains all order information plus metadata for event processing
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEventDTO {

    private Long orderId;
    private String customerName;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
    private String eventType; // CRUD
    private LocalDateTime timestamp;

    public static OrderEventDTO fromOrder(Order order, String eventType) {
        return OrderEventDTO.builder()
                .orderId(order.getId())
                .customerName(order.getCustomerName())
                .productName(order.getProductName())
                .quantity(order.getQuantity())
                .price(order.getPrice())
                .eventType(eventType)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
