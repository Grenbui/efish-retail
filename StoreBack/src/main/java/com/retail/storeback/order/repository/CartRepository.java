package com.retail.storeback.order.repository;

import com.retail.storeback.order.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {
    Optional<Cart> findByCustomerId(UUID customerId);
    Optional<Cart> findBySessionId(String sessionId);
}
