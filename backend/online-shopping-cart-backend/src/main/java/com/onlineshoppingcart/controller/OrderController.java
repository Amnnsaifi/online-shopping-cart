package com.onlineshoppingcart.controller;

import com.onlineshoppingcart.entity.Order;
import com.onlineshoppingcart.entity.OrderItem;
import com.onlineshoppingcart.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Order> placeOrder(
            Authentication authentication) {

        Order order = orderService.placeOrder(
                authentication.getName()
        );

        return ResponseEntity.ok(order);
    }

    @GetMapping
    public ResponseEntity<List<Order>> getMyOrders(
            Authentication authentication) {

        return ResponseEntity.ok(
                orderService.getMyOrders(
                        authentication.getName()
                )
        );
    }

    @GetMapping("/{orderId}/items")
    public ResponseEntity<List<OrderItem>> getOrderItems(
            Authentication authentication,
            @PathVariable Long orderId) {

        List<Order> orders =
                orderService.getMyOrders(authentication.getName());

        Order matchingOrder = orders.stream()
                .filter(order -> order.getId().equals(orderId))
                .findFirst()
                .orElse(null);

        if (matchingOrder == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                orderService.getOrderItems(orderId)
        );
    }
}