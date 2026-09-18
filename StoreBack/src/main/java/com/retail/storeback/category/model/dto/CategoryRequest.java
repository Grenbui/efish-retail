package com.retail.storeback.category.model.dto;

import lombok.Builder;

@Builder
public record CategoryRequest(
        String categoryName,
        String categoryDescription
) {
}
