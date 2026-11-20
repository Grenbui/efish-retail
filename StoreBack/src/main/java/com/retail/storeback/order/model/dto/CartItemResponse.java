package com.retail.storeback.order.model.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
public record CartItemResponse(
        UUID cartItemId,
        UUID productId,
        String productName,
        BigDecimal productPrice,
        int quantity,
        BigDecimal subTotal,
        Instant createdAt,
        Instant updatedAt
) {
}
