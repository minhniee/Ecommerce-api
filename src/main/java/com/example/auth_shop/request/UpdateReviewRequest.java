package com.example.auth_shop.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;



// src/main/java/com/example/auth_shop/request/UpdateReviewRequest.java
@Data
public class UpdateReviewRequest {
    @Min(value = 1) @Max(value = 5)
    private Integer rating;
    
    @Size(max = 1000)
    private String comment;
}