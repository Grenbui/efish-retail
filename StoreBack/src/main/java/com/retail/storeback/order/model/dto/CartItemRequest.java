package com.retail.storeback.order.model.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CartItemRequest(
        UUID productId,
        int quantity
) {
}
