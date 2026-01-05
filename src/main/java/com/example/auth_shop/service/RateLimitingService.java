package com.example.auth_shop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * RateLimitingService - Dịch vụ giới hạn số lượng requests
 * 
 * Sử dụng Redis để lưu trữ số lượng requests và thời gian
 * Implement token bucket algorithm đơn giản
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitingService {
    
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    private final RedisTemplate<String, String> redisTemplate;
    
    /**
     * Kiểm tra rate limit cho một key
     * 
     * @param key Key để identify rate limit (ví dụ: IP address, email)
     * @param maxRequests Số lượng requests tối đa
     * @param windowInSeconds Thời gian window (seconds)
     * @return true nếu vượt quá limit, false nếu còn trong limit
     */
    public boolean isRateLimitExceeded(String key, int maxRequests, int windowInSeconds) {
        String redisKey = RATE_LIMIT_PREFIX + key;
        
        try {
            String currentCountStr = redisTemplate.opsForValue().get(redisKey);
            int currentCount = (currentCountStr != null) ? Integer.parseInt(currentCountStr) : 0;
            
            if (currentCount >= maxRequests) {
                log.warn("Rate limit exceeded for key: {} ({} requests in {} seconds)", 
                    maskKey(key), currentCount, windowInSeconds);
                return true;
            }
            
            // Increment counter
            if (currentCount == 0) {
                // First request in window - set với TTL
                redisTemplate.opsForValue().set(redisKey, "1", windowInSeconds, TimeUnit.SECONDS);
            } else {
                // Increment existing counter (TTL không thay đổi)
                redisTemplate.opsForValue().increment(redisKey);
            }
            
            log.debug("Rate limit check for key: {} - {}/{} requests", 
                maskKey(key), currentCount + 1, maxRequests);
            return false;
            
        } catch (Exception e) {
            log.error("Error checking rate limit for key: {} - {}", maskKey(key), e.getMessage());
            // Nếu có lỗi, cho phép request tiếp tục để tránh block users
            return false;
        }
    }
    
    /**
     * Reset rate limit counter cho một key
     */
    public void resetRateLimit(String key) {
        String redisKey = RATE_LIMIT_PREFIX + key;
        try {
            redisTemplate.delete(redisKey);
            log.debug("Rate limit reset for key: {}", maskKey(key));
        } catch (Exception e) {
            log.error("Error resetting rate limit for key: {} - {}", maskKey(key), e.getMessage());
        }
    }
    
    /**
     * Mask key để log an toàn hơn
     */
    private String maskKey(String key) {
        if (key == null || key.length() <= 10) {
            return "***";
        }
        // Nếu là email, chỉ hiển thị phần trước @ và 2 ký tự đầu của domain
        if (key.contains("@")) {
            String[] parts = key.split("@");
            if (parts.length == 2) {
                return parts[0].substring(0, Math.min(3, parts[0].length())) + 
                       "***@" + 
                       parts[1].substring(0, Math.min(2, parts[1].length())) + 
                       "***";
            }
        }
        return key.substring(0, 4) + "***" + key.substring(Math.max(4, key.length() - 4));
    }
}

