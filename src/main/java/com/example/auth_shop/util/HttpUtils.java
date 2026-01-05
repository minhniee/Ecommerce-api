package com.example.auth_shop.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * HttpUtils - Utility class cho HTTP operations
 */
@Slf4j
public class HttpUtils {
    
    /**
     * Lấy client IP address từ request
     * 
     * Xử lý các trường hợp:
     * - X-Forwarded-For header (proxy/load balancer)
     * - X-Real-IP header (nginx)
     * - getRemoteAddr() (direct connection)
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            // X-Forwarded-For có thể chứa nhiều IPs, lấy IP đầu tiên
            log.info("X-Forwarded-For: {}", xForwardedFor);
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            log.info("X-Real-IP: {}", xRealIp);
            return xRealIp;
        }
        log.info("Remote Address: {}", request.getRemoteAddr());
        return request.getRemoteAddr();
    }
}

