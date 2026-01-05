package com.example.auth_shop.service;

import com.example.auth_shop.exceptions.AccountLockedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;

/**
 * AccountLockoutService - Dịch vụ quản lý account lockout
 * 
 * Implement account lockout mechanism:
 * - Track failed login attempts
 * - Lock account sau N lần failed attempts
 * - Auto unlock sau một khoảng thời gian
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountLockoutService {
    
    private static final String FAILED_ATTEMPTS_PREFIX = "failed_attempts:";
    private static final String ACCOUNT_LOCKED_PREFIX = "account_locked:";
    
    // Configuration
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 15; // 15 minutes
    
    private final RedisTemplate<String, String> redisTemplate;
    
    /**
     * Ghi nhận failed login attempt
     */
    public void recordFailedAttempt(String email) {
        String failedAttemptsKey = FAILED_ATTEMPTS_PREFIX + email;
        
        try {
            String currentAttemptsStr = redisTemplate.opsForValue().get(failedAttemptsKey);
            int currentAttempts = (currentAttemptsStr != null) ? Integer.parseInt(currentAttemptsStr) : 0;
            int newAttempts = currentAttempts + 1;
            
            // Set với TTL 1 giờ (reset sau 1 giờ không có failed attempts)
            redisTemplate.opsForValue().set(failedAttemptsKey, String.valueOf(newAttempts), 
                60, TimeUnit.MINUTES);
            
            log.warn("Failed login attempt for email: {} - Attempt {}/{}", 
                maskEmail(email), newAttempts, MAX_FAILED_ATTEMPTS);
            
            // Nếu vượt quá max attempts, lock account
            if (newAttempts >= MAX_FAILED_ATTEMPTS) {
                lockAccount(email);
            }
            
        } catch (Exception e) {
            log.error("Error recording failed attempt for email: {} - {}", 
                maskEmail(email), e.getMessage());
        }
    }
    
    /**
     * Reset failed attempts sau khi login thành công
     */
    public void resetFailedAttempts(String email) {
        String failedAttemptsKey = FAILED_ATTEMPTS_PREFIX + email;
        try {
            redisTemplate.delete(failedAttemptsKey);
            log.debug("Failed attempts reset for email: {}", maskEmail(email));
        } catch (Exception e) {
            log.error("Error resetting failed attempts for email: {} - {}", 
                maskEmail(email), e.getMessage());
        }
    }
    
    /**
     * Lock account
     */
    private void lockAccount(String email) {
        String lockKey = ACCOUNT_LOCKED_PREFIX + email;
        long unlockTimestamp = System.currentTimeMillis() + (LOCKOUT_DURATION_MINUTES * 60 * 1000L);
        
        try {
            redisTemplate.opsForValue().set(lockKey, String.valueOf(unlockTimestamp), 
                LOCKOUT_DURATION_MINUTES, TimeUnit.MINUTES);
            
            log.warn("Account locked for email: {} - Will unlock at {}", 
                maskEmail(email), LocalDateTime.ofEpochSecond(unlockTimestamp / 1000, 0, ZoneOffset.UTC));
            
        } catch (Exception e) {
            log.error("Error locking account for email: {} - {}", maskEmail(email), e.getMessage());
        }
    }
    
    /**
     * Kiểm tra account có bị lock trong Redis không (temporary lockout)
     * 
     * @param email Email của user
     * @return true nếu account bị lock trong Redis, false nếu không
     */
    public boolean isAccountLockedInRedis(String email) {
        String lockKey = ACCOUNT_LOCKED_PREFIX + email;
        
        try {
            String unlockTimestampStr = redisTemplate.opsForValue().get(lockKey);
            
            if (unlockTimestampStr != null) {
                long unlockTimestamp = Long.parseLong(unlockTimestampStr);
                long currentTime = System.currentTimeMillis();
                
                if (currentTime < unlockTimestamp) {
                    return true; // Vẫn còn bị lock
                } else {
                    // Lock đã hết hạn, xóa lock
                    redisTemplate.delete(lockKey);
                    return false;
                }
            }
            
            return false;
            
        } catch (Exception e) {
            log.error("Error checking Redis lock for email: {} - {}", 
                maskEmail(email), e.getMessage());
            // Nếu có lỗi, return false để tránh block users
            return false;
        }
    }
    
    /**
     * Kiểm tra account có bị lock không (Redis - temporary lockout)
     * 
     * @throws AccountLockedException nếu account bị lock trong Redis
     */
    public void checkAccountLocked(String email) {
        String lockKey = ACCOUNT_LOCKED_PREFIX + email;
        
        try {
            String unlockTimestampStr = redisTemplate.opsForValue().get(lockKey);
            
            if (unlockTimestampStr != null) {
                long unlockTimestamp = Long.parseLong(unlockTimestampStr);
                long currentTime = System.currentTimeMillis();
                
                if (currentTime < unlockTimestamp) {
                    long remainingMinutes = (unlockTimestamp - currentTime) / (60 * 1000);
                    throw new AccountLockedException(
                        String.format("Account is locked due to too many failed attempts. Please try again in %d minutes", remainingMinutes),
                        unlockTimestamp
                    );
                } else {
                    // Lock đã hết hạn, xóa lock
                    redisTemplate.delete(lockKey);
                }
            }
            
        } catch (AccountLockedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error checking account lock for email: {} - {}", 
                maskEmail(email), e.getMessage());
            // Nếu có lỗi, không lock account để tránh block users
        }
    }
    
    /**
     * Unlock account manually (dùng cho admin)
     */
    public void unlockAccount(String email) {
        String lockKey = ACCOUNT_LOCKED_PREFIX + email;
        String failedAttemptsKey = FAILED_ATTEMPTS_PREFIX + email;
        
        try {
            redisTemplate.delete(lockKey);
            redisTemplate.delete(failedAttemptsKey);
            log.info("Account manually unlocked for email: {}", maskEmail(email));
        } catch (Exception e) {
            log.error("Error unlocking account for email: {} - {}", 
                maskEmail(email), e.getMessage());
        }
    }
    
    /**
     * Lấy số lần failed attempts còn lại
     */
    public int getRemainingAttempts(String email) {
        String failedAttemptsKey = FAILED_ATTEMPTS_PREFIX + email;
        try {
            String attemptsStr = redisTemplate.opsForValue().get(failedAttemptsKey);
            int attempts = (attemptsStr != null) ? Integer.parseInt(attemptsStr) : 0;
            return Math.max(0, MAX_FAILED_ATTEMPTS - attempts);
        } catch (Exception e) {
            log.error("Error getting remaining attempts for email: {} - {}", 
                maskEmail(email), e.getMessage());
            return MAX_FAILED_ATTEMPTS;
        }
    }
    
    /**
     * Mask email để log an toàn hơn
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        String[] parts = email.split("@");
        if (parts.length == 2) {
            String username = parts[0];
            String domain = parts[1];
            String maskedUsername = username.length() > 3 
                ? username.substring(0, 3) + "***" 
                : "***";
            return maskedUsername + "@" + domain;
        }
        return "***";
    }
}

