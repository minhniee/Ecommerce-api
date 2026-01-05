package com.example.auth_shop.service.order;

import com.example.auth_shop.dto.OrderDto;
import com.example.auth_shop.dto.OrderStatusHistoryDto;
import com.example.auth_shop.enums.OrderStatus;
import com.example.auth_shop.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IOrderService {
    Order placeOrder(Long userId);
    OrderDto getOrder(Long orderId);
    List<OrderDto> getUserOrders(Long userId);
    Page<OrderDto> getUserOrders(Long userId, Pageable pageable);
    OrderDto updateOrderStatus(Long orderId, OrderStatus newStatus, String changedBy, String notes);
    List<OrderStatusHistoryDto> getOrderStatusHistory(Long orderId);
    OrderDto convertToDto(Order order);
}
