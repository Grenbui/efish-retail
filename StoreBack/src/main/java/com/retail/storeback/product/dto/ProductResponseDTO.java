package com.retail.storeback.product.dto;

import com.retail.storeback.product.model.ProductStatusEnum;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProductResponseDTO(
        UUID id,
        UUID categoryId,
        String name,

        @PositiveOrZero
        Double price,
        String imageUrl,

        @Enumerated(EnumType.STRING)
        ProductStatusEnum status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
