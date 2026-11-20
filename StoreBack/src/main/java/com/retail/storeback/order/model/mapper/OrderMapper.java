package com.retail.storeback.order.model.mapper;

import com.retail.storeback.order.model.Order;
import com.retail.storeback.order.model.OrderItem;
import com.retail.storeback.order.model.dto.OrderItemResponse;
import com.retail.storeback.order.model.dto.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", imports = {BigDecimal.class})
public interface OrderMapper {

    @Mapping(source = "id", target = "orderItemId")
    @Mapping(target = "subTotal", expression = "java(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQty())))")
    OrderItemResponse toItemResponse(OrderItem item);

    @Mapping(source = "id", target = "orderId")
    OrderResponse toOrderResponse(Order order);
}
