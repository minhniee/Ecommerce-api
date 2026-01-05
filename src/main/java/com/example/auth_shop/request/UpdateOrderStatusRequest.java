package com.example.auth_shop.request;

import com.example.auth_shop.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {
    @NotNull(message = "Order status is required")
    private OrderStatus status;
    
    @Size(max = 100, message = "Changed by must not exceed 100 characters")
    private String changedBy;
    
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}

