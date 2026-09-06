package com.onlineshoppingcart.service;

import com.onlineshoppingcart.entity.CartItem;
import com.onlineshoppingcart.entity.Product;
import com.onlineshoppingcart.repository.CartItemRepository;
import com.onlineshoppingcart.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(
            CartItemRepository cartItemRepository,
            ProductRepository productRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    public CartItem addToCart(
            String userEmail,
            Long productId,
            Integer quantity) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Product not found"));

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than 0");
        }

        if (quantity > product.getQuantity()) {
            throw new IllegalArgumentException(
                    "Not enough product stock");
        }

        CartItem cartItem = cartItemRepository
                .findByUserEmailAndProductId(userEmail, productId)
                .orElse(null);

        if (cartItem == null) {
            cartItem = new CartItem(
                    userEmail,
                    productId,
                    quantity
            );
        } else {
            int newQuantity = cartItem.getQuantity() + quantity;

            if (newQuantity > product.getQuantity()) {
                throw new IllegalArgumentException(
                        "Not enough product stock");
            }

            cartItem.setQuantity(newQuantity);
        }

        return cartItemRepository.save(cartItem);
    }

    public List<CartItem> getCart(String userEmail) {
        return cartItemRepository.findByUserEmail(userEmail);
    }

    public CartItem updateCartQuantity(
            String userEmail,
            Long cartItemId,
            Integer quantity) {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than 0");
        }

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Cart item not found"));

        if (!cartItem.getUserEmail().equals(userEmail)) {
            throw new IllegalArgumentException(
                    "You cannot update another user's cart item");
        }

        Product product = productRepository
                .findById(cartItem.getProductId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product not found"));

        if (quantity > product.getQuantity()) {
            throw new IllegalArgumentException(
                    "Not enough product stock");
        }

        cartItem.setQuantity(quantity);

        return cartItemRepository.save(cartItem);
    }

    public void removeFromCart(String userEmail, Long cartItemId) {

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Cart item not found"));

        if (!cartItem.getUserEmail().equals(userEmail)) {
            throw new IllegalArgumentException(
                    "You cannot remove another user's cart item");
        }

        cartItemRepository.delete(cartItem);
    }
}