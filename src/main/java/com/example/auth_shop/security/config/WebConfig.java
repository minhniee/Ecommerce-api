package com.example.auth_shop.security.config;

import jakarta.servlet.Filter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebConfig - Web MVC Configuration
 * 
 * LƯU Ý:
 * ======
 * CORS configuration đã được xử lý trong CorsConfig.java
 * Không cần config CORS ở đây nữa để tránh conflict
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Đăng ký SecurityHeadersFilter vào filter chain
     * 
     * FilterRegistrationBean cho phép:
     * - Đăng ký custom filter
     * - Set order (thứ tự trong filter chain)
     * - Set URL patterns
     */
    @Bean
    public FilterRegistrationBean<Filter> securityHeadersFilterRegistration(
            SecurityHeadersConfig securityHeadersConfig) {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(securityHeadersConfig.securityHeadersFilter());
        registration.addUrlPatterns("/*"); // Áp dụng cho tất cả URLs
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE); // Chạy đầu tiên trong filter chain
        registration.setName("securityHeadersFilter");
        return registration;
    }
}
