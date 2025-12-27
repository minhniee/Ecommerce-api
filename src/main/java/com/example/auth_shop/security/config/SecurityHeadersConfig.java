package com.example.auth_shop.security.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * SecurityHeadersConfig - Thêm các security headers vào HTTP response
 * 
 * GIẢI THÍCH VỀ SECURITY HEADERS:
 * ===============================
 * 
 * Security headers là các HTTP headers đặc biệt giúp bảo vệ ứng dụng
 * khỏi các attacks phổ biến như XSS, clickjacking, MIME sniffing, etc.
 * 
 * CÁC ATTACKS VÀ CÁCH BẢO VỆ:
 * ==========================
 * 
 * 1. XSS (Cross-Site Scripting):
 *    - Attack: Inject malicious scripts vào website
 *    - Protection: X-XSS-Protection, Content-Security-Policy
 * 
 * 2. Clickjacking:
 *    - Attack: Trick user click vào button/link ẩn
 *    - Protection: X-Frame-Options
 * 
 * 3. MIME Sniffing:
 *    - Attack: Browser tự động detect content type, có thể execute scripts
 *    - Protection: X-Content-Type-Options
 * 
 * 4. Man-in-the-Middle:
 *    - Attack: Intercept và modify traffic
 *    - Protection: Strict-Transport-Security (HSTS)
 * 
 * 5. Information Disclosure:
 *    - Attack: Leak thông tin về server
 *    - Protection: X-Powered-By removal, Server header removal
 */
@Configuration
public class SecurityHeadersConfig {

    /**
     * Security Headers Filter
     * 
     * Filter này sẽ thêm các security headers vào mỗi HTTP response
     */
    @Bean
    public Filter securityHeadersFilter() {
        return new Filter() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, 
                                FilterChain chain) throws IOException, ServletException {
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                
                // X-FRAME-OPTIONS
                // ===============
                // Ngăn website được embed trong iframe
                // Options:
                // - DENY: Không cho phép embed ở bất kỳ đâu
                // - SAMEORIGIN: Chỉ cho phép embed từ cùng origin
                // - ALLOW-FROM uri: Chỉ cho phép từ URI cụ thể (deprecated)
                httpResponse.setHeader("X-Frame-Options", "DENY");
                
                // X-CONTENT-TYPE-OPTIONS
                // ======================
                // Ngăn browser tự động detect MIME type
                // Bảo vệ khỏi MIME sniffing attacks
                // nosniff: Browser phải dùng Content-Type header, không được guess
                httpResponse.setHeader("X-Content-Type-Options", "nosniff");
                
                // X-XSS-PROTECTION
                // ================
                // Enable XSS filter của browser (legacy, nhưng vẫn hữu ích)
                // 1; mode=block: Enable filter và block page nếu detect XSS
                // Note: Modern browsers có CSP thì không cần header này nữa
                httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
                
                // STRICT-TRANSPORT-SECURITY (HSTS)
                // =================================
                // Bắt buộc browser chỉ dùng HTTPS trong tương lai
                // max-age: Thời gian cache policy (seconds)
                // includeSubDomains: Áp dụng cho cả subdomains
                // preload: Có thể thêm vào HSTS preload list
                // 
                // QUAN TRỌNG: Chỉ set khi đã dùng HTTPS, không set cho HTTP
                // Nếu set cho HTTP, browser sẽ không thể truy cập site nữa
                // httpResponse.setHeader("Strict-Transport-Security", 
                //     "max-age=31536000; includeSubDomains; preload");
                
                // CONTENT-SECURITY-POLICY (CSP)
                // =============================
                // Định nghĩa các sources được phép load resources
                // 
                // Directives:
                // - default-src: Default cho tất cả resources
                // - script-src: Chỉ cho phép scripts từ sources nào
                // - style-src: Chỉ cho phép styles từ sources nào
                // - img-src: Chỉ cho phép images từ sources nào
                // - connect-src: Chỉ cho phép AJAX/fetch từ sources nào
                // 
                // 'self': Chỉ từ cùng origin
                // 'unsafe-inline': Cho phép inline scripts/styles (không khuyến nghị)
                // 'unsafe-eval': Cho phép eval() (không khuyến nghị)
                //
                // Ví dụ strict:
                // "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline';"
                //
                // Ví dụ cho development (cho phép inline):
                httpResponse.setHeader("Content-Security-Policy", 
                    "default-src 'self'; " +
                    "script-src 'self' 'unsafe-inline'; " +
                    "style-src 'self' 'unsafe-inline'; " +
                    "img-src 'self' data: https:; " +
                    "font-src 'self' data:; " +
                    "connect-src 'self'"
                );
                
                // REFERRER-POLICY
                // ===============
                // Kiểm soát thông tin Referer header được gửi
                // Options:
                // - no-referrer: Không gửi Referer header
                // - strict-origin-when-cross-origin: Gửi origin khi cross-origin
                // - same-origin: Chỉ gửi khi cùng origin
                // - no-referrer-when-downgrade: Không gửi khi downgrade từ HTTPS → HTTP
                httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
                
                // PERMISSIONS-POLICY (trước đây là Feature-Policy)
                // ================================================
                // Disable các browser features không cần thiết
                // Giảm attack surface
                // 
                // Features có thể disable:
                // - geolocation: GPS location
                // - microphone: Microphone access
                // - camera: Camera access
                // - payment: Payment API
                // - usb: USB device access
                httpResponse.setHeader("Permissions-Policy", 
                    "geolocation=(), " +
                    "microphone=(), " +
                    "camera=(), " +
                    "payment=(), " +
                    "usb=()"
                );
                
                // REMOVE SERVER HEADER
                // ====================
                // Ẩn thông tin về server (Spring Boot, Tomcat, etc.)
                // Giảm information disclosure
                // Note: Có thể config trong application.properties:
                // server.error.include-server-info=false
                
                // REMOVE X-POWERED-BY HEADER
                // ==========================
                // Ẩn thông tin về framework/technology stack
                // Có thể config trong application.properties:
                // server.servlet.context-path=/
                // Hoặc trong web.xml cho servlet containers
                
                // Continue filter chain
                chain.doFilter(request, response);
            }
        };
    }
}

