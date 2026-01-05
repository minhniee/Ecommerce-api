package com.example.auth_shop.controller;

import com.example.auth_shop.config.PaginationConfig;
import com.example.auth_shop.dto.OrderDto;
import com.example.auth_shop.dto.OrderStatusHistoryDto;
import com.example.auth_shop.model.Order;
import com.example.auth_shop.request.UpdateOrderStatusRequest;
import com.example.auth_shop.response.APIResponse;
import com.example.auth_shop.response.PaginatedResponse;
import com.example.auth_shop.service.order.IOrderService;
import com.example.auth_shop.util.PaginationUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/orders")
public class OrderController {
    private final IOrderService orderService;
    private final PaginationConfig paginationConfig;

    @PostMapping
    public ResponseEntity<APIResponse> createOrder(@RequestParam Long userId) {
        Order order = orderService.placeOrder(userId);
        OrderDto orderDto = orderService.convertToDto(order);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.created("Order placed successfully", orderDto));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<APIResponse> getOrderById(@PathVariable Long orderId) {
        OrderDto order = orderService.getOrder(orderId);
        return ResponseEntity.ok(APIResponse.success("Order retrieved successfully", order));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<APIResponse> getUserOrders(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "orderDate") String sortBy,
            @RequestParam(required = false, defaultValue = "DESC") String sortDir) {
        
        Pageable pageable = PaginationUtils.createPageable(
                page, size, paginationConfig.getMaxPageSize(), sortBy, sortDir);
        
        Page<OrderDto> ordersPage = orderService.getUserOrders(userId, pageable);
        PaginatedResponse<OrderDto> paginatedResponse = PaginatedResponse.of(ordersPage);
        
        return ResponseEntity.ok(APIResponse.success("Orders retrieved successfully", paginatedResponse));
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<APIResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        OrderDto order = orderService.updateOrderStatus(
                orderId, 
                request.getStatus(), 
                request.getChangedBy(), 
                request.getNotes()
        );
        return ResponseEntity.ok(APIResponse.success("Order status updated successfully", order));
    }

    @GetMapping("/{orderId}/status-history")
    public ResponseEntity<APIResponse> getOrderStatusHistory(@PathVariable Long orderId) {
        List<OrderStatusHistoryDto> history = orderService.getOrderStatusHistory(orderId);
        return ResponseEntity.ok(APIResponse.success("Order status history retrieved successfully", history));
    }
}
