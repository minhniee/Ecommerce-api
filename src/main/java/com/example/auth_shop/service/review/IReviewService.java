package com.example.auth_shop.service.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.auth_shop.dto.ReviewDto;
import com.example.auth_shop.model.Review;
import com.example.auth_shop.request.CreateReviewRequest;
import com.example.auth_shop.request.UpdateReviewRequest;
import com.example.auth_shop.response.ProductRatingSummary;

// src/main/java/com/example/auth_shop/service/review/IReviewService.java
public interface IReviewService {
    Review createReview(CreateReviewRequest request);
    Review updateReview(Long reviewId, UpdateReviewRequest request);
    void deleteReview(Long reviewId);
    ReviewDto getReviewById(Long reviewId);
    Page<ReviewDto> getProductReviews(Long productId, Pageable pageable);
    Page<ReviewDto> getUserReviews(Long userId, Pageable pageable);
    ProductRatingSummary getProductRatingSummary(Long productId);
    boolean hasUserReviewedProduct(Long productId);
    ReviewDto convertToDto(Review review);
}