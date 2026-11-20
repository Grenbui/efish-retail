package com.retail.storeback.order.controller;

import com.retail.storeback.order.model.OrderStatusEnum;
import com.retail.storeback.order.model.dto.CheckoutRequest;
import com.retail.storeback.order.model.dto.OrderItemResponse;
import com.retail.storeback.order.model.dto.OrderResponse;
import com.retail.storeback.order.service.OrderService;
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
import org.springframework.web.filter.CharacterEncodingFilter;

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
class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/orders/checkout - Chốt đơn thành công trả về 201 Created")
    void checkout_whenValid_shouldReturn201() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();

        OrderItemResponse item = OrderItemResponse.builder()
                .orderItemId(UUID.randomUUID())
                .productId(UUID.randomUUID())
                .productNameSnap("Cá Thần Tiên")
                .qty(2)
                .unitPrice(new BigDecimal("75000.00"))
                .subTotal(new BigDecimal("150000.00"))
                .unitCostSnap(new BigDecimal("40000.00"))
                .build();

        OrderResponse response = OrderResponse.builder()
                .orderId(orderId)
                .orderCode("ORD-20261120-TEST")
                .customerName("Trần Văn B")
                .customerPhone("0912345678")
                .shippingAddress("456 Đường Thủy Sinh, Hà Nội")
                .totalAmount(new BigDecimal("150000.00"))
                .status(OrderStatusEnum.PENDING)
                .orderItems(List.of(item))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(orderService.checkout(any(CheckoutRequest.class))).thenReturn(response);

        String jsonPayload = """
                {
                    "cartId": "%s",
                    "customerName": "Trần Văn B",
                    "customerPhone": "0912345678",
                    "shippingAddress": "456 Đường Thủy Sinh, Hà Nội"
                }
                """.formatted(cartId);

        mockMvc.perform(post("/api/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.orderCode").value("ORD-20261120-TEST"))
                .andExpect(jsonPath("$.totalAmount").value(150000.00))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /api/orders/checkout - Trả về 409 Conflict khi kho không đủ hàng")
    void checkout_whenInsufficientStock_shouldReturn409() throws Exception {
        when(orderService.checkout(any(CheckoutRequest.class)))
                .thenThrow(new InsufficientStockException("Tồn kho cá không đủ để đáp ứng"));

        String jsonPayload = """
                {
                    "cartId": "%s",
                    "customerName": "Khách",
                    "customerPhone": "0900000000",
                    "shippingAddress": "HCM"
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isConflict())
                .andExpect(result -> org.assertj.core.api.Assertions.assertThat(
                        result.getResponse().getContentAsString()).isNotEmpty());
    }

    @Test
    @DisplayName("GET /api/orders/{orderId} - Trả về 200 khi đơn hàng tồn tại")
    void getOrderById_whenExists_shouldReturn200() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderResponse response = OrderResponse.builder()
                .orderId(orderId)
                .orderCode("ORD-20261120-001")
                .customerName("Khách VIP")
                .totalAmount(new BigDecimal("500000.00"))
                .status(OrderStatusEnum.CONFIRMED)
                .build();

        when(orderService.getOrderById(orderId)).thenReturn(response);

        mockMvc.perform(get("/api/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.orderCode").value("ORD-20261120-001"));
    }

    @Test
    @DisplayName("GET /api/orders/code/{orderCode} - Trả về 200 khi tra cứu mã đơn thành công")
    void getOrderByCode_whenExists_shouldReturn200() throws Exception {
        OrderResponse response = OrderResponse.builder()
                .orderId(UUID.randomUUID())
                .orderCode("ORD-20261120-CODE123")
                .status(OrderStatusEnum.SHIPPING)
                .build();

        when(orderService.getOrderByCode("ORD-20261120-CODE123")).thenReturn(response);

        mockMvc.perform(get("/api/orders/code/{orderCode}", "ORD-20261120-CODE123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderCode").value("ORD-20261120-CODE123"))
                .andExpect(jsonPath("$.status").value("SHIPPING"));
    }

    @Test
    @DisplayName("PATCH /api/orders/{orderId}/status - Cập nhật trạng thái đơn thành công")
    void updateOrderStatus_whenValid_shouldReturn200() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderResponse response = OrderResponse.builder()
                .orderId(orderId)
                .status(OrderStatusEnum.COMPLETED)
                .build();

        when(orderService.updateOrderStatus(orderId, OrderStatusEnum.COMPLETED)).thenReturn(response);

        mockMvc.perform(patch("/api/orders/{orderId}/status", orderId)
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("POST /api/orders/{orderId}/cancel - Hủy đơn hàng thành công")
    void cancelOrder_whenValid_shouldReturn200() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderResponse response = OrderResponse.builder()
                .orderId(orderId)
                .status(OrderStatusEnum.CANCELLED)
                .build();

        when(orderService.updateOrderStatus(orderId, OrderStatusEnum.CANCELLED)).thenReturn(response);

        mockMvc.perform(post("/api/orders/{orderId}/cancel", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
