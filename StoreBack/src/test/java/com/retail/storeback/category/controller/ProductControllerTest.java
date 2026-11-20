package com.retail.storeback.category.controller;

import com.retail.storeback.category.model.dto.ProductRequest;
import com.retail.storeback.category.model.dto.ProductResponse;
import com.retail.storeback.category.service.ProductService;
import com.retail.storeback.utils.GlobalExceptionHandler;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/products - Trả về 200 và danh sách sản phẩm")
    void getAllProducts_whenCalled_shouldReturn200AndList() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        ProductResponse response = ProductResponse.builder()
                .productId(productId)
                .categoryId(categoryId)
                .categoryName("Cá Tetra")
                .sku("FISH-TETRA-01")
                .productName("Cá Sóc Đầu Đỏ")
                .sizeLabel("Size S 2-3cm")
                .sellingPrice(new BigDecimal("15000.00"))
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(productService.getAllProducts()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].productId").value(productId.toString()))
                .andExpect(jsonPath("$[0].sku").value("FISH-TETRA-01"))
                .andExpect(jsonPath("$[0].productName").value("Cá Sóc Đầu Đỏ"))
                .andExpect(jsonPath("$[0].sellingPrice").value(15000.00));
    }

    @Test
    @DisplayName("GET /api/product/{id} - Trả về 200 khi sản phẩm tồn tại")
    void getProductById_whenExists_shouldReturn200AndProduct() throws Exception {
        UUID productId = UUID.randomUUID();
        ProductResponse response = ProductResponse.builder()
                .productId(productId)
                .sku("FISH-BETTA-02")
                .productName("Cá Betta Dumbo")
                .sizeLabel("Size M")
                .sellingPrice(new BigDecimal("80000.00"))
                .active(true)
                .build();

        when(productService.getProductById(productId)).thenReturn(response);

        mockMvc.perform(get("/api/product/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId.toString()))
                .andExpect(jsonPath("$.productName").value("Cá Betta Dumbo"))
                .andExpect(jsonPath("$.sizeLabel").value("Size M"));
    }

    @Test
    @DisplayName("GET /api/product/{id} - Trả về 404 khi không tìm thấy")
    void getProductById_whenNotFound_shouldReturn404() throws Exception {
        UUID productId = UUID.randomUUID();
        when(productService.getProductById(productId))
                .thenThrow(new EntityNotFoundException("Product not found with id: " + productId));

        mockMvc.perform(get("/api/product/{id}", productId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Product not found with id: " + productId));
    }

    @Test
    @DisplayName("GET /api/products/category/{categoryId} - Trả về danh sách sản phẩm theo danh mục")
    void getProductsByCategory_whenCalled_shouldReturn200AndList() throws Exception {
        UUID categoryId = UUID.randomUUID();
        ProductResponse response = ProductResponse.builder()
                .productId(UUID.randomUUID())
                .categoryId(categoryId)
                .sku("FISH-GUPPY-01")
                .productName("Guppy Full Gold")
                .sellingPrice(new BigDecimal("60000.00"))
                .build();

        when(productService.getProductsByCategory(categoryId)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/products/category/{categoryId}", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sku").value("FISH-GUPPY-01"))
                .andExpect(jsonPath("$[0].productName").value("Guppy Full Gold"));
    }

    @Test
    @DisplayName("POST /api/product - Trả về 201 Created khi tạo mới hợp lệ")
    void createProduct_whenValid_shouldReturn201Created() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        ProductResponse response = ProductResponse.builder()
                .productId(productId)
                .categoryId(categoryId)
                .categoryName("Cá Thủy Sinh")
                .sku("FISH-CARDINAL-M")
                .productName("Cá Cardinal")
                .sizeLabel("Size M 3cm")
                .sellingPrice(new BigDecimal("30000.00"))
                .active(true)
                .build();

        when(productService.create(any(ProductRequest.class))).thenReturn(response);

        String jsonPayload = """
                {
                    "categoryId": "%s",
                    "sku": "FISH-CARDINAL-M",
                    "productName": "Cá Cardinal",
                    "sizeLabel": "Size M 3cm",
                    "sellingPrice": 30000.00,
                    "active": true
                }
                """.formatted(categoryId);

        mockMvc.perform(post("/api/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(productId.toString()))
                .andExpect(jsonPath("$.sku").value("FISH-CARDINAL-M"))
                .andExpect(jsonPath("$.productName").value("Cá Cardinal"));
    }

    @Test
    @DisplayName("POST /api/product - Trả về 400 Bad Request khi SKU đã tồn tại")
    void createProduct_whenSkuDuplicate_shouldReturn400BadRequest() throws Exception {
        when(productService.create(any(ProductRequest.class)))
                .thenThrow(new IllegalArgumentException("Product with SKU already exists: DUP-SKU"));

        String jsonPayload = """
                {
                    "sku": "DUP-SKU",
                    "productName": "Cá Trùng Mã",
                    "sellingPrice": 50000.00
                }
                """;

        mockMvc.perform(post("/api/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Product with SKU already exists: DUP-SKU"));
    }

    @Test
    @DisplayName("POST /api/product - Trả về 404 khi Category không tồn tại")
    void createProduct_whenCategoryNotFound_shouldReturn404NotFound() throws Exception {
        UUID ghostCategoryId = UUID.randomUUID();
        when(productService.create(any(ProductRequest.class)))
                .thenThrow(new EntityNotFoundException("Category not found with id: " + ghostCategoryId));

        String jsonPayload = """
                {
                    "categoryId": "%s",
                    "sku": "NEW-SKU",
                    "productName": "Cá Không Có Danh Mục",
                    "sellingPrice": 50000.00
                }
                """.formatted(ghostCategoryId);

        mockMvc.perform(post("/api/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Category not found with id: " + ghostCategoryId));
    }

    @Test
    @DisplayName("PATCH /api/product/{id} - Trả về 200 khi cập nhật thành công")
    void updateProduct_whenValid_shouldReturn200() throws Exception {
        UUID productId = UUID.randomUUID();
        ProductResponse response = ProductResponse.builder()
                .productId(productId)
                .productName("Tên Cá Mới")
                .sellingPrice(new BigDecimal("99000.00"))
                .build();

        when(productService.update(any(ProductRequest.class), eq(productId))).thenReturn(response);

        String jsonPayload = """
                {
                    "productName": "Tên Cá Mới",
                    "sellingPrice": 99000.00
                }
                """;

        mockMvc.perform(patch("/api/product/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId.toString()))
                .andExpect(jsonPath("$.productName").value("Tên Cá Mới"));
    }

    @Test
    @DisplayName("PATCH /api/product/{id} - Trả về 404 khi ID sản phẩm không tồn tại")
    void updateProduct_whenNotFound_shouldReturn404() throws Exception {
        UUID productId = UUID.randomUUID();
        when(productService.update(any(ProductRequest.class), eq(productId)))
                .thenThrow(new EntityNotFoundException("Product not found with id: " + productId));

        String jsonPayload = """
                {
                    "productName": "Tên Cá"
                }
                """;

        mockMvc.perform(patch("/api/product/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Product not found with id: " + productId));
    }

    @Test
    @DisplayName("DELETE /api/product/{id} - Trả về 200 khi xóa thành công")
    void deleteProduct_whenExists_shouldReturn200() throws Exception {
        UUID productId = UUID.randomUUID();
        when(productService.delete(productId)).thenReturn("Product deleted successfully");

        mockMvc.perform(delete("/api/product/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(content().string("Product deleted successfully"));
    }

    @Test
    @DisplayName("DELETE /api/product/{id} - Trả về 404 khi ID không tồn tại")
    void deleteProduct_whenNotFound_shouldReturn404() throws Exception {
        UUID productId = UUID.randomUUID();
        when(productService.delete(productId))
                .thenThrow(new EntityNotFoundException("Product not found with id: " + productId));

        mockMvc.perform(delete("/api/product/{id}", productId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Product not found with id: " + productId));
    }
}
