package org.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity representing an Order in the db.
 * {@code @Entity} marks this as a JPA entity.
 * {@code @Table} specifies the db table name
 */
@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    /**
     * Lifecycle callback - Sets a creation timestamp before persisting
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Order copy() {
        Order copiedOrder = new Order();
        copiedOrder.setId(this.getId());
        copiedOrder.setCustomerName(this.getCustomerName());
        copiedOrder.setProductName(this.getProductName());
        copiedOrder.setQuantity(this.getQuantity());
        copiedOrder.setPrice(this.getPrice());
        copiedOrder.setOrderStatus(this.getOrderStatus());
        copiedOrder.setCreatedAt(this.getCreatedAt());
        copiedOrder.setProcessedAt(this.getProcessedAt());
        return copiedOrder;
    }
}
