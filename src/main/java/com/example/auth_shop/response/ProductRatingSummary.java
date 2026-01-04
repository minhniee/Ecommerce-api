package com.example.auth_shop.response;

import java.util.Map;

import lombok.Builder;
import lombok.Data;

// src/main/java/com/example/auth_shop/response/ProductRatingSummary.java
@Data
@Builder
public class ProductRatingSummary {
    private Long productId;
    private Double averageRating;
    private Long totalReviews;
    private Map<Integer, Long> ratingDistribution; // {5: 100, 4: 50, 3: 20, 2: 5, 1: 2}
}