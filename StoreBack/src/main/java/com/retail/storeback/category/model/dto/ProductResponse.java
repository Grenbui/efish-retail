package com.retail.storeback.category.model.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
public record ProductResponse(
        UUID productId,
        UUID categoryId,
        String categoryName,
        String sku,
        String productName,
        String productDescription,
        String sizeLabel,
        BigDecimal sellingPrice,
        String imageUrl,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
