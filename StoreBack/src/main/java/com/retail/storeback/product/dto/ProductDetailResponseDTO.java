package com.retail.storeback.product.dto;

public record ProductDetailResponseDTO (
        ProductResponseDTO product,
        String description
) {
}
