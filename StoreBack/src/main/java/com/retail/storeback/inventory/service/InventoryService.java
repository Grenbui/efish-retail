package com.retail.storeback.inventory.service;

import java.math.BigDecimal;
import java.util.UUID;

public interface InventoryService {

    /**
     * Deducts stock for an order item.
     * @param productId UUID of product
     * @param quantity quantity to deduct
     * @return Current average cost snapshot (unitCostSnap) for the product
     */
    BigDecimal deductStockForOrder(UUID productId, int quantity);

    /**
     * Restores stock when an order is cancelled.
     * @param productId UUID of product
     * @param quantity quantity to return
     * @param orderId reference order ID
     */
    void returnStockForCancel(UUID productId, int quantity, UUID orderId);
}
