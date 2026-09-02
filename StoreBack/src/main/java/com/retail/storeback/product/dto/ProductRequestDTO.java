package com.retail.storeback.product.dto;

import com.retail.storeback.product.model.ProductStatusEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProductRequestDTO(
        UUID categoryId,
        String name,
        String description,
        String imageUrl,
        Double price,
        ProductStatusEnum status,
        LocalDateTime createdAt
) {
}
