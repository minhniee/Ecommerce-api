package com.example.auth_shop.service.review;

import java.util.HashMap;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.auth_shop.dto.ReviewDto;
import com.example.auth_shop.exceptions.AlreadyExistsException;
import com.example.auth_shop.exceptions.NotOwnerException;
import com.example.auth_shop.exceptions.ResourceNotFoundException;
import com.example.auth_shop.model.Product;
import com.example.auth_shop.model.Review;
import com.example.auth_shop.model.User;
import com.example.auth_shop.repository.ProductRepository;
import com.example.auth_shop.repository.ReviewRepository;
import com.example.auth_shop.request.CreateReviewRequest;
import com.example.auth_shop.request.UpdateReviewRequest;
import com.example.auth_shop.response.ProductRatingSummary;
import com.example.auth_shop.service.user.UserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService implements IReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;
    
    @Transactional
    @Override
    public Review createReview(CreateReviewRequest request) {
        User user = userService.getAuthenticatedUser();
    

            Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    
        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
    
        try {
            return reviewRepository.save(review);
        } catch (DataIntegrityViolationException ex) {
            throw new AlreadyExistsException("You have already reviewed this product");
        }
    }
    
    @Transactional
    @Override
    public Review updateReview(Long reviewId, UpdateReviewRequest request) {
        User user = userService.getAuthenticatedUser();
        
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        if(!review.getUser().getId().equals(user.getId())) {
            throw new NotOwnerException("You are not the owner of this review");
        }
        review.setRating(request.getRating());
        review.setComment(request.getComment());
            return reviewRepository.save(review);

    }

    @Transactional
    @Override
    public void deleteReview(Long reviewId) {
        User user = userService.getAuthenticatedUser();

        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        if(!review.getUser().getId().equals(user.getId())) {
            throw new NotOwnerException("You are not the owner of this review");
        }
            reviewRepository.delete(review);
    }

    @Override
    public ReviewDto getReviewById(Long reviewId) {
        // TODO Auto-generated method stub
        // User user = userService.getAuthenticatedUser();

        throw new UnsupportedOperationException("Unimplemented method 'getReviewById'");
    }

    @Override
    public Page<ReviewDto> getProductReviews(Long productId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByProductId(productId, pageable);

        return reviews.map(this::convertToDto);
    }

    @Override
    public Page<ReviewDto> getUserReviews(Long userId, Pageable pageable) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUserReviews'");
    }

    @Override
    public ProductRatingSummary getProductRatingSummary(Long productId) {
        Double avgRating = reviewRepository.getAverageRatingByProductId(productId);
        Long totalReviews = reviewRepository.countByProductId(productId);
        
        // Build rating distribution map
        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) distribution.put(i, 0L);
        
        reviewRepository.getRatingDistributionByProductId(productId)
            .forEach(row -> distribution.put((Integer) row[0], (Long) row[1]));
        
        return ProductRatingSummary.builder()
            .productId(productId)
            .averageRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0)
            .totalReviews(totalReviews)
            .ratingDistribution(distribution)
            .build();
    }

    @Override
    public boolean hasUserReviewedProduct(Long productId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hasUserReviewedProduct'");
    }
    @Override
    public ReviewDto convertToDto(Review review) {
        return modelMapper.map(review, ReviewDto.class);
    }
    
}
