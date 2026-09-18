package com.retail.storeback.category.model.dto;

import java.time.Instant;

public record ProductResponse(
        String productId,
        String categoryId,
        String productName,
        String productDescription,
        int productQuantity,
        ProductStatusEnum productStatus,
        Instant createdAt,
        Instant updatedAt
) {
}
