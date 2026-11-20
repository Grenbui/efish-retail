package com.retail.storeback.inventory.service;

import com.retail.storeback.category.repository.ProductRepository;
import com.retail.storeback.inventory.model.InventoryChangeType;
import com.retail.storeback.inventory.model.InventoryLog;
import com.retail.storeback.inventory.model.InventoryStock;
import com.retail.storeback.inventory.model.dto.DeadLossRequest;
import com.retail.storeback.inventory.model.dto.InventoryLogResponse;
import com.retail.storeback.inventory.model.dto.InventoryStockResponse;
import com.retail.storeback.inventory.model.dto.MarketImportRequest;
import com.retail.storeback.inventory.model.mapper.InventoryMapper;
import com.retail.storeback.inventory.repository.InventoryLogRepository;
import com.retail.storeback.inventory.repository.InventoryStockRepository;
import com.retail.storeback.utils.handleExceptions.InsufficientStockException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryStockRepository stockRepository;
    private final InventoryLogRepository logRepository;
    private final ProductRepository productRepository;
    private final InventoryMapper inventoryMapper;

    @Override
    @Transactional
    public InventoryStockResponse importFromMarket(MarketImportRequest request) {
        log.info("Importing stock from market for product: {}, qty: {}, unitCost: {}",
                request.productId(), request.quantity(), request.unitCost());

        if (!productRepository.existsById(request.productId())) {
            throw new EntityNotFoundException("Product not found with id: " + request.productId());
        }

        InventoryStock stock = stockRepository.findById(request.productId())
                .orElseGet(() -> InventoryStock.builder()
                        .productId(request.productId())
                        .availableQty(0)
                        .avgCost(BigDecimal.ZERO)
                        .build());

        int currentQty = stock.getAvailableQty();
        BigDecimal currentAvgCost = stock.getAvgCost() != null ? stock.getAvgCost() : BigDecimal.ZERO;
        int importQty = request.quantity();
        BigDecimal importCost = request.unitCost();

        BigDecimal newAvgCost;
        if (currentQty <= 0) {
            newAvgCost = importCost.setScale(2, RoundingMode.HALF_UP);
        } else {
            BigDecimal currentValue = currentAvgCost.multiply(BigDecimal.valueOf(currentQty));
            BigDecimal importValue = importCost.multiply(BigDecimal.valueOf(importQty));
            int newTotalQty = currentQty + importQty;
            newAvgCost = currentValue.add(importValue)
                    .divide(BigDecimal.valueOf(newTotalQty), 2, RoundingMode.HALF_UP);
        }

        stock.setAvailableQty(currentQty + importQty);
        stock.setAvgCost(newAvgCost);
        InventoryStock savedStock = stockRepository.save(stock);

        InventoryLog inventoryLog = InventoryLog.builder()
                .productId(request.productId())
                .changeType(InventoryChangeType.IMPORT_MARKET)
                .qtyChange(importQty)
                .unitCost(importCost.setScale(2, RoundingMode.HALF_UP))
                .sourceName(request.sourceName())
                .evidenceUrls(request.evidenceUrls())
                .note(request.note())
                .build();
        logRepository.save(inventoryLog);

        return inventoryMapper.toStockResponse(savedStock);
    }

    @Override
    @Transactional
    public InventoryStockResponse recordDeadLoss(DeadLossRequest request) {
        log.info("Recording dead loss for product: {}, qty: {}", request.productId(), request.quantity());

        if (!productRepository.existsById(request.productId())) {
            throw new EntityNotFoundException("Product not found with id: " + request.productId());
        }

        InventoryStock stock = stockRepository.findById(request.productId())
                .orElseThrow(() -> new InsufficientStockException("Sản phẩm chưa có tồn kho để ghi nhận hao hụt"));

        if (stock.getAvailableQty() < request.quantity()) {
            throw new InsufficientStockException("Tồn kho không đủ để ghi nhận hao hụt cá chết. Hiện có: "
                    + stock.getAvailableQty() + ", yêu cầu giảm: " + request.quantity());
        }

        stock.setAvailableQty(stock.getAvailableQty() - request.quantity());
        InventoryStock savedStock = stockRepository.save(stock);

        InventoryLog inventoryLog = InventoryLog.builder()
                .productId(request.productId())
                .changeType(InventoryChangeType.DEAD_LOSS)
                .qtyChange(-request.quantity())
                .unitCost(stock.getAvgCost())
                .evidenceUrls(request.evidenceUrls())
                .note(request.note())
                .build();
        logRepository.save(inventoryLog);

        return inventoryMapper.toStockResponse(savedStock);
    }

    @Override
    @Transactional
    public BigDecimal deductStockForOrder(UUID productId, int quantity) {
        return deductStockForOrder(productId, quantity, null);
    }

    @Override
    @Transactional
    public BigDecimal deductStockForOrder(UUID productId, int quantity, UUID orderId) {
        log.info("Deducting stock for product: {}, qty: {}, orderId: {}", productId, quantity, orderId);

        InventoryStock stock = stockRepository.findById(productId)
                .orElseThrow(() -> new InsufficientStockException("Tồn kho cá không đủ để đáp ứng"));

        if (stock.getAvailableQty() < quantity) {
            throw new InsufficientStockException("Tồn kho cá không đủ để đáp ứng");
        }

        stock.setAvailableQty(stock.getAvailableQty() - quantity);
        stockRepository.save(stock);

        InventoryLog inventoryLog = InventoryLog.builder()
                .productId(productId)
                .changeType(InventoryChangeType.SELL_ORDER)
                .qtyChange(-quantity)
                .unitCost(stock.getAvgCost())
                .referenceId(orderId)
                .build();
        logRepository.save(inventoryLog);

        return stock.getAvgCost();
    }

    @Override
    @Transactional
    public void returnStockForCancel(UUID productId, int quantity, UUID orderId) {
        log.info("Returning stock for product: {}, qty: {}, orderId: {}", productId, quantity, orderId);

        InventoryStock stock = stockRepository.findById(productId)
                .orElseGet(() -> InventoryStock.builder()
                        .productId(productId)
                        .availableQty(0)
                        .avgCost(BigDecimal.ZERO)
                        .build());

        stock.setAvailableQty(stock.getAvailableQty() + quantity);
        stockRepository.save(stock);

        InventoryLog inventoryLog = InventoryLog.builder()
                .productId(productId)
                .changeType(InventoryChangeType.CANCEL_RETURN)
                .qtyChange(quantity)
                .referenceId(orderId)
                .build();
        logRepository.save(inventoryLog);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryStockResponse getStock(UUID productId) {
        InventoryStock stock = stockRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy thông tin tồn kho cho sản phẩm: " + productId));
        return inventoryMapper.toStockResponse(stock);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryLogResponse> getLogsByProductId(UUID productId) {
        List<InventoryLog> logs = logRepository.findByProductIdOrderByCreatedAtDesc(productId);
        return inventoryMapper.toLogResponseList(logs);
    }
}
