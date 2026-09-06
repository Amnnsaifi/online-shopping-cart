package com.onlineshoppingcart.service;

import com.onlineshoppingcart.entity.CartItem;
import com.onlineshoppingcart.entity.Order;
import com.onlineshoppingcart.entity.OrderItem;
import com.onlineshoppingcart.entity.Product;
import com.onlineshoppingcart.repository.CartItemRepository;
import com.onlineshoppingcart.repository.OrderItemRepository;
import com.onlineshoppingcart.repository.OrderRepository;
import com.onlineshoppingcart.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository) {

        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    public Order placeOrder(String userEmail) {

        List<CartItem> cartItems =
                cartItemRepository.findByUserEmail(userEmail);

        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        double totalAmount = 0.0;

        for (CartItem cartItem : cartItems) {

            Product product = productRepository
                    .findById(cartItem.getProductId())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Product not found"));

            if (cartItem.getQuantity() > product.getQuantity()) {
                throw new IllegalArgumentException(
                        "Not enough stock for product: "
                                + product.getName());
            }

            totalAmount +=
                    product.getPrice() * cartItem.getQuantity();
        }

        // Create the order first
        Order order = new Order(
                userEmail,
                totalAmount,
                "PLACED"
        );

        Order savedOrder = orderRepository.save(order);

        // Create order items and reduce stock
        for (CartItem cartItem : cartItems) {

            Product product = productRepository
                    .findById(cartItem.getProductId())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Product not found"));

            OrderItem orderItem = new OrderItem(
                    savedOrder.getId(),
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    cartItem.getQuantity()
            );

            orderItemRepository.save(orderItem);

            product.setQuantity(
                    product.getQuantity()
                            - cartItem.getQuantity()
            );

            productRepository.save(product);
        }

        // Clear cart after successful order
        cartItemRepository.deleteAll(cartItems);

        return savedOrder;
    }

    public List<Order> getMyOrders(String userEmail) {
        return orderRepository.findByUserEmail(userEmail);
    }

    public List<OrderItem> getOrderItems(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }
}