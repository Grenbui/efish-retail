package com.retail.storeback.category.model.dto;

import lombok.Builder;

@Builder
public record ProductRequest(
        String categoryId,
        String productName,
        String productDescription,
        int productQuantity,
        ProductStatusEnum productStatus
) {
}
