package com.example.auth_shop.controller;

import com.example.auth_shop.exceptions.AccountLockedException;
import com.example.auth_shop.exceptions.AlreadyExistsException;
import com.example.auth_shop.exceptions.RateLimitExceededException;
import com.example.auth_shop.request.LoginRequest;
import com.example.auth_shop.request.RefreshTokenRequest;
import com.example.auth_shop.request.RegisterRequest;
import com.example.auth_shop.response.APIResponse;
import com.example.auth_shop.response.JwtResponse;
import com.example.auth_shop.security.jwt.JwtUtils;
import com.example.auth_shop.security.user.ShopUserDetails;
import com.example.auth_shop.security.user.ShopUserDetailsService;
import com.example.auth_shop.service.AccountLockoutService;
import com.example.auth_shop.service.AuditLoggingService;
import com.example.auth_shop.service.RateLimitingService;
import com.example.auth_shop.service.TokenBlacklistService;
import com.example.auth_shop.service.user.UserService;
import com.example.auth_shop.util.HttpUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final TokenBlacklistService tokenBlacklistService;
    private final RateLimitingService rateLimitingService;
    private final AccountLockoutService accountLockoutService;
    private final AuditLoggingService auditLoggingService;
    private final ShopUserDetailsService userDetailsService;
    private final UserService userService;
    
    // Rate limiting configuration
    private static final int LOGIN_RATE_LIMIT = 5; // 5 requests
    private static final int LOGIN_RATE_LIMIT_WINDOW = 60; // per 60 seconds
    private static final int REGISTER_RATE_LIMIT = 3; // 3 requests
    private static final int REGISTER_RATE_LIMIT_WINDOW = 300; // per 5 minutes

    @PostMapping("/login")
    public ResponseEntity<APIResponse> login(@Valid @RequestBody LoginRequest req, 
                                           HttpServletRequest request) {
        String clientIp = HttpUtils.getClientIpAddress(request);
        String rateLimitKey = "login:" + req.getEmail() + ":" + clientIp;
        
        try {
            // 1. Check rate limit
            if (rateLimitingService.isRateLimitExceeded(rateLimitKey, LOGIN_RATE_LIMIT, LOGIN_RATE_LIMIT_WINDOW)) {
                auditLoggingService.logRateLimitExceeded(rateLimitKey, clientIp, "/login");
                throw new RateLimitExceededException(
                    "Too many login attempts. Please try again later.");
            }
            
            // 2. Check account lockout
            accountLockoutService.checkAccountLocked(req.getEmail());
            
            // 3. Authenticate
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(
                            req.getEmail(), req.getPassword()));

            // 4. Login successful
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String accessToken = jwtUtils.generateTokenForUser(authentication);
            String refreshToken = jwtUtils.generateRefreshToken(authentication);
            ShopUserDetails userDetails = (ShopUserDetails) authentication.getPrincipal();
            
            // 5. Reset failed attempts và rate limit
            accountLockoutService.resetFailedAttempts(req.getEmail());
            rateLimitingService.resetRateLimit(rateLimitKey);
            
            // 6. Audit log
            auditLoggingService.logLoginSuccess(req.getEmail(), clientIp);
            
            // 7. Create response
            JwtResponse jwtResponse = JwtResponse.builder()
                    .id(userDetails.getId())
                    .token(accessToken)
                    .refreshToken(refreshToken)
                    .build();
            
            return ResponseEntity.ok(APIResponse.success("Login successful", jwtResponse));
            
        } catch (AccountLockedException e) {
            auditLoggingService.logLoginFailure(req.getEmail(), clientIp, "Account locked");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(APIResponse.error(403, e.getMessage(), request.getRequestURI()));
                    
        } catch (RateLimitExceededException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(APIResponse.error(429, e.getMessage(), request.getRequestURI()));
                    
        } catch (AuthenticationException e) {
            // Record failed attempt
            accountLockoutService.recordFailedAttempt(req.getEmail());
            auditLoggingService.logLoginFailure(req.getEmail(), clientIp, "Invalid credentials");
            
            int remainingAttempts = accountLockoutService.getRemainingAttempts(req.getEmail());
            String message = remainingAttempts > 0 
                ? String.format("Invalid email or password. %d attempt(s) remaining.", remainingAttempts)
                : "Invalid email or password.";
                
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(APIResponse.error(401, message, request.getRequestURI()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<APIResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest req,
                                                   HttpServletRequest request) {
        try {
            String refreshToken = req.getRefreshToken();
            
            // 1. Validate refresh token
            if (!jwtUtils.validateToken(refreshToken)) {
                auditLoggingService.logRefreshToken("unknown", false);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(APIResponse.error(401, "Invalid refresh token", request.getRequestURI()));
            }
            
            // 2. Check if refresh token is blacklisted
            if (tokenBlacklistService.isTokenBlacklisted(refreshToken)) {
                auditLoggingService.logRefreshToken("unknown", false);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(APIResponse.error(401, "Refresh token has been revoked", request.getRequestURI()));
            }
            
            // 3. Extract email from refresh token
            String email = jwtUtils.getUserNameFromToken(refreshToken);
            
            // 4. Load user details from database
            ShopUserDetails userDetails = (ShopUserDetails) userDetailsService.loadUserByUsername(email);
            
            // 5. Create authentication object
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            
            // 6. Generate new tokens
            String newAccessToken = jwtUtils.generateTokenForUser(authentication);
            String newRefreshToken = jwtUtils.generateRefreshToken(authentication);
            
            // 7. Blacklist old refresh token
            Date oldTokenExpiration = jwtUtils.getExpirationDateFromToken(refreshToken);
            tokenBlacklistService.blacklistToken(refreshToken, oldTokenExpiration);
            
            // 8. Audit log
            auditLoggingService.logRefreshToken(email, true);
            
            // 9. Create response
            JwtResponse jwtResponse = JwtResponse.builder()
                    .id(userDetails.getId())
                    .token(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .build();
            
            return ResponseEntity.ok(APIResponse.success("Token refreshed successfully", jwtResponse));
            
        } catch (Exception e) {
            auditLoggingService.logRefreshToken("unknown", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(APIResponse.error(401, "Invalid refresh token", request.getRequestURI()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<APIResponse> logout(HttpServletRequest request) {
        String clientIp = HttpUtils.getClientIpAddress(request);
        String header = request.getHeader("Authorization"); 
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            Date expirationDate = jwtUtils.getExpirationDateFromToken(token);
            tokenBlacklistService.blacklistToken(token, expirationDate);
            
            // Get email from token for audit log
            try {
                String email = jwtUtils.getUserNameFromToken(token);
                auditLoggingService.logTokenRevocation(email, token.substring(0, 10) + "***");
            } catch (Exception e) {
                // Ignore if cannot extract email
            }
            
            return ResponseEntity.ok(APIResponse.success("Logout successful"));
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(APIResponse.badRequest("Invalid Authorization header. Must start with 'Bearer '"));
    }

    @PostMapping("/register")
    public ResponseEntity<APIResponse> register(@Valid @RequestBody RegisterRequest req,
                                               HttpServletRequest request) {
        String clientIp = HttpUtils.getClientIpAddress(request);
        String rateLimitKey = "register:" + req.getEmail() + ":" + clientIp;
        
        try {
            // 1. Check rate limit
            if (rateLimitingService.isRateLimitExceeded(rateLimitKey, REGISTER_RATE_LIMIT, REGISTER_RATE_LIMIT_WINDOW)) {
                auditLoggingService.logRateLimitExceeded(rateLimitKey, clientIp, "/register");
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(APIResponse.error(429, 
                            "Too many registration attempts. Please try again later.", 
                            request.getRequestURI()));
            }
            
            // 2. Register user
            userService.register(req);
            
            // 3. Reset rate limit (successful registration)
            rateLimitingService.resetRateLimit(rateLimitKey);
            
            // 4. Audit log (có thể thêm method logRegistration nếu cần)
            log.info("User registration successful: {} from IP: {}", req.getEmail(), clientIp);
            
            // 5. Create response
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(APIResponse.success("Registration successful. Please login to continue."));
            
        } catch (AlreadyExistsException e) {
            // Email already exists
            log.warn("Registration attempt with existing email: {} from IP: {}", req.getEmail(), clientIp);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(APIResponse.error(409, e.getMessage(), request.getRequestURI()));
                    
        } catch (Exception e) {
            log.error("Registration error for email: {} from IP: {} - {}", 
                req.getEmail(), clientIp, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(500, "Registration failed. Please try again later.", 
                        request.getRequestURI()));
        }
    }
}
