package com.example.auth_shop.repository;

import com.example.auth_shop.model.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {
    List<OrderStatusHistory> findByOrderOrderIdOrderByChangedAtAsc(Long orderId);
    List<OrderStatusHistory> findByOrderOrderIdOrderByChangedAtDesc(Long orderId);
}

