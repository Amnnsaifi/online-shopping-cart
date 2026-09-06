package com.onlineshoppingcart.controller;

import com.onlineshoppingcart.entity.CartItem;
import com.onlineshoppingcart.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    public ResponseEntity<CartItem> addToCart(
            Authentication authentication,
            @RequestBody Map<String, Object> request) {

        String userEmail = authentication.getName();

        Long productId = Long.valueOf(
                request.get("productId").toString()
        );

        Integer quantity = Integer.valueOf(
                request.get("quantity").toString()
        );

        CartItem cartItem = cartService.addToCart(
                userEmail,
                productId,
                quantity
        );

        return new ResponseEntity<>(
                cartItem,
                HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<List<CartItem>> getCart(
            Authentication authentication) {

        return ResponseEntity.ok(
                cartService.getCart(authentication.getName())
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<CartItem> updateCartQuantity(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        Integer quantity = Integer.valueOf(
                request.get("quantity").toString()
        );

        CartItem cartItem = cartService.updateCartQuantity(
                authentication.getName(),
                id,
                quantity
        );

        return ResponseEntity.ok(cartItem);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeFromCart(
            Authentication authentication,
            @PathVariable Long id) {

        cartService.removeFromCart(
                authentication.getName(),
                id
        );

        return ResponseEntity.noContent().build();
    }
}