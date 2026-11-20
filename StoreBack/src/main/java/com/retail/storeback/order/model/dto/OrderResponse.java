package com.retail.storeback.order.model.dto;

import com.retail.storeback.order.model.OrderStatusEnum;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
public record OrderResponse(
        UUID orderId,
        String orderCode,
        UUID customerId,
        String customerName,
        String customerPhone,
        String shippingAddress,
        BigDecimal totalAmount,
        OrderStatusEnum status,
        String note,
        List<OrderItemResponse> orderItems,
        Instant createdAt,
        Instant updatedAt
) {
}
