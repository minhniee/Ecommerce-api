package com.example.auth_shop.security.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * CorsConfig - Cấu hình CORS (Cross-Origin Resource Sharing)
 * 
 * GIẢI THÍCH VỀ CORS:
 * ====================
 * 
 * CORS là một cơ chế bảo mật của trình duyệt để kiểm soát các request từ domain khác.
 * 
 * VẤN ĐỀ CORS:
 * ============
 * Khi frontend (http://localhost:3000) gọi API đến backend (http://localhost:8082),
 * trình duyệt sẽ block request này vì khác origin (Same-Origin Policy).
 * 
 * SAME-ORIGIN POLICY:
 * ===================
 * Hai URLs được coi là cùng origin nếu có:
 * - Same protocol (http/https)
 * - Same domain (localhost)
 * - Same port (3000)
 * 
 * Ví dụ:
 * - http://localhost:3000 và http://localhost:8082 → KHÁC ORIGIN (khác port)
 * - http://example.com và https://example.com → KHÁC ORIGIN (khác protocol)
 * - http://example.com và http://api.example.com → KHÁC ORIGIN (khác subdomain)
 * 
 * CORS WORKFLOW:
 * ==============
 * 1. Browser gửi PREFLIGHT request (OPTIONS) trước khi gửi actual request
 * 2. Server trả về CORS headers trong response
 * 3. Browser kiểm tra headers và quyết định có cho phép request không
 * 
 * CORS HEADERS:
 * ============
 * - Access-Control-Allow-Origin: Domain nào được phép truy cập
 * - Access-Control-Allow-Methods: Methods nào được phép (GET, POST, etc.)
 * - Access-Control-Allow-Headers: Headers nào được phép
 * - Access-Control-Allow-Credentials: Có cho phép gửi cookies không
 * - Access-Control-Max-Age: Cache preflight requests trong bao lâu
 * 
 * SECURITY CONSIDERATIONS:
 * =======================
 * 1. KHÔNG dùng "*" cho allowCredentials=true (browser sẽ reject)
 * 2. Chỉ allow các origins cần thiết
 * 3. Chỉ allow các headers cần thiết
 * 4. Chỉ allow các methods cần thiết
 * 5. Set max-age hợp lý để cache preflight requests
 */
@Configuration
public class CorsConfig {

    /**
     * CORS Configuration Source
     * 
     * TẠI SAO CẦN CONFIGURATION SOURCE?
     * ==================================
     * Để có thể config CORS khác nhau cho các paths khác nhau
     * Ví dụ: /api/** có CORS khác với /public/**
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        
        // ALLOWED ORIGINS
        // ===============
        // Chỉ cho phép các origins cụ thể, KHÔNG dùng "*"
        // Nếu dùng "*" thì không thể set allowCredentials=true
        List<String> allowedOrigins = Arrays.asList(
            "http://localhost:3000",  // React app
            "http://localhost:4200",  // Angular app
            "http://127.0.0.1:5500"   // Live Server
        );
        config.setAllowedOrigins(allowedOrigins);
        
        // ALLOWED METHODS
        // ===============
        // Chỉ cho phép các HTTP methods cần thiết
        // Giảm attack surface bằng cách không allow các methods không cần
        config.setAllowedMethods(Arrays.asList(
            "GET",      // Read operations
            "POST",     // Create operations
            "PUT",      // Update operations
            "DELETE",   // Delete operations
            "OPTIONS"   // Preflight requests
        ));
        
        // ALLOWED HEADERS
        // ===============
        // Chỉ cho phép các headers cần thiết
        // Authorization: Cần cho JWT token
        // Content-Type: Cần cho JSON requests
        // X-Requested-With: Một số frameworks cần
        config.setAllowedHeaders(Arrays.asList(
            "Authorization",    // JWT token header
            "Content-Type",     // JSON content type
            "X-Requested-With", // AJAX requests
            "Accept",           // Accept header
            "Origin"            // Origin header
        ));
        
        // EXPOSED HEADERS
        // ==============
        // Headers nào client có thể đọc được từ response
        config.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type"
        ));
        
        // ALLOW CREDENTIALS
        // =================
        // Cho phép gửi cookies và authentication headers
        // QUAN TRỌNG: Phải set true để JWT token hoạt động
        // NHƯNG: Không thể dùng "*" cho allowedOrigins khi này
        config.setAllowCredentials(true);
        
        // MAX AGE
        // =======
        // Cache preflight requests trong 1 giờ
        // Giảm số lượng OPTIONS requests
        config.setMaxAge(3600L); // 1 hour in seconds
        
        // Apply configuration cho tất cả paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        
        return source;
    }

    /**
     * CORS Filter
     * 
     * Filter này sẽ intercept tất cả requests và thêm CORS headers vào response
     */
    @Bean
    public CorsFilter corsFilter() {
        return new CorsFilter(corsConfigurationSource());
    }
}
