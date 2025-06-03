package org.example.repository;

import org.example.entity.Order;
import org.example.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Spring Data JPA repository for Order entity.
 * Extends JpaRepository which provides CRUD operations and more.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByOrderStatus(OrderStatus orderStatus);

    @Query("SELECT o FROM Order o WHERE o.customerName = ?1 ORDER BY createdAt DESC")
    List<Order> findByCustomerName(String customerName);

    @Query("SELECT o FROM Order o WHERE o.price > :minPrice AND o.orderStatus = :orderStatus")
    List<Order> findExpensiveOrdersByStatus(BigDecimal minPrice, OrderStatus orderStatus);
}
