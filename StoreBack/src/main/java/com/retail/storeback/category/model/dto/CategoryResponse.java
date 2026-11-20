package com.retail.storeback.category.model.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record CategoryResponse(
        UUID categoryId,
        String categoryName,
        String categoryDescription,
        Instant createdAt,
        Instant updatedAt
) {
}
