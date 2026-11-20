package com.retail.storeback.inventory.controller;

import com.retail.storeback.inventory.model.dto.DeadLossRequest;
import com.retail.storeback.inventory.model.dto.InventoryLogResponse;
import com.retail.storeback.inventory.model.dto.InventoryStockResponse;
import com.retail.storeback.inventory.model.dto.MarketImportRequest;
import com.retail.storeback.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/import")
    public ResponseEntity<InventoryStockResponse> importFromMarket(@Valid @RequestBody MarketImportRequest request) {
        InventoryStockResponse response = inventoryService.importFromMarket(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/dead-loss")
    public ResponseEntity<InventoryStockResponse> recordDeadLoss(@Valid @RequestBody DeadLossRequest request) {
        InventoryStockResponse response = inventoryService.recordDeadLoss(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stock/{productId}")
    public ResponseEntity<InventoryStockResponse> getStock(@PathVariable UUID productId) {
        InventoryStockResponse response = inventoryService.getStock(productId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/logs/{productId}")
    public ResponseEntity<List<InventoryLogResponse>> getLogsByProductId(@PathVariable UUID productId) {
        List<InventoryLogResponse> response = inventoryService.getLogsByProductId(productId);
        return ResponseEntity.ok(response);
    }
}
