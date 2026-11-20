package com.retail.storeback.order.model.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
public record CartResponse(
        UUID cartId,
        UUID customerId,
        String sessionId,
        List<CartItemResponse> items,
        BigDecimal totalAmount,
        int totalQuantity,
        Instant createdAt,
        Instant updatedAt
) {
}
