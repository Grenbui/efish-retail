package com.retail.storeback.order.service;

import com.retail.storeback.category.model.Product;
import com.retail.storeback.category.repository.ProductRepository;
import com.retail.storeback.order.model.Cart;
import com.retail.storeback.order.model.CartItem;
import com.retail.storeback.order.model.dto.CartItemRequest;
import com.retail.storeback.order.model.dto.CartResponse;
import com.retail.storeback.order.model.mapper.CartMapper;
import com.retail.storeback.order.repository.CartRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;

    @Transactional(readOnly = true)
    public CartResponse getCartById(UUID cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found with id: " + cartId));
        return cartMapper.toCartResponse(cart);
    }

    @Transactional
    public CartResponse getOrCreateCart(UUID customerId, String sessionId) {
        if (customerId != null) {
            Cart cart = cartRepository.findByCustomerId(customerId)
                    .orElseGet(() -> cartRepository.save(Cart.builder()
                            .customerId(customerId)
                            .sessionId(sessionId)
                            .cartItems(new ArrayList<>())
                            .build()));
            return cartMapper.toCartResponse(cart);
        } else if (sessionId != null && !sessionId.isBlank()) {
            Cart cart = cartRepository.findBySessionId(sessionId)
                    .orElseGet(() -> cartRepository.save(Cart.builder()
                            .sessionId(sessionId)
                            .cartItems(new ArrayList<>())
                            .build()));
            return cartMapper.toCartResponse(cart);
        } else {
            Cart cart = cartRepository.save(Cart.builder()
                    .cartItems(new ArrayList<>())
                    .build());
            return cartMapper.toCartResponse(cart);
        }
    }

    @Transactional
    public CartResponse addItemToCart(UUID cartId, CartItemRequest request) {
        if (request.quantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found with id: " + cartId));

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + request.productId()));

        if (!product.isActive()) {
            throw new IllegalArgumentException("Product is currently not active for sale: " + product.getName());
        }

        CartItem existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProductId().equals(request.productId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + request.quantity());
            existingItem.setProductPrice(product.getSellingPrice());
            existingItem.setProductName(product.getName());
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .productId(product.getId())
                    .productName(product.getName())
                    .productPrice(product.getSellingPrice())
                    .quantity(request.quantity())
                    .build();
            cart.getCartItems().add(newItem);
        }

        Cart savedCart = cartRepository.save(cart);
        return cartMapper.toCartResponse(savedCart);
    }

    @Transactional
    public CartResponse updateItemQuantity(UUID cartId, UUID cartItemId, int newQuantity) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found with id: " + cartId));

        CartItem item = cart.getCartItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Item not found in cart: " + cartItemId));

        if (newQuantity <= 0) {
            cart.getCartItems().remove(item);
        } else {
            item.setQuantity(newQuantity);
        }

        Cart savedCart = cartRepository.save(cart);
        return cartMapper.toCartResponse(savedCart);
    }

    @Transactional
    public CartResponse removeItemFromCart(UUID cartId, UUID cartItemId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found with id: " + cartId));

        boolean removed = cart.getCartItems().removeIf(item -> item.getId().equals(cartItemId));
        if (!removed) {
            throw new EntityNotFoundException("Item not found in cart with id: " + cartItemId);
        }

        Cart savedCart = cartRepository.save(cart);
        return cartMapper.toCartResponse(savedCart);
    }

    @Transactional
    public CartResponse clearCart(UUID cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found with id: " + cartId));

        cart.getCartItems().clear();
        Cart savedCart = cartRepository.save(cart);
        return cartMapper.toCartResponse(savedCart);
    }
}
