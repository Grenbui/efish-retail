package com.retail.storeback.order.model.mapper;

import com.retail.storeback.order.model.Cart;
import com.retail.storeback.order.model.CartItem;
import com.retail.storeback.order.model.dto.CartItemResponse;
import com.retail.storeback.order.model.dto.CartResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring", imports = {BigDecimal.class})
public interface CartMapper {

    @Mapping(source = "id", target = "cartItemId")
    @Mapping(target = "subTotal", expression = "java(item.getProductPrice().multiply(BigDecimal.valueOf(item.getQuantity())))")
    CartItemResponse toItemResponse(CartItem item);

    default CartResponse toCartResponse(Cart cart) {
        if (cart == null) {
            return null;
        }

        List<CartItemResponse> itemResponses = cart.getCartItems() != null
                ? cart.getCartItems().stream().map(this::toItemResponse).toList()
                : Collections.emptyList();

        BigDecimal totalAmount = itemResponses.stream()
                .map(CartItemResponse::subTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalQuantity = itemResponses.stream()
                .mapToInt(CartItemResponse::quantity)
                .sum();

        return CartResponse.builder()
                .cartId(cart.getId())
                .customerId(cart.getCustomerId())
                .sessionId(cart.getSessionId())
                .items(itemResponses)
                .totalAmount(totalAmount)
                .totalQuantity(totalQuantity)
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }
}
