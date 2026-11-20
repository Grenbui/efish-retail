package com.retail.storeback.order.service;

import com.retail.storeback.category.model.Product;
import com.retail.storeback.category.repository.ProductRepository;
import com.retail.storeback.order.model.Cart;
import com.retail.storeback.order.model.CartItem;
import com.retail.storeback.order.model.dto.CartItemRequest;
import com.retail.storeback.order.model.dto.CartResponse;
import com.retail.storeback.order.model.mapper.CartMapper;
import com.retail.storeback.order.model.mapper.CartMapperImpl;
import com.retail.storeback.order.repository.CartRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    private final CartMapper cartMapper = new CartMapperImpl();

    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartService(cartRepository, productRepository, cartMapper);
    }

    @Test
    @DisplayName("Lấy giỏ hàng theo ID thành công")
    void getCartById_whenExists_shouldReturnCartResponse() {
        UUID cartId = UUID.randomUUID();
        Cart cart = Cart.builder()
                .id(cartId)
                .cartItems(new ArrayList<>())
                .build();

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));

        CartResponse response = cartService.getCartById(cartId);

        assertThat(response).isNotNull();
        assertThat(response.cartId()).isEqualTo(cartId);
        assertThat(response.items()).isEmpty();
    }

    @Test
    @DisplayName("Lấy giỏ hàng theo ID không tồn tại ném EntityNotFoundException")
    void getCartById_whenNotFound_shouldThrowEntityNotFoundException() {
        UUID cartId = UUID.randomUUID();
        when(cartRepository.findById(cartId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.getCartById(cartId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(cartId.toString());
    }

    @Test
    @DisplayName("Thêm cá cảnh vào giỏ hàng thành công khi sản phẩm chưa có trong giỏ")
    void addItemToCart_whenNewItem_shouldAddToCart() {
        UUID cartId = UUID.randomUUID();
        Cart cart = Cart.builder().id(cartId).cartItems(new ArrayList<>()).build();

        UUID productId = UUID.randomUUID();
        Product product = Product.builder()
                .id(productId)
                .name("Cá Bảy Màu Koi")
                .sellingPrice(new BigDecimal("40000.00"))
                .active(true)
                .build();

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));

        CartItemRequest request = CartItemRequest.builder()
                .productId(productId)
                .quantity(2)
                .build();

        CartResponse response = cartService.addItemToCart(cartId, request);

        assertThat(response).isNotNull();
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).productName()).isEqualTo("Cá Bảy Màu Koi");
        assertThat(response.items().get(0).quantity()).isEqualTo(2);
        assertThat(response.totalAmount()).isEqualByComparingTo(new BigDecimal("80000.00"));
        assertThat(response.totalQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("Thêm cá cảnh đã có trong giỏ hàng thì cộng dồn số lượng")
    void addItemToCart_whenExistingItem_shouldIncrementQuantity() {
        UUID cartId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        CartItem existingItem = CartItem.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .productName("Cá Neon")
                .productPrice(new BigDecimal("10000.00"))
                .quantity(3)
                .build();

        ArrayList<CartItem> items = new ArrayList<>();
        items.add(existingItem);
        Cart cart = Cart.builder().id(cartId).cartItems(items).build();
        existingItem.setCart(cart);

        Product product = Product.builder()
                .id(productId)
                .name("Cá Neon")
                .sellingPrice(new BigDecimal("10000.00"))
                .active(true)
                .build();

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));

        CartItemRequest request = CartItemRequest.builder()
                .productId(productId)
                .quantity(5)
                .build();

        CartResponse response = cartService.addItemToCart(cartId, request);

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).quantity()).isEqualTo(8);
        assertThat(response.totalAmount()).isEqualByComparingTo(new BigDecimal("80000.00"));
    }

    @Test
    @DisplayName("Thêm vào giỏ hàng thất bại khi số lượng <= 0")
    void addItemToCart_whenQuantityInvalid_shouldThrowIllegalArgumentException() {
        UUID cartId = UUID.randomUUID();
        CartItemRequest request = CartItemRequest.builder()
                .productId(UUID.randomUUID())
                .quantity(0)
                .build();

        assertThatThrownBy(() -> cartService.addItemToCart(cartId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("greater than zero");
    }

    @Test
    @DisplayName("Thêm vào giỏ hàng thất bại khi sản phẩm ngừng bán (active = false)")
    void addItemToCart_whenProductInactive_shouldThrowIllegalArgumentException() {
        UUID cartId = UUID.randomUUID();
        Cart cart = Cart.builder().id(cartId).cartItems(new ArrayList<>()).build();

        UUID productId = UUID.randomUUID();
        Product product = Product.builder()
                .id(productId)
                .name("Cá Ngừng Bán")
                .sellingPrice(BigDecimal.TEN)
                .active(false)
                .build();

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        CartItemRequest request = CartItemRequest.builder()
                .productId(productId)
                .quantity(1)
                .build();

        assertThatThrownBy(() -> cartService.addItemToCart(cartId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not active for sale");
    }

    @Test
    @DisplayName("Cập nhật số lượng sản phẩm trong giỏ thành công")
    void updateItemQuantity_whenQuantityPositive_shouldUpdateQuantity() {
        UUID cartId = UUID.randomUUID();
        UUID cartItemId = UUID.randomUUID();

        CartItem item = CartItem.builder()
                .id(cartItemId)
                .productId(UUID.randomUUID())
                .productName("Cá Guppy")
                .productPrice(new BigDecimal("50000.00"))
                .quantity(2)
                .build();

        ArrayList<CartItem> items = new ArrayList<>();
        items.add(item);
        Cart cart = Cart.builder().id(cartId).cartItems(items).build();

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));

        CartResponse response = cartService.updateItemQuantity(cartId, cartItemId, 5);

        assertThat(response.items().get(0).quantity()).isEqualTo(5);
        assertThat(response.totalAmount()).isEqualByComparingTo(new BigDecimal("250000.00"));
    }

    @Test
    @DisplayName("Cập nhật số lượng về 0 thì tự động xóa mục khỏi giỏ hàng")
    void updateItemQuantity_whenQuantityZero_shouldRemoveItem() {
        UUID cartId = UUID.randomUUID();
        UUID cartItemId = UUID.randomUUID();

        CartItem item = CartItem.builder()
                .id(cartItemId)
                .productId(UUID.randomUUID())
                .productName("Cá Guppy")
                .productPrice(BigDecimal.TEN)
                .quantity(2)
                .build();

        ArrayList<CartItem> items = new ArrayList<>();
        items.add(item);
        Cart cart = Cart.builder().id(cartId).cartItems(items).build();

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));

        CartResponse response = cartService.updateItemQuantity(cartId, cartItemId, 0);

        assertThat(response.items()).isEmpty();
        assertThat(response.totalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Xóa một mục khỏi giỏ hàng thành công")
    void removeItemFromCart_whenItemExists_shouldRemove() {
        UUID cartId = UUID.randomUUID();
        UUID cartItemId = UUID.randomUUID();

        CartItem item = CartItem.builder()
                .id(cartItemId)
                .productId(UUID.randomUUID())
                .productName("Cá")
                .productPrice(BigDecimal.TEN)
                .quantity(1)
                .build();

        ArrayList<CartItem> items = new ArrayList<>();
        items.add(item);
        Cart cart = Cart.builder().id(cartId).cartItems(items).build();

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));

        CartResponse response = cartService.removeItemFromCart(cartId, cartItemId);

        assertThat(response.items()).isEmpty();
    }

    @Test
    @DisplayName("Xóa trắng toàn bộ giỏ hàng thành công")
    void clearCart_whenCalled_shouldClearAllItems() {
        UUID cartId = UUID.randomUUID();

        CartItem item1 = CartItem.builder().id(UUID.randomUUID()).productId(UUID.randomUUID()).productPrice(BigDecimal.TEN).quantity(1).build();
        CartItem item2 = CartItem.builder().id(UUID.randomUUID()).productId(UUID.randomUUID()).productPrice(BigDecimal.TEN).quantity(2).build();

        ArrayList<CartItem> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);
        Cart cart = Cart.builder().id(cartId).cartItems(items).build();

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));

        CartResponse response = cartService.clearCart(cartId);

        assertThat(response.items()).isEmpty();
        assertThat(response.totalQuantity()).isEqualTo(0);
    }
}
