package com.example.auth_shop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.auth_shop.model.Review;

// src/main/java/com/example/auth_shop/repository/ReviewRepository.java
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    // Tìm reviews theo product với pagination
    Page<Review> findByProductId(Long productId, Pageable pageable);
    
    // Tìm reviews theo user
    Page<Review> findByUserId(Long userId, Pageable pageable);
    
    // Kiểm tra user đã review product chưa
    boolean existsByUserIdAndProductId(Long userId, Long productId);
    
    // Tìm review của user cho product cụ thể
    Optional<Review> findByUserIdAndProductId(Long userId, Long productId);
    
    // Tính rating trung bình
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double getAverageRatingByProductId(@Param("productId") Long productId);
    
    // Đếm số reviews
    Long countByProductId(Long productId);
    
    // Thống kê rating distribution
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.product.id = :productId GROUP BY r.rating")
    List<Object[]> getRatingDistributionByProductId(@Param("productId") Long productId);
}