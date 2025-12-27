package com.example.auth_shop.security.config;

import com.example.auth_shop.security.jwt.AuthTokenFilter;
import com.example.auth_shop.security.jwt.JwtAuthEntryPoint;
import com.example.auth_shop.security.user.ShopUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

/**
 * ShopConfig - Main Security Configuration
 * 
 * GIẢI THÍCH VỀ SPRING SECURITY CONFIGURATION:
 * ============================================
 * 
 * @EnableWebSecurity:
 * - Kích hoạt Spring Security cho ứng dụng
 * - Tự động tạo SecurityFilterChain
 * - Disable default security configuration
 * 
 * @EnableMethodSecurity:
 * - Cho phép sử dụng @PreAuthorize, @PostAuthorize, @Secured
 * - prePostEnabled=true: Enable @PreAuthorize và @PostAuthorize
 * 
 * SECURITY FILTER CHAIN:
 * =====================
 * SecurityFilterChain định nghĩa thứ tự và cách các filters hoạt động.
 * Mỗi request sẽ đi qua filter chain theo thứ tự được định nghĩa.
 * 
 * FLOW CỦA MỘT REQUEST:
 * ====================
 * 1. Request đến → Filter Chain
 * 2. CORS Filter → Thêm CORS headers
 * 3. CSRF Filter → Kiểm tra CSRF token (nếu enabled)
 * 4. AuthTokenFilter (Custom) → Validate JWT và set Authentication
 * 5. UsernamePasswordAuthenticationFilter → Xử lý form login (nếu có)
 * 6. FilterSecurityInterceptor → Kiểm tra authorization
 * 7. Controller → Xử lý business logic
 * 
 * AUTHENTICATION vs AUTHORIZATION:
 * ================================
 * - Authentication: Xác định user là ai (login)
 * - Authorization: Xác định user có quyền làm gì (permissions)
 */
@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true) // Cho phép dùng @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
public class ShopConfig {
    private final ShopUserDetailsService userDetailsService;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;

    // Các URLs cần authentication
    private static final List<String> SECURED_URLS = List.of(
        "/api/v1/carts/**",
        "/api/v1/cartItems/**"
    );

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    /**
     * PasswordEncoder - Bean để encode và verify passwords
     * 
     * BCryptPasswordEncoder:
     * ======================
     * - Hash function một chiều (one-way)
     * - Tự động thêm salt vào mỗi password
     * - Có thể điều chỉnh strength (cost factor)
     * - Mỗi lần hash cùng một password sẽ cho kết quả khác nhau (do salt)
     * 
     * TẠI SAO DÙNG BCrypt?
     * ====================
     * - An toàn và được khuyến nghị
     * - Chống lại rainbow table attacks (do salt)
     * - Có thể tăng cost factor khi hardware mạnh hơn
     * - Được sử dụng rộng rãi trong production
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCryptPasswordEncoder với strength 12 (default là 10)
        // Strength càng cao thì càng an toàn nhưng càng chậm
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Custom JWT Authentication Filter
     * 
     * Filter này sẽ được thêm vào filter chain TRƯỚC UsernamePasswordAuthenticationFilter
     */
    @Bean
    public AuthTokenFilter authTokenFilter() {
        return new AuthTokenFilter();
    }

    /**
     * AuthenticationManager - Quản lý authentication process
     * 
     * AuthenticationManager:
     * ======================
     * - Interface chính để authenticate users
     * - Sử dụng các AuthenticationProvider để authenticate
     * - Trả về Authentication object nếu thành công
     * - Throw AuthenticationException nếu thất bại
     * 
     * FLOW:
     * 1. AuthenticationManager.authenticate(authentication)
     * 2. Tìm AuthenticationProvider phù hợp
     * 3. Provider.authenticate(authentication)
     * 4. Return Authentication object
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * DaoAuthenticationProvider - Provider để authenticate với database
     * 
     * DaoAuthenticationProvider:
     * =========================
     * - Sử dụng UserDetailsService để load user từ database
     * - Sử dụng PasswordEncoder để verify password
     * - Tạo Authentication object với UserDetails và authorities
     * 
     * FLOW:
     * 1. Load UserDetails từ UserDetailsService
     * 2. Verify password với PasswordEncoder
     * 3. Check account status (enabled, non-expired, non-locked)
     * 4. Return Authentication object
     */
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        
        // Set UserDetailsService để load user từ database
        authProvider.setUserDetailsService(userDetailsService);
        
        // Set PasswordEncoder để verify password
        authProvider.setPasswordEncoder(passwordEncoder());
        
        return authProvider;
    }

    /**
     * SecurityFilterChain - Main security configuration
     * 
     * Đây là method quan trọng nhất, định nghĩa toàn bộ security behavior
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF CONFIGURATION
            // ===================
            // CSRF (Cross-Site Request Forgery) là một attack khi attacker
            // trick user thực hiện actions trên website mà họ đã login
            //
            // TẠI SAO DISABLE CHO API?
            // - API thường dùng JWT tokens, không dùng cookies
            // - CSRF protection chủ yếu cho form-based authentication
            // - JWT tokens được gửi trong header, không bị ảnh hưởng bởi CSRF
            //
            // NHƯNG: Nếu API có thể được gọi từ browser (form submit),
            // thì nên enable CSRF protection
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")  // Disable CSRF cho API endpoints
                // Nếu muốn enable CSRF cho API, có thể dùng:
                // .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            )
            
            // EXCEPTION HANDLING
            // ==================
            // Xử lý các exception trong quá trình authentication
            // authenticationEntryPoint: Xử lý khi chưa authenticate
            // accessDeniedHandler: Xử lý khi không có quyền (đã authenticate nhưng không đủ quyền)
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthEntryPoint)
                // Có thể thêm:
                // .accessDeniedHandler(customAccessDeniedHandler)
            )
            
            // SESSION MANAGEMENT
            // ==================
            // STATELESS: Không tạo session, mỗi request phải có JWT token
            // Đây là pattern phổ biến cho REST APIs
            //
            // Các options khác:
            // - ALWAYS: Luôn tạo session
            // - IF_REQUIRED: Tạo session nếu cần (default)
            // - NEVER: Không bao giờ tạo session
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // AUTHORIZATION
            // =============
            // Định nghĩa ai có thể truy cập vào đâu
            //
            // Thứ tự quan trọng: Spring Security kiểm tra từ trên xuống dưới
            // Request đầu tiên match sẽ được áp dụng
            .authorizeHttpRequests(auth -> auth
                // Các URLs cần authentication
                .requestMatchers(SECURED_URLS.toArray(String[]::new))
                    .authenticated()  // Phải đã authenticate
                
                // Có thể thêm các rules khác:
                // .requestMatchers("/api/v1/admin/**")
                //     .hasRole("ADMIN")  // Phải có role ADMIN
                // .requestMatchers("/api/v1/users/**")
                //     .hasAnyRole("ADMIN", "USER")  // Phải có một trong các roles
                
                // Tất cả các requests khác đều được phép (public)
                .anyRequest()
                    .permitAll()
            )
            
            // AUTHENTICATION PROVIDER
            // =======================
            // Đăng ký DaoAuthenticationProvider để authenticate users
            .authenticationProvider(daoAuthenticationProvider())
            
            // CUSTOM FILTER
            // =============
            // Thêm AuthTokenFilter TRƯỚC UsernamePasswordAuthenticationFilter
            // Để JWT token được xử lý trước khi đến form login filter
            .addFilterBefore(authTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        
        log.info("Security configuration initialized successfully");
        return http.build();
    }
}
