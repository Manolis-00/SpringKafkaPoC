package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.OrderRequestDTO;
import org.example.entity.Order;
import org.example.enums.OrderStatus;
import org.example.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Creates a new order.
     *
     * @param requestDTO - Requires a {@link OrderRequestDTO} object
     * @return - The body entity of the response
     */
    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody OrderRequestDTO requestDTO) {
        Order createdOrder = orderService.createOrder(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    /**
     * Retrieves all orders
     *
     * @return - The response entity
     */
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * Retrieves orders by status
     *
     * @param status - The status of the order as {@link String}
     * @return - A list of the orders whose orderStatus matches the input
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable String status) {
        OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
        List<Order> orders = orderService.getOrdersByStatus(orderStatus);
        return ResponseEntity.ok(orders);
    }

    /**
     * Retrieves orders by customer name.
     *
     * @param customerName -The customerName
     * @return - A list of the orders made to the provided customer
     */
    @GetMapping("/customer/{customerName}")
    public ResponseEntity<List<Order>> getOrdersByCustomer(@PathVariable String customerName) {
        List<Order> orders = orderService.getOrdersByCustomer(customerName);
        return ResponseEntity.ok(orders);
    }

    /**
     * Updates order status
     *
     * @param orderId - the id of the {@link Order}
     * @param status  - the status of the {@link Order}
     * @return - Provides a patch, which updates the order that matches the provided orderId with the new status provided.
     */
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {
     OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());
     Order updateOrder = orderService.updateOrderStatus(orderId, newStatus);
     return ResponseEntity.ok(updateOrder);
    }
}
