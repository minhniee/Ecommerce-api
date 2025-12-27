package com.example.auth_shop.exceptions;

import com.example.auth_shop.response.APIResponse;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler - Xử lý tất cả exceptions trong ứng dụng
 * 
 * GIẢI THÍCH VỀ @ControllerAdvice:
 * ===============================
 * 
 * @ControllerAdvice là một annotation đặc biệt trong Spring MVC cho phép
 * xử lý exceptions một cách centralized cho tất cả controllers.
 * 
 * CÁCH HOẠT ĐỘNG:
 * ===============
 * 1. Khi một exception được throw trong controller
 * 2. Spring tìm @ExceptionHandler method phù hợp trong @ControllerAdvice
 * 3. Method đó xử lý exception và trả về response
 * 4. Client nhận được response thay vì error page mặc định
 * 
 * LỢI ÍCH:
 * ========
 * - Centralized error handling
 * - Consistent error response format
 * - Không cần try-catch trong mỗi controller
 * - Dễ maintain và test
 * 
 * EXCEPTION HIERARCHY:
 * ===================
 * - Exception (root)
 *   - RuntimeException
 *     - IllegalArgumentException
 *     - IllegalStateException
 *   - IOException
 *   - SecurityException
 *     - AccessDeniedException
 *     - AuthenticationException
 *       - BadCredentialsException
 *       - InsufficientAuthenticationException
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Xử lý AccessDeniedException
     * 
     * KHI NÀO XẢY RA?
     * ==============
     * - User đã authenticate nhưng không có quyền truy cập resource
     * - Ví dụ: User thường cố truy cập admin endpoint
     * 
     * HTTP STATUS: 403 Forbidden
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<APIResponse> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied for request: {} - {}", request.getRequestURI(), ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new APIResponse(
                    "You do not have permission to access this resource",
                    null
                ));
    }

    /**
     * Xử lý JwtException
     * 
     * KHI NÀO XẢY RA?
     * ==============
     * - JWT token không hợp lệ
     * - Token đã hết hạn
     * - Token bị sửa đổi
     * 
     * HTTP STATUS: 401 Unauthorized
     */
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<APIResponse> handleJwtException(
            JwtException ex, HttpServletRequest request) {
        log.warn("JWT error for request: {} - {}", request.getRequestURI(), ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new APIResponse(
                    "Invalid or expired token",
                    null
                ));
    }

    /**
     * Xử lý BadCredentialsException
     * 
     * KHI NÀO XẢY RA?
     * ==============
     * - Username/password không đúng khi login
     * 
     * HTTP STATUS: 401 Unauthorized
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<APIResponse> handleBadCredentialsException(
            BadCredentialsException ex, HttpServletRequest request) {
        log.warn("Bad credentials for request: {} - {}", request.getRequestURI(), ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new APIResponse(
                    "Invalid email or password",
                    null
                ));
    }

    /**
     * Xử lý InsufficientAuthenticationException
     * 
     * KHI NÀO XẢY RA?
     * ==============
     * - Request không có authentication token
     * - Token không đủ thông tin để authenticate
     * 
     * HTTP STATUS: 401 Unauthorized
     */
    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ResponseEntity<APIResponse> handleInsufficientAuthenticationException(
            InsufficientAuthenticationException ex, HttpServletRequest request) {
        log.warn("Insufficient authentication for request: {} - {}", 
                request.getRequestURI(), ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new APIResponse(
                    "Authentication required",
                    null
                ));
    }

    /**
     * Xử lý MethodArgumentNotValidException
     * 
     * KHI NÀO XẢY RA?
     * ==============
     * - Validation fail khi dùng @Valid trong controller
     * - Request body không đúng format hoặc thiếu required fields
     * 
     * HTTP STATUS: 400 Bad Request
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Validation failed for request: {} - {}", 
                request.getRequestURI(), ex.getMessage());
        
        // Collect tất cả validation errors
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new APIResponse(
                    "Validation failed",
                    errors
                ));
    }

    /**
     * Xử lý ConstraintViolationException
     * 
     * KHI NÀO XẢY RA?
     * ==============
     * - Validation fail ở method parameters (không phải request body)
     * - Ví dụ: @Valid trên @RequestParam hoặc @PathVariable
     * 
     * HTTP STATUS: 400 Bad Request
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<APIResponse> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {
        log.warn("Constraint violation for request: {} - {}", 
                request.getRequestURI(), ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        });
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new APIResponse(
                    "Validation failed",
                    errors
                ));
    }

    /**
     * Xử lý ResourceNotFoundException
     * 
     * HTTP STATUS: 404 Not Found
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<APIResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found for request: {} - {}", 
                request.getRequestURI(), ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new APIResponse(
                    ex.getMessage(),
                    null
                ));
    }

    /**
     * Xử lý AlreadyExistsException
     * 
     * HTTP STATUS: 409 Conflict
     */
    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<APIResponse> handleAlreadyExistsException(
            AlreadyExistsException ex, HttpServletRequest request) {
        log.warn("Resource already exists for request: {} - {}", 
                request.getRequestURI(), ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new APIResponse(
                    ex.getMessage(),
                    null
                ));
    }

    /**
     * Xử lý ProductOutOfStockException
     * 
     * HTTP STATUS: 400 Bad Request
     */
    @ExceptionHandler(ProductOutOfStockException.class)
    public ResponseEntity<APIResponse> handleProductOutOfStockException(
            ProductOutOfStockException ex, HttpServletRequest request) {
        log.warn("Product out of stock for request: {} - {}", 
                request.getRequestURI(), ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new APIResponse(
                    ex.getMessage(),
                    null
                ));
    }

    /**
     * Xử lý tất cả các exceptions khác (catch-all)
     * 
     * QUAN TRỌNG:
     * ==========
     * - Luôn đặt ở cuối cùng
     * - Chỉ log error, không expose chi tiết cho client
     * - Tránh information leakage
     * 
     * HTTP STATUS: 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error("Unexpected error for request: {} - {}", 
                request.getRequestURI(), ex.getMessage(), ex);
        
        // KHÔNG expose chi tiết exception cho client
        // Chỉ trả về message chung chung
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new APIResponse(
                    "An error occurred. Please try again later.",
                    null
                ));
    }
}
