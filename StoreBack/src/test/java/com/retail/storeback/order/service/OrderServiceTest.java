package com.retail.storeback.order.service;

import com.retail.storeback.category.model.Product;
import com.retail.storeback.category.repository.ProductRepository;
import com.retail.storeback.inventory.service.InventoryService;
import com.retail.storeback.order.model.Cart;
import com.retail.storeback.order.model.CartItem;
import com.retail.storeback.order.model.Order;
import com.retail.storeback.order.model.OrderItem;
import com.retail.storeback.order.model.OrderStatusEnum;
import com.retail.storeback.order.model.dto.CheckoutRequest;
import com.retail.storeback.order.model.dto.OrderResponse;
import com.retail.storeback.order.model.mapper.OrderMapper;
import com.retail.storeback.order.model.mapper.OrderMapperImpl;
import com.retail.storeback.order.repository.CartRepository;
import com.retail.storeback.order.repository.OrderRepository;
import com.retail.storeback.utils.handleExceptions.InsufficientStockException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryService inventoryService;

    private final OrderMapper orderMapper = new OrderMapperImpl();

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, cartRepository, productRepository, inventoryService, orderMapper);
    }

    @Test
    @DisplayName("Checkout thành công: Trừ kho, tạo Order + OrderItem kèm snapshot unitCost, xóa giỏ hàng")
    void checkout_whenValid_shouldDeductStockCreateOrderAndClearCart() {
        UUID cartId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        CartItem cartItem = CartItem.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .productName("Cá Neon Vua")
                .productPrice(new BigDecimal("20000.00"))
                .quantity(3)
                .build();

        ArrayList<CartItem> cartItems = new ArrayList<>();
        cartItems.add(cartItem);

        Cart cart = Cart.builder()
                .id(cartId)
                .customerId(customerId)
                .cartItems(cartItems)
                .build();

        Product product = Product.builder()
                .id(productId)
                .name("Cá Neon Vua")
                .sellingPrice(new BigDecimal("20000.00"))
                .active(true)
                .build();

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(inventoryService.deductStockForOrder(productId, 3)).thenReturn(new BigDecimal("12000.00")); // Giá vốn snapshot
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(UUID.randomUUID());
            return o;
        });

        CheckoutRequest request = CheckoutRequest.builder()
                .cartId(cartId)
                .customerId(customerId)
                .customerName("Nguyễn Văn A")
                .customerPhone("0987654321")
                .shippingAddress("123 Đường Cá Cảnh, Quận 1")
                .note("Giao giờ hành chính")
                .build();

        OrderResponse response = orderService.checkout(request);

        assertThat(response).isNotNull();
        assertThat(response.orderCode()).startsWith("ORD-");
        assertThat(response.customerName()).isEqualTo("Nguyễn Văn A");
        assertThat(response.status()).isEqualTo(OrderStatusEnum.PENDING);
        assertThat(response.totalAmount()).isEqualByComparingTo(new BigDecimal("60000.00"));
        assertThat(response.orderItems()).hasSize(1);
        assertThat(response.orderItems().get(0).unitCostSnap()).isEqualByComparingTo(new BigDecimal("12000.00"));
        assertThat(response.orderItems().get(0).qty()).isEqualTo(3);

        // Giỏ hàng phải được xóa sạch sau khi checkout
        assertThat(cart.getCartItems()).isEmpty();
        verify(cartRepository, times(1)).save(cart);
        verify(inventoryService, times(1)).deductStockForOrder(productId, 3);
    }

    @Test
    @DisplayName("Checkout thất bại khi không tìm thấy giỏ hàng")
    void checkout_whenCartNotFound_shouldThrowEntityNotFoundException() {
        UUID cartId = UUID.randomUUID();
        when(cartRepository.findById(cartId)).thenReturn(Optional.empty());

        CheckoutRequest request = CheckoutRequest.builder().cartId(cartId).build();

        assertThatThrownBy(() -> orderService.checkout(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(cartId.toString());
    }

    @Test
    @DisplayName("Checkout thất bại khi giỏ hàng trống")
    void checkout_whenCartIsEmpty_shouldThrowIllegalArgumentException() {
        UUID cartId = UUID.randomUUID();
        Cart cart = Cart.builder().id(cartId).cartItems(new ArrayList<>()).build();
        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));

        CheckoutRequest request = CheckoutRequest.builder().cartId(cartId).build();

        assertThatThrownBy(() -> orderService.checkout(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty cart");
    }

    @Test
    @DisplayName("Checkout thất bại khi sản phẩm cá cảnh ngừng bán")
    void checkout_whenProductInactive_shouldThrowIllegalArgumentException() {
        UUID cartId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        CartItem cartItem = CartItem.builder()
                .productId(productId)
                .productName("Cá Đã Hết Hàng")
                .productPrice(BigDecimal.TEN)
                .quantity(1)
                .build();

        ArrayList<CartItem> cartItems = new ArrayList<>();
        cartItems.add(cartItem);
        Cart cart = Cart.builder().id(cartId).cartItems(cartItems).build();

        Product product = Product.builder().id(productId).active(false).build();

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        CheckoutRequest request = CheckoutRequest.builder().cartId(cartId).build();

        assertThatThrownBy(() -> orderService.checkout(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no longer active");
    }

    @Test
    @DisplayName("Checkout thất bại và ném InsufficientStockException khi tồn kho không đủ")
    void checkout_whenInsufficientStock_shouldThrowInsufficientStockException() {
        UUID cartId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        CartItem cartItem = CartItem.builder()
                .productId(productId)
                .productName("Cá Rồng")
                .productPrice(new BigDecimal("5000000.00"))
                .quantity(10)
                .build();

        ArrayList<CartItem> cartItems = new ArrayList<>();
        cartItems.add(cartItem);
        Cart cart = Cart.builder().id(cartId).cartItems(cartItems).build();

        Product product = Product.builder().id(productId).active(true).build();

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(inventoryService.deductStockForOrder(productId, 10))
                .thenThrow(new InsufficientStockException("Tồn kho không đủ cho sản phẩm: " + productId));

        CheckoutRequest request = CheckoutRequest.builder().cartId(cartId).build();

        assertThatThrownBy(() -> orderService.checkout(request))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Tồn kho không đủ");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Lấy đơn hàng theo ID thành công")
    void getOrderById_whenExists_shouldReturnResponse() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder()
                .id(orderId)
                .orderCode("ORD-20261120-001")
                .customerName("Khách Mua Cá")
                .totalAmount(new BigDecimal("100000.00"))
                .status(OrderStatusEnum.PENDING)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderById(orderId);

        assertThat(response).isNotNull();
        assertThat(response.orderId()).isEqualTo(orderId);
        assertThat(response.orderCode()).isEqualTo("ORD-20261120-001");
    }

    @Test
    @DisplayName("Cập nhật trạng thái đơn sang CANCELLED thì hoàn tồn kho cho Inventory")
    void updateOrderStatus_whenCancelled_shouldReturnStockToInventory() {
        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        OrderItem item = OrderItem.builder()
                .productId(productId)
                .qty(5)
                .unitPrice(BigDecimal.TEN)
                .unitCostSnap(BigDecimal.ONE)
                .build();

        Order order = Order.builder()
                .id(orderId)
                .orderCode("ORD-CANCEL")
                .status(OrderStatusEnum.CONFIRMED)
                .orderItems(List.of(item))
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        OrderResponse response = orderService.updateOrderStatus(orderId, OrderStatusEnum.CANCELLED);

        assertThat(response.status()).isEqualTo(OrderStatusEnum.CANCELLED);
        verify(inventoryService, times(1)).returnStockForCancel(productId, 5, orderId);
    }

    @Test
    @DisplayName("Cập nhật trạng thái đơn đã hủy ném IllegalArgumentException")
    void updateOrderStatus_whenAlreadyCancelled_shouldThrowIllegalArgumentException() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder()
                .id(orderId)
                .status(OrderStatusEnum.CANCELLED)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateOrderStatus(orderId, OrderStatusEnum.CONFIRMED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already cancelled");

        verify(orderRepository, never()).save(any(Order.class));
    }
}
