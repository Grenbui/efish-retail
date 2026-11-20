package com.retail.storeback.order.model.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record OrderItemResponse(
        UUID orderItemId,
        UUID productId,
        String productNameSnap,
        int qty,
        BigDecimal unitPrice,
        BigDecimal subTotal,
        BigDecimal unitCostSnap
) {
}
