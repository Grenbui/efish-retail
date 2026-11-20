package com.retail.storeback.inventory.service;

import com.retail.storeback.inventory.model.dto.DeadLossRequest;
import com.retail.storeback.inventory.model.dto.InventoryLogResponse;
import com.retail.storeback.inventory.model.dto.InventoryStockResponse;
import com.retail.storeback.inventory.model.dto.MarketImportRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface InventoryService {

    /**
     * Nhập cá cảnh từ chợ sỉ, tăng tồn kho và tính lại giá vốn bình quân gia quyền.
     */
    InventoryStockResponse importFromMarket(MarketImportRequest request);

    /**
     * Ghi nhận cá chết / hao hụt (DOA / chết bể), giảm tồn kho (chặn không cho âm kho).
     */
    InventoryStockResponse recordDeadLoss(DeadLossRequest request);

    /**
     * Deducts stock for an order item.
     * @param productId UUID of product
     * @param quantity quantity to deduct
     * @return Current average cost snapshot (unitCostSnap) for the product
     */
    BigDecimal deductStockForOrder(UUID productId, int quantity);

    /**
     * Deducts stock for an order item with order reference.
     */
    BigDecimal deductStockForOrder(UUID productId, int quantity, UUID orderId);

    /**
     * Restores stock when an order is cancelled.
     * @param productId UUID of product
     * @param quantity quantity to return
     * @param orderId reference order ID
     */
    void returnStockForCancel(UUID productId, int quantity, UUID orderId);

    /**
     * Tra cứu thông tin tồn kho và giá vốn của sản phẩm.
     */
    InventoryStockResponse getStock(UUID productId);

    /**
     * Tra cứu lịch sử biến động kho của sản phẩm.
     */
    List<InventoryLogResponse> getLogsByProductId(UUID productId);
}
