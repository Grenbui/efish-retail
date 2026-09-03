package com.retail.storeback.product.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record CategoryRequestDTO(
        String name,
        String slug,
        UUID parentId,
        LocalDateTime createdAt
) {
}
