package com.example.auth_shop.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// src/main/java/com/example/auth_shop/dto/ReviewDto.java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDto {
    private Long id;
    private String firstName;
    private Long userId;
    private String lastName; // firstName + lastName
    private Long productId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}

