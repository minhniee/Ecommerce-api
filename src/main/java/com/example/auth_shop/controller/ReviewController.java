package com.example.auth_shop.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.auth_shop.config.PaginationConfig;
import com.example.auth_shop.dto.ReviewDto;
import com.example.auth_shop.model.Review;
import com.example.auth_shop.request.CreateReviewRequest;
import com.example.auth_shop.request.UpdateReviewRequest;
import com.example.auth_shop.response.APIResponse;
import com.example.auth_shop.response.ProductRatingSummary;
import com.example.auth_shop.service.review.IReviewService;
import com.example.auth_shop.util.PaginationUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// src/main/java/com/example/auth_shop/controller/ReviewController.java
@RestController
@RequestMapping("${api.prefix}/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final IReviewService reviewService;
    private final PaginationConfig paginationConfig;
    @PostMapping
    public ResponseEntity<APIResponse> createReview(@Valid @RequestBody CreateReviewRequest request) {
        Review review = reviewService.createReview(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(APIResponse.created("Review created successfully", reviewService.convertToDto(review)));
    }
    
    @GetMapping("/product/{productId}")
    public ResponseEntity<APIResponse> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        // pagination logic...
        Pageable pageable = PaginationUtils.createPageable(
                page, size, paginationConfig.getMaxPageSize(), sortBy, sortDir);
        Page<ReviewDto> reviewsPage = reviewService.getProductReviews(productId, pageable);
        return ResponseEntity.ok(APIResponse.success("Reviews retrieved successfully", reviewsPage));
    }
    
    @GetMapping("/product/{productId}/summary")
    public ResponseEntity<APIResponse> getProductRatingSummary(@PathVariable Long productId) {
        ProductRatingSummary summary = reviewService.getProductRatingSummary(productId);
        return ResponseEntity.ok(APIResponse.success("Rating summary retrieved", summary));
    }
    
    @PutMapping("/{reviewId}")
    public ResponseEntity<APIResponse> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody UpdateReviewRequest request) {
        Review review = reviewService.updateReview(reviewId, request);
        return ResponseEntity.ok(APIResponse.success("Review updated successfully", reviewService.convertToDto(review)));
    }
    
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<APIResponse> deleteReview(@PathVariable Long reviewId) {
        // Owner hoặc Admin được delete
        reviewService.deleteReview(reviewId);
        return ResponseEntity.ok(APIResponse.success("Review deleted successfully"));

    }
}