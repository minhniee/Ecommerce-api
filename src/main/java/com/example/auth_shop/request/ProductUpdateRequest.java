package com.example.auth_shop.request;

import com.example.auth_shop.model.Category;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductUpdateRequest {
    private Long id;
    
    @NotBlank(message = "Product name is required")
    @Size(min = 1, max = 255, message = "Product name must be between 1 and 255 characters")
    private String name;
    
    @NotBlank(message = "Brand is required")
    @Size(min = 1, max = 100, message = "Brand must be between 1 and 100 characters")
    private String brand;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 integer digits and 2 decimal places")
    private BigDecimal price;
    
    @Min(value = 0, message = "Inventory must be 0 or greater")
    @Max(value = 999999, message = "Inventory must be less than 1000000")
    private int inventory;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @NotNull(message = "Category is required")
    @Valid
    private Category category;
}
