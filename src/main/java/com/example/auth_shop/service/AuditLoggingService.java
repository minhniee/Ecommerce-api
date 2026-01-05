package com.example.auth_shop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * AuditLoggingService - Dịch vụ ghi log các security events
 * 
 * Log các sự kiện bảo mật quan trọng:
 * - Login success/failure
 * - Token revocation
 * - Account lockout
 * - Rate limit exceeded
 * - Password changes
 * - etc.
 * 
 * NOTE: Trong production, nên lưu vào database hoặc external logging service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLoggingService {
    
    /**
     * Log login success
     */
    public void logLoginSuccess(String email, String ipAddress) {
        log.info("AUDIT: Login success - email: {}, ip: {}, timestamp: {}", 
            maskEmail(email), ipAddress, LocalDateTime.now());
        // TODO: Lưu vào database nếu cần
    }
    
    /**
     * Log login failure
     */
    public void logLoginFailure(String email, String ipAddress, String reason) {
        log.warn("AUDIT: Login failure - email: {}, ip: {}, reason: {}, timestamp: {}", 
            maskEmail(email), ipAddress, reason, LocalDateTime.now());
        // TODO: Lưu vào database nếu cần
    }
    
    /**
     * Log token revocation
     */
    public void logTokenRevocation(String email, String tokenHash) {
        log.info("AUDIT: Token revoked - email: {}, tokenHash: {}, timestamp: {}", 
            maskEmail(email), tokenHash, LocalDateTime.now());
        // TODO: Lưu vào database nếu cần
    }
    
    /**
     * Log account lockout
     */
    public void logAccountLockout(String email, String ipAddress, long unlockTime) {
        log.warn("AUDIT: Account locked - email: {}, ip: {}, unlockTime: {}, timestamp: {}", 
            maskEmail(email), ipAddress, unlockTime, LocalDateTime.now());
        // TODO: Lưu vào database nếu cần
    }
    
    /**
     * Log rate limit exceeded
     */
    public void logRateLimitExceeded(String key, String ipAddress, String endpoint) {
        log.warn("AUDIT: Rate limit exceeded - key: {}, ip: {}, endpoint: {}, timestamp: {}", 
            maskKey(key), ipAddress, endpoint, LocalDateTime.now());
        // TODO: Lưu vào database nếu cần
    }
    
    /**
     * Log refresh token usage
     */
    public void logRefreshToken(String email, boolean success) {
        log.info("AUDIT: Refresh token - email: {}, success: {}, timestamp: {}", 
            maskEmail(email), success, LocalDateTime.now());
        // TODO: Lưu vào database nếu cần
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
    
    /**
     * Mask key để log an toàn hơn
     */
    private String maskKey(String key) {
        if (key == null || key.length() <= 10) {
            return "***";
        }
        return key.substring(0, 4) + "***" + key.substring(Math.max(4, key.length() - 4));
    }
}

