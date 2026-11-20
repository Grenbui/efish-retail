package com.retail.storeback.order.model.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CheckoutRequest(
        UUID cartId,
        UUID customerId,
        String customerName,
        String customerPhone,
        String shippingAddress,
        String note
) {
}
