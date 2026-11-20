package com.retail.storeback.inventory.model.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
public record InventoryStockResponse(
        UUID productId,
        int availableQty,
        BigDecimal avgCost,
        Long version,
        Instant updatedAt
) {
}
