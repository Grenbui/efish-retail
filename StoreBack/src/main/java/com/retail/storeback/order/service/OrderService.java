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
import com.retail.storeback.order.repository.CartRepository;
import com.retail.storeback.order.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponse checkout(CheckoutRequest request) {
        if (request.cartId() == null) {
            throw new IllegalArgumentException("Cart ID is required for checkout");
        }

        Cart cart = cartRepository.findById(request.cartId())
                .orElseThrow(() -> new EntityNotFoundException("Cart not found with id: " + request.cartId()));

        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot checkout with an empty cart");
        }

        String orderCode = generateOrderCode();

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        Order order = Order.builder()
                .orderCode(orderCode)
                .customerId(request.customerId() != null ? request.customerId() : cart.getCustomerId())
                .customerName(request.customerName())
                .customerPhone(request.customerPhone())
                .shippingAddress(request.shippingAddress())
                .note(request.note())
                .status(OrderStatusEnum.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .orderItems(new ArrayList<>())
                .build();

        for (CartItem cartItem : cart.getCartItems()) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + cartItem.getProductId()));

            if (!product.isActive()) {
                throw new IllegalArgumentException("Product is no longer active for sale: " + product.getName());
            }

            // Deduct stock from Inventory Lite, returns current avgCost snapshot
            BigDecimal unitCostSnap = inventoryService.deductStockForOrder(cartItem.getProductId(), cartItem.getQuantity());

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .productId(cartItem.getProductId())
                    .productNameSnap(cartItem.getProductName())
                    .qty(cartItem.getQuantity())
                    .unitPrice(cartItem.getProductPrice())
                    .unitCostSnap(unitCostSnap != null ? unitCostSnap : BigDecimal.ZERO)
                    .build();

            orderItems.add(orderItem);

            BigDecimal lineTotal = cartItem.getProductPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(lineTotal);
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        // Clear cart after successful checkout
        cart.getCartItems().clear();
        cartRepository.save(cart);

        return orderMapper.toOrderResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));
        return orderMapper.toOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderByCode(String orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with code: " + orderCode));
        return orderMapper.toOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByCustomer(UUID customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    @Transactional
    public OrderResponse updateOrderStatus(UUID orderId, OrderStatusEnum newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() == OrderStatusEnum.CANCELLED) {
            throw new IllegalArgumentException("Cannot change status of an already cancelled order");
        }

        if (newStatus == OrderStatusEnum.CANCELLED) {
            // Restore inventory when order is cancelled
            for (OrderItem item : order.getOrderItems()) {
                inventoryService.returnStockForCancel(item.getProductId(), item.getQty(), order.getId());
            }
        }

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);
        return orderMapper.toOrderResponse(updatedOrder);
    }

    private String generateOrderCode() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomHex = Integer.toHexString(ThreadLocalRandom.current().nextInt(0x1000, 0x10000)).toUpperCase();
        return "ORD-" + datePart + "-" + randomHex;
    }
}
