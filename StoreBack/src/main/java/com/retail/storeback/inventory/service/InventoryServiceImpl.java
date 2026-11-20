package com.retail.storeback.inventory.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
public class InventoryServiceImpl implements InventoryService {

    @Override
    public BigDecimal deductStockForOrder(UUID productId, int quantity) {
        log.info("Deducting stock for product {} with quantity {}", productId, quantity);
        // Default placeholder: returns 0.00 until InventoryStock table and logic are integrated in Module 2
        return BigDecimal.ZERO;
    }

    @Override
    public void returnStockForCancel(UUID productId, int quantity, UUID orderId) {
        log.info("Returning stock for product {} with quantity {} from cancelled order {}", productId, quantity, orderId);
        // Default placeholder
    }
}
