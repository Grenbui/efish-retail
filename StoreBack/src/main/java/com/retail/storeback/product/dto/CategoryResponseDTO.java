package com.retail.storeback.product.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CategoryResponseDTO(
        UUID id,
        String name,
        String slug,
        UUID parentId,
        LocalDateTime createdAt
) {
}
