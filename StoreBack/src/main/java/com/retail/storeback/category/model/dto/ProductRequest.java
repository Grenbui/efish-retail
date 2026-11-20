package com.retail.storeback.category.model.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record ProductRequest(
        UUID categoryId,
        String sku,
        String productName,
        String productDescription,
        String sizeLabel,
        BigDecimal sellingPrice,
        String imageUrl,
        Boolean active
) {
}
