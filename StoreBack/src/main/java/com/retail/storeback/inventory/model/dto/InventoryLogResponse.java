package com.retail.storeback.inventory.model.dto;

import com.retail.storeback.inventory.model.InventoryChangeType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
public record InventoryLogResponse(
        UUID id,
        UUID productId,
        InventoryChangeType changeType,
        int qtyChange,
        BigDecimal unitCost,
        String sourceName,
        List<String> evidenceUrls,
        UUID referenceId,
        String note,
        Instant createdAt
) {
}
