package com.example.auth_shop.security.jwt;

import com.example.auth_shop.security.user.ShopUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import com.example.auth_shop.service.TokenBlacklistService; 

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * AuthTokenFilter - Custom Filter trong Spring Security Filter Chain
 * 
 * GIẢI THÍCH SPRING SECURITY FILTER CHAIN:
 * ==========================================
 * 
 * Spring Security sử dụng một chuỗi các Filter (Filter Chain) để xử lý mỗi HTTP request.
 * Thứ tự các filter rất quan trọng vì mỗi filter có thể:
 * - Cho phép request tiếp tục (chain.doFilter)
 * - Chặn request và trả về response ngay lập tức
 * - Modify request/response trước khi chuyển tiếp
 * 
 * FILTER CHAIN ORDER (từ đầu đến cuối):
 * 1. ChannelProcessingFilter - Xử lý HTTPS redirect
 * 2. SecurityContextPersistenceFilter - Lưu SecurityContext giữa các requests
 * 3. HeaderWriterFilter - Thêm security headers
 * 4. CorsFilter - Xử lý CORS
 * 5. CsrfFilter - Bảo vệ CSRF attacks
 * 6. LogoutFilter - Xử lý logout
 * 7. UsernamePasswordAuthenticationFilter - Xử lý form login
 * 8. [CUSTOM FILTERS] - Các filter tự định nghĩa (như AuthTokenFilter này)
 * 9. BasicAuthenticationFilter - Xử lý Basic Auth
 * 10. RequestCacheAwareFilter - Cache requests để redirect sau khi login
 * 11. SecurityContextHolderAwareRequestFilter - Wrap request với security-aware
 * 12. AnonymousAuthenticationFilter - Tạo anonymous authentication nếu chưa authenticate
 * 13. SessionManagementFilter - Quản lý sessions
 * 14. ExceptionTranslationFilter - Xử lý exceptions và redirect
 * 15. FilterSecurityInterceptor - Thực hiện authorization (kiểm tra quyền truy cập)
 * 
 * ONCEPERREQUESTFILTER:
 * =====================
 * OncePerRequestFilter đảm bảo filter chỉ chạy 1 lần cho mỗi request,
 * ngay cả khi request được forward hoặc include.
 * 
 * TẠI SAO CẦN FILTER NÀY?
 * =========================
 * - JWT tokens không được Spring Security xử lý mặc định
 * - Cần extract JWT từ Authorization header
 * - Validate token và tạo Authentication object
 * - Set vào SecurityContext để các filter sau có thể sử dụng
 */
@Slf4j
public class AuthTokenFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private ShopUserDetailsService userDetailsService;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    /**
     * doFilterInternal - Method chính của filter
     * 
     * FLOW XỬ LÝ:
     * 1. Parse JWT từ Authorization header
     * 2. Validate token (signature, expiration)
     * 3. Extract username từ token
     * 4. Load UserDetails từ database
     * 5. Tạo Authentication object
     * 6. Set vào SecurityContext
     * 7. Cho phép request tiếp tục trong filter chain
     * 
     * SECURITY CONTEXT:
     * ================
     * SecurityContext chứa Authentication object, được lưu trong ThreadLocal.
     * Các filter sau và controller có thể truy cập qua:
     * SecurityContextHolder.getContext().getAuthentication()
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            // Bước 1: Parse JWT từ Authorization header
            // Format: "Authorization: Bearer <token>"
            String jwt = parseJwt(request);
            
            // Bước 2: Nếu có token và token hợp lệ
            if (StringUtils.hasText(jwt) && jwtUtils.validateToken(jwt)) {
                // Bước 3: Extract username (email) từ token
                if (tokenBlacklistService.isTokenBlacklisted(jwt)) {
                     handleAuthenticationException(request, response, "Token has been revoked");
                     return;
                }
                String username = jwtUtils.getUserNameFromToken(jwt);
                
                // Bước 4: Load UserDetails từ database
                // UserDetails chứa thông tin user và authorities (roles)
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // Bước 5: Tạo Authentication object
                // UsernamePasswordAuthenticationToken là implementation của Authentication
                // Parameters: principal (user), credentials (null vì đã validate), authorities
                Authentication auth = new UsernamePasswordAuthenticationToken(
                    userDetails,           // Principal - thông tin user
                    null,                  // Credentials - không cần vì đã validate JWT
                    userDetails.getAuthorities() // Authorities - roles của user
                );
                
                // Bước 6: Set Authentication vào SecurityContext
                // SecurityContext được lưu trong ThreadLocal, chỉ tồn tại trong thread hiện tại
                SecurityContextHolder.getContext().setAuthentication(auth);
                
                log.debug("Set SecurityContext for user: {}", username);
            }
            
            // Bước 7: Cho phép request tiếp tục trong filter chain
            // Nếu không gọi doFilter, request sẽ bị chặn
            filterChain.doFilter(request, response);
            
        } catch (JwtException e) {
            // JWT validation failed - token invalid, expired, hoặc malformed
            log.error("JWT validation failed: {}", e.getMessage());
            handleJwtException(request, response, e);
            // KHÔNG gọi filterChain.doFilter() - dừng filter chain và trả về error
            
        } catch (UsernameNotFoundException e) {
            // User không tồn tại trong database
            log.error("User not found: {}", e.getMessage());
            handleAuthenticationException(request, response, "User not found");
            // KHÔNG gọi filterChain.doFilter()
            
        } catch (Exception e) {
            // Các exception khác
            log.error("Authentication error: {}", e.getMessage(), e);
            handleAuthenticationException(request, response, "Authentication failed");
            // KHÔNG gọi filterChain.doFilter()
        }
    }

    /**
     * Xử lý JWT exception - trả về response JSON chuẩn
     * 
     * TẠI SAO KHÔNG EXPOSE CHI TIẾT ERROR?
     * ====================================
     * - Không expose thông tin về token structure
     * - Không cho attacker biết tại sao token fail
     * - Chỉ trả về message chung chung
     */
    private void handleJwtException(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   JwtException e) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        errorResponse.put("error", "Unauthorized");
        errorResponse.put("message", "Invalid or expired token");
        errorResponse.put("path", request.getRequestURI());
        
        // Không expose chi tiết exception để tránh information leakage
        response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
    }

    /**
     * Xử lý authentication exception chung
     */
    private void handleAuthenticationException(HttpServletRequest request,
                                              HttpServletResponse response,
                                              String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        errorResponse.put("error", "Unauthorized");
        errorResponse.put("message", message);
        errorResponse.put("path", request.getRequestURI());
        
        response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
    }

    /**
     * Parse JWT từ Authorization header
     * 
     * FORMAT:
     * Authorization: Bearer <token>
     * 
     * TẠI SAO DÙNG "Bearer"?
     * ======================
     * Bearer là một authentication scheme trong HTTP.
     * Nó cho biết token được gửi kèm, không phải credentials.
     * Các scheme khác: Basic, Digest, etc.
     */
    private String parseJwt(@NonNull HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        
        // Kiểm tra header có tồn tại và bắt đầu bằng "Bearer "
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            // Extract token (bỏ qua "Bearer " - 7 ký tự đầu)
            return headerAuth.substring(7);
        }
        
        return null;
    }
}
