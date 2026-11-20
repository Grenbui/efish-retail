package com.retail.storeback.order.controller;

import com.retail.storeback.order.model.dto.CartItemRequest;
import com.retail.storeback.order.model.dto.CartResponse;
import com.retail.storeback.order.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/{cartId}")
    public ResponseEntity<CartResponse> getCartById(@PathVariable UUID cartId) {
        return ResponseEntity.ok(cartService.getCartById(cartId));
    }

    @GetMapping
    public ResponseEntity<CartResponse> getOrCreateCart(
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) String sessionId) {
        return ResponseEntity.ok(cartService.getOrCreateCart(customerId, sessionId));
    }

    @PostMapping("/{cartId}/items")
    public ResponseEntity<CartResponse> addItemToCart(
            @PathVariable UUID cartId,
            @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.addItemToCart(cartId, request));
    }

    @PatchMapping("/{cartId}/items/{cartItemId}")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @PathVariable UUID cartId,
            @PathVariable UUID cartItemId,
            @RequestParam int quantity) {
        return ResponseEntity.ok(cartService.updateItemQuantity(cartId, cartItemId, quantity));
    }

    @DeleteMapping("/{cartId}/items/{cartItemId}")
    public ResponseEntity<CartResponse> removeItemFromCart(
            @PathVariable UUID cartId,
            @PathVariable UUID cartItemId) {
        return ResponseEntity.ok(cartService.removeItemFromCart(cartId, cartItemId));
    }

    @DeleteMapping("/{cartId}")
    public ResponseEntity<CartResponse> clearCart(@PathVariable UUID cartId) {
        return ResponseEntity.ok(cartService.clearCart(cartId));
    }
}
