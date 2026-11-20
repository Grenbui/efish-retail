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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryStockRepository stockRepository;

    @Mock
    private InventoryLogRepository logRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryMapper inventoryMapper;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private UUID productId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
    }

    @Test
    @DisplayName("importFromMarket - Nhập lần đầu thành công với giá vốn và số lượng ban đầu")
    void importFromMarket_whenFirstImport_shouldSetQuantityAndCost() {
        MarketImportRequest request = MarketImportRequest.builder()
                .productId(productId)
                .quantity(10)
                .unitCost(new BigDecimal("20000.00"))
                .sourceName("Sạp cá Ba Thuận Kiều")
                .evidenceUrls(List.of("https://image.com/bill1.png"))
                .note("Nhập đợt cá Neon đầu tiên")
                .build();

        when(productRepository.existsById(productId)).thenReturn(true);
        when(stockRepository.findById(productId)).thenReturn(Optional.empty());
        when(stockRepository.save(any(InventoryStock.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryStockResponse expectedResponse = InventoryStockResponse.builder()
                .productId(productId)
                .availableQty(10)
                .avgCost(new BigDecimal("20000.00"))
                .build();
        when(inventoryMapper.toStockResponse(any(InventoryStock.class))).thenReturn(expectedResponse);

        InventoryStockResponse result = inventoryService.importFromMarket(request);

        assertThat(result.availableQty()).isEqualTo(10);
        assertThat(result.avgCost()).isEqualByComparingTo("20000.00");

        ArgumentCaptor<InventoryStock> stockCaptor = ArgumentCaptor.forClass(InventoryStock.class);
        verify(stockRepository).save(stockCaptor.capture());
        InventoryStock savedStock = stockCaptor.getValue();
        assertThat(savedStock.getAvailableQty()).isEqualTo(10);
        assertThat(savedStock.getAvgCost()).isEqualByComparingTo("20000.00");

        ArgumentCaptor<InventoryLog> logCaptor = ArgumentCaptor.forClass(InventoryLog.class);
        verify(logRepository).save(logCaptor.capture());
        InventoryLog savedLog = logCaptor.getValue();
        assertThat(savedLog.getChangeType()).isEqualTo(InventoryChangeType.IMPORT_MARKET);
        assertThat(savedLog.getQtyChange()).isEqualTo(10);
        assertThat(savedLog.getUnitCost()).isEqualByComparingTo("20000.00");
        assertThat(savedLog.getSourceName()).isEqualTo("Sạp cá Ba Thuận Kiều");
    }

    @Test
    @DisplayName("importFromMarket - Khi sản phẩm không tồn tại thì ném EntityNotFoundException")
    void importFromMarket_whenProductNotExists_shouldThrowEntityNotFoundException() {
        MarketImportRequest request = MarketImportRequest.builder()
                .productId(productId)
                .quantity(10)
                .unitCost(new BigDecimal("20000.00"))
                .build();

        when(productRepository.existsById(productId)).thenReturn(false);

        assertThatThrownBy(() -> inventoryService.importFromMarket(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Product not found");

        verify(stockRepository, never()).save(any());
        verify(logRepository, never()).save(any());
    }

    @Test
    @DisplayName("importFromMarket - Tính đúng giá vốn bình quân gia quyền khi đã có tồn kho trước đó")
    void importFromMarket_whenExistingStock_shouldCalculateWeightedAverageCost() {
        // Đang có 10 con với giá 20,000đ
        InventoryStock existingStock = InventoryStock.builder()
                .productId(productId)
                .availableQty(10)
                .avgCost(new BigDecimal("20000.00"))
                .build();

        // Nhập thêm 10 con với giá 30,000đ
        // Tổng tiền = (10 * 20,000) + (10 * 30,000) = 500,000đ
        // Tổng số lượng = 20 con
        // Giá vốn mới = 500,000 / 20 = 25,000.00đ
        MarketImportRequest request = MarketImportRequest.builder()
                .productId(productId)
                .quantity(10)
                .unitCost(new BigDecimal("30000.00"))
                .sourceName("Chợ Kim Biên")
                .build();

        when(productRepository.existsById(productId)).thenReturn(true);
        when(stockRepository.findById(productId)).thenReturn(Optional.of(existingStock));
        when(stockRepository.save(any(InventoryStock.class))).thenAnswer(invocation -> invocation.getArgument(0));

        inventoryService.importFromMarket(request);

        ArgumentCaptor<InventoryStock> stockCaptor = ArgumentCaptor.forClass(InventoryStock.class);
        verify(stockRepository).save(stockCaptor.capture());
        InventoryStock savedStock = stockCaptor.getValue();

        assertThat(savedStock.getAvailableQty()).isEqualTo(20);
        assertThat(savedStock.getAvgCost()).isEqualByComparingTo("25000.00");
    }

    @Test
    @DisplayName("recordDeadLoss - Ghi nhận cá chết thành công, trừ kho và giữ nguyên giá vốn")
    void recordDeadLoss_whenValid_shouldDeductStockAndSaveLog() {
        InventoryStock existingStock = InventoryStock.builder()
                .productId(productId)
                .availableQty(10)
                .avgCost(new BigDecimal("25000.00"))
                .build();

        DeadLossRequest request = DeadLossRequest.builder()
                .productId(productId)
                .quantity(3)
                .evidenceUrls(List.of("https://image.com/dead_fish.jpg"))
                .note("Cá chết do sốc nước khi thả bể")
                .build();

        when(productRepository.existsById(productId)).thenReturn(true);
        when(stockRepository.findById(productId)).thenReturn(Optional.of(existingStock));
        when(stockRepository.save(any(InventoryStock.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryStockResponse expectedResponse = InventoryStockResponse.builder()
                .productId(productId)
                .availableQty(7)
                .avgCost(new BigDecimal("25000.00"))
                .build();
        when(inventoryMapper.toStockResponse(any(InventoryStock.class))).thenReturn(expectedResponse);

        InventoryStockResponse response = inventoryService.recordDeadLoss(request);

        assertThat(response.availableQty()).isEqualTo(7);

        ArgumentCaptor<InventoryStock> stockCaptor = ArgumentCaptor.forClass(InventoryStock.class);
        verify(stockRepository).save(stockCaptor.capture());
        InventoryStock savedStock = stockCaptor.getValue();
        assertThat(savedStock.getAvailableQty()).isEqualTo(7);
        assertThat(savedStock.getAvgCost()).isEqualByComparingTo("25000.00");

        ArgumentCaptor<InventoryLog> logCaptor = ArgumentCaptor.forClass(InventoryLog.class);
        verify(logRepository).save(logCaptor.capture());
        InventoryLog savedLog = logCaptor.getValue();
        assertThat(savedLog.getChangeType()).isEqualTo(InventoryChangeType.DEAD_LOSS);
        assertThat(savedLog.getQtyChange()).isEqualTo(-3);
        assertThat(savedLog.getUnitCost()).isEqualByComparingTo("25000.00");
    }

    @Test
    @DisplayName("recordDeadLoss - Số lượng chết lớn hơn tồn kho thì ném InsufficientStockException")
    void recordDeadLoss_whenLossQtyExceedsAvailable_shouldThrowInsufficientStockException() {
        InventoryStock existingStock = InventoryStock.builder()
                .productId(productId)
                .availableQty(2)
                .avgCost(new BigDecimal("25000.00"))
                .build();

        DeadLossRequest request = DeadLossRequest.builder()
                .productId(productId)
                .quantity(5)
                .build();

        when(productRepository.existsById(productId)).thenReturn(true);
        when(stockRepository.findById(productId)).thenReturn(Optional.of(existingStock));

        assertThatThrownBy(() -> inventoryService.recordDeadLoss(request))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Tồn kho không đủ để ghi nhận hao hụt");

        verify(stockRepository, never()).save(any());
        verify(logRepository, never()).save(any());
    }

    @Test
    @DisplayName("deductStockForOrder - Trừ kho đơn hàng thành công và trả về snapshot avgCost")
    void deductStockForOrder_whenValid_shouldDeductAndReturnAvgCost() {
        UUID orderId = UUID.randomUUID();
        InventoryStock existingStock = InventoryStock.builder()
                .productId(productId)
                .availableQty(15)
                .avgCost(new BigDecimal("35000.00"))
                .build();

        when(stockRepository.findById(productId)).thenReturn(Optional.of(existingStock));

        BigDecimal snapshotCost = inventoryService.deductStockForOrder(productId, 5, orderId);

        assertThat(snapshotCost).isEqualByComparingTo("35000.00");
        assertThat(existingStock.getAvailableQty()).isEqualTo(10);

        ArgumentCaptor<InventoryLog> logCaptor = ArgumentCaptor.forClass(InventoryLog.class);
        verify(logRepository).save(logCaptor.capture());
        InventoryLog savedLog = logCaptor.getValue();
        assertThat(savedLog.getChangeType()).isEqualTo(InventoryChangeType.SELL_ORDER);
        assertThat(savedLog.getQtyChange()).isEqualTo(-5);
        assertThat(savedLog.getReferenceId()).isEqualTo(orderId);
    }

    @Test
    @DisplayName("deductStockForOrder - Tồn kho không đủ thì ném InsufficientStockException")
    void deductStockForOrder_whenInsufficient_shouldThrowInsufficientStockException() {
        InventoryStock existingStock = InventoryStock.builder()
                .productId(productId)
                .availableQty(3)
                .avgCost(new BigDecimal("35000.00"))
                .build();

        when(stockRepository.findById(productId)).thenReturn(Optional.of(existingStock));

        assertThatThrownBy(() -> inventoryService.deductStockForOrder(productId, 5))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Tồn kho cá không đủ để đáp ứng");
    }

    @Test
    @DisplayName("returnStockForCancel - Hoàn lại kho khi hủy đơn hàng")
    void returnStockForCancel_shouldIncreaseQtyAndSaveLog() {
        UUID orderId = UUID.randomUUID();
        InventoryStock existingStock = InventoryStock.builder()
                .productId(productId)
                .availableQty(10)
                .avgCost(new BigDecimal("35000.00"))
                .build();

        when(stockRepository.findById(productId)).thenReturn(Optional.of(existingStock));

        inventoryService.returnStockForCancel(productId, 5, orderId);

        assertThat(existingStock.getAvailableQty()).isEqualTo(15);

        ArgumentCaptor<InventoryLog> logCaptor = ArgumentCaptor.forClass(InventoryLog.class);
        verify(logRepository).save(logCaptor.capture());
        InventoryLog savedLog = logCaptor.getValue();
        assertThat(savedLog.getChangeType()).isEqualTo(InventoryChangeType.CANCEL_RETURN);
        assertThat(savedLog.getQtyChange()).isEqualTo(5);
        assertThat(savedLog.getReferenceId()).isEqualTo(orderId);
    }

    @Test
    @DisplayName("getStock - Trả về thông tin tồn kho khi tồn tại")
    void getStock_whenExists_shouldReturnResponse() {
        InventoryStock existingStock = InventoryStock.builder()
                .productId(productId)
                .availableQty(20)
                .avgCost(new BigDecimal("15000.00"))
                .updatedAt(Instant.now())
                .build();

        InventoryStockResponse expectedResponse = InventoryStockResponse.builder()
                .productId(productId)
                .availableQty(20)
                .avgCost(new BigDecimal("15000.00"))
                .build();

        when(stockRepository.findById(productId)).thenReturn(Optional.of(existingStock));
        when(inventoryMapper.toStockResponse(existingStock)).thenReturn(expectedResponse);

        InventoryStockResponse result = inventoryService.getStock(productId);

        assertThat(result.availableQty()).isEqualTo(20);
        assertThat(result.avgCost()).isEqualByComparingTo("15000.00");
    }

    @Test
    @DisplayName("getStock - Ném EntityNotFoundException khi không tìm thấy tồn kho")
    void getStock_whenNotExists_shouldThrowEntityNotFoundException() {
        when(stockRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.getStock(productId))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("getLogsByProductId - Trả về lịch sử biến động kho")
    void getLogsByProductId_shouldReturnLogsList() {
        InventoryLog log1 = InventoryLog.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .changeType(InventoryChangeType.IMPORT_MARKET)
                .qtyChange(20)
                .build();

        when(logRepository.findByProductIdOrderByCreatedAtDesc(productId)).thenReturn(List.of(log1));
        when(inventoryMapper.toLogResponseList(anyList())).thenReturn(List.of(
                InventoryLogResponse.builder()
                        .id(log1.getId())
                        .productId(productId)
                        .changeType(InventoryChangeType.IMPORT_MARKET)
                        .qtyChange(20)
                        .build()
        ));

        List<InventoryLogResponse> logs = inventoryService.getLogsByProductId(productId);

        assertThat(logs).hasSize(1);
        assertThat(logs.getFirst().changeType()).isEqualTo(InventoryChangeType.IMPORT_MARKET);
    }
}
