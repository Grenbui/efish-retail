package com.retail.storeback.inventory.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
public record MarketImportRequest(
        @NotNull(message = "Mã sản phẩm không được để trống")
        UUID productId,

        @Positive(message = "Số lượng nhập phải lớn hơn 0")
        int quantity,

        @NotNull(message = "Đơn giá vốn không được để trống")
        @DecimalMin(value = "0.0", inclusive = true, message = "Đơn giá vốn phải >= 0")
        BigDecimal unitCost,

        String sourceName,
        List<String> evidenceUrls,
        String note
) {
}
