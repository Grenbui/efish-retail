package com.retail.storeback.inventory.controller;

import com.retail.storeback.inventory.model.InventoryChangeType;
import com.retail.storeback.inventory.model.dto.DeadLossRequest;
import com.retail.storeback.inventory.model.dto.InventoryLogResponse;
import com.retail.storeback.inventory.model.dto.InventoryStockResponse;
import com.retail.storeback.inventory.model.dto.MarketImportRequest;
import com.retail.storeback.inventory.service.InventoryService;
import com.retail.storeback.utils.GlobalExceptionHandler;
import com.retail.storeback.utils.handleExceptions.InsufficientStockException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private InventoryController inventoryController;

    private UUID productId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(inventoryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        productId = UUID.randomUUID();
    }

    @Test
    @DisplayName("POST /api/inventory/import - Nhập kho chợ sỉ thành công trả về 200")
    void importFromMarket_whenValid_shouldReturn200() throws Exception {
        InventoryStockResponse response = InventoryStockResponse.builder()
                .productId(productId)
                .availableQty(50)
                .avgCost(new BigDecimal("22000.00"))
                .build();

        when(inventoryService.importFromMarket(any(MarketImportRequest.class))).thenReturn(response);

        String jsonPayload = """
                {
                    "productId": "%s",
                    "quantity": 50,
                    "unitCost": 22000.00,
                    "sourceName": "Sạp Năm Cá Cảnh",
                    "evidenceUrls": ["https://img.com/invoice1.jpg"],
                    "note": "Nhập đợt cá Guppy rồng đỏ"
                }
                """.formatted(productId);

        mockMvc.perform(post("/api/inventory/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId.toString()))
                .andExpect(jsonPath("$.availableQty").value(50))
                .andExpect(jsonPath("$.avgCost").value(22000.00));
    }

    @Test
    @DisplayName("POST /api/inventory/import - Trả về 400 khi số lượng <= 0")
    void importFromMarket_whenInvalidQuantity_shouldReturn400() throws Exception {
        String jsonPayload = """
                {
                    "productId": "%s",
                    "quantity": 0,
                    "unitCost": 22000.00
                }
                """.formatted(productId);

        mockMvc.perform(post("/api/inventory/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/inventory/import - Trả về 404 khi sản phẩm không tồn tại")
    void importFromMarket_whenProductNotFound_shouldReturn404() throws Exception {
        when(inventoryService.importFromMarket(any(MarketImportRequest.class)))
                .thenThrow(new EntityNotFoundException("Product not found"));

        String jsonPayload = """
                {
                    "productId": "%s",
                    "quantity": 10,
                    "unitCost": 20000.00
                }
                """.formatted(productId);

        mockMvc.perform(post("/api/inventory/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/inventory/dead-loss - Ghi nhận cá chết thành công trả về 200")
    void recordDeadLoss_whenValid_shouldReturn200() throws Exception {
        InventoryStockResponse response = InventoryStockResponse.builder()
                .productId(productId)
                .availableQty(45)
                .avgCost(new BigDecimal("22000.00"))
                .build();

        when(inventoryService.recordDeadLoss(any(DeadLossRequest.class))).thenReturn(response);

        String jsonPayload = """
                {
                    "productId": "%s",
                    "quantity": 5,
                    "evidenceUrls": ["https://img.com/dead_evidence.jpg"],
                    "note": "Cá chết do sốc PH"
                }
                """.formatted(productId);

        mockMvc.perform(post("/api/inventory/dead-loss")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableQty").value(45));
    }

    @Test
    @DisplayName("POST /api/inventory/dead-loss - Trả về 409 khi tồn kho không đủ để trừ hao hụt")
    void recordDeadLoss_whenInsufficientStock_shouldReturn409() throws Exception {
        when(inventoryService.recordDeadLoss(any(DeadLossRequest.class)))
                .thenThrow(new InsufficientStockException("Tồn kho không đủ để ghi nhận hao hụt"));

        String jsonPayload = """
                {
                    "productId": "%s",
                    "quantity": 100
                }
                """.formatted(productId);

        mockMvc.perform(post("/api/inventory/dead-loss")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /api/inventory/stock/{productId} - Tra cứu tồn kho thành công trả về 200")
    void getStock_whenExists_shouldReturn200() throws Exception {
        InventoryStockResponse response = InventoryStockResponse.builder()
                .productId(productId)
                .availableQty(30)
                .avgCost(new BigDecimal("18000.00"))
                .build();

        when(inventoryService.getStock(productId)).thenReturn(response);

        mockMvc.perform(get("/api/inventory/stock/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId.toString()))
                .andExpect(jsonPath("$.availableQty").value(30))
                .andExpect(jsonPath("$.avgCost").value(18000.00));
    }

    @Test
    @DisplayName("GET /api/inventory/stock/{productId} - Trả về 404 khi không tìm thấy tồn kho")
    void getStock_whenNotExists_shouldReturn404() throws Exception {
        when(inventoryService.getStock(productId))
                .thenThrow(new EntityNotFoundException("Stock not found"));

        mockMvc.perform(get("/api/inventory/stock/{productId}", productId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/inventory/logs/{productId} - Tra cứu lịch sử kho trả về 200")
    void getLogsByProductId_shouldReturn200() throws Exception {
        InventoryLogResponse logItem = InventoryLogResponse.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .changeType(InventoryChangeType.IMPORT_MARKET)
                .qtyChange(50)
                .unitCost(new BigDecimal("20000.00"))
                .createdAt(Instant.now())
                .build();

        when(inventoryService.getLogsByProductId(productId)).thenReturn(List.of(logItem));

        mockMvc.perform(get("/api/inventory/logs/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].changeType").value("IMPORT_MARKET"))
                .andExpect(jsonPath("$[0].qtyChange").value(50));
    }
}
