package com.retail.storeback.product.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CategoryRequestDTO(
        String name,
        String slug,
        UUID parentId,
        LocalDateTime createdAt
) {
}
