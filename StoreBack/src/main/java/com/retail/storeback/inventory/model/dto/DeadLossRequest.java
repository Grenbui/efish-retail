package com.retail.storeback.inventory.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record DeadLossRequest(
        @NotNull(message = "Mã sản phẩm không được để trống")
        UUID productId,

        @Positive(message = "Số lượng cá chết/hao hụt phải lớn hơn 0")
        int quantity,

        List<String> evidenceUrls,
        String note
) {
}
