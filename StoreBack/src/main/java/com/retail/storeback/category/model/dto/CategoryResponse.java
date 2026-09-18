package com.retail.storeback.category.model.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record CategoryResponse(
        String categoryId,
        String categoryName,
        String categoryDescription,
        Instant createdAt,
        Instant updatedAt
) {
}
