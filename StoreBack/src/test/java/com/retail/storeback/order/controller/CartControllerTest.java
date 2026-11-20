package com.retail.storeback.order.controller;

import com.retail.storeback.order.model.dto.CartItemRequest;
import com.retail.storeback.order.model.dto.CartItemResponse;
import com.retail.storeback.order.model.dto.CartResponse;
import com.retail.storeback.order.service.CartService;
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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cartController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/cart/{cartId} - Lấy giỏ hàng thành công")
    void getCartById_whenExists_shouldReturn200() throws Exception {
        UUID cartId = UUID.randomUUID();
        CartResponse response = CartResponse.builder()
                .cartId(cartId)
                .items(Collections.emptyList())
                .totalAmount(BigDecimal.ZERO)
                .totalQuantity(0)
                .build();

        when(cartService.getCartById(cartId)).thenReturn(response);

        mockMvc.perform(get("/api/cart/{cartId}", cartId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(cartId.toString()));
    }

    @Test
    @DisplayName("GET /api/cart/{cartId} - Trả về 404 khi không tìm thấy")
    void getCartById_whenNotFound_shouldReturn404() throws Exception {
        UUID cartId = UUID.randomUUID();
        when(cartService.getCartById(cartId))
                .thenThrow(new EntityNotFoundException("Cart not found with id: " + cartId));

        mockMvc.perform(get("/api/cart/{cartId}", cartId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Cart not found with id: " + cartId));
    }

    @Test
    @DisplayName("POST /api/cart/{cartId}/items - Thêm cá vào giỏ hàng thành công")
    void addItemToCart_whenValid_shouldReturn200() throws Exception {
        UUID cartId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        CartItemResponse itemResponse = CartItemResponse.builder()
                .cartItemId(itemId)
                .productId(productId)
                .productName("Cá Guppy Rồng Đỏ")
                .productPrice(new BigDecimal("50000.00"))
                .quantity(2)
                .subTotal(new BigDecimal("100000.00"))
                .build();

        CartResponse response = CartResponse.builder()
                .cartId(cartId)
                .items(List.of(itemResponse))
                .totalAmount(new BigDecimal("100000.00"))
                .totalQuantity(2)
                .build();

        when(cartService.addItemToCart(eq(cartId), any(CartItemRequest.class))).thenReturn(response);

        String jsonPayload = """
                {
                    "productId": "%s",
                    "quantity": 2
                }
                """.formatted(productId);

        mockMvc.perform(post("/api/cart/{cartId}/items", cartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productName").value("Cá Guppy Rồng Đỏ"))
                .andExpect(jsonPath("$.totalAmount").value(100000.00));
    }

    @Test
    @DisplayName("PATCH /api/cart/{cartId}/items/{cartItemId} - Cập nhật số lượng thành công")
    void updateItemQuantity_whenValid_shouldReturn200() throws Exception {
        UUID cartId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        CartResponse response = CartResponse.builder()
                .cartId(cartId)
                .totalQuantity(5)
                .build();

        when(cartService.updateItemQuantity(cartId, itemId, 5)).thenReturn(response);

        mockMvc.perform(patch("/api/cart/{cartId}/items/{cartItemId}", cartId, itemId)
                        .param("quantity", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalQuantity").value(5));
    }

    @Test
    @DisplayName("DELETE /api/cart/{cartId}/items/{cartItemId} - Xóa mục khỏi giỏ thành công")
    void removeItemFromCart_whenValid_shouldReturn200() throws Exception {
        UUID cartId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        CartResponse response = CartResponse.builder()
                .cartId(cartId)
                .items(Collections.emptyList())
                .build();

        when(cartService.removeItemFromCart(cartId, itemId)).thenReturn(response);

        mockMvc.perform(delete("/api/cart/{cartId}/items/{cartItemId}", cartId, itemId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/cart/{cartId} - Xóa trắng giỏ hàng thành công")
    void clearCart_whenValid_shouldReturn200() throws Exception {
        UUID cartId = UUID.randomUUID();
        CartResponse response = CartResponse.builder()
                .cartId(cartId)
                .items(Collections.emptyList())
                .build();

        when(cartService.clearCart(cartId)).thenReturn(response);

        mockMvc.perform(delete("/api/cart/{cartId}", cartId))
                .andExpect(status().isOk());
    }
}
