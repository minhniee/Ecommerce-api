package com.example.auth_shop.exceptions;

import com.example.auth_shop.response.APIResponse;
import com.example.auth_shop.response.APIResponse.ErrorDetail;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * GlobalExceptionHandler - Xử lý tất cả exceptions một cách tập trung
 * 
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody
 * Tự động serialize response thành JSON
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== AUTHENTICATION & AUTHORIZATION ====================

    /**
     * 401 - Bad Credentials (sai email/password)
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<APIResponse> handleBadCredentialsException(
            BadCredentialsException ex, HttpServletRequest request) {
        log.warn("Bad credentials for request: {} - {}", request.getRequestURI(), ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(APIResponse.error(401, "Invalid email or password", request.getRequestURI()));
    }

    /**
     * 401 - JWT Token invalid/expired
     */
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<APIResponse> handleJwtException(
            JwtException ex, HttpServletRequest request) {
        log.warn("JWT error for request: {} - {}", request.getRequestURI(), ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(APIResponse.error(401, "Invalid or expired token", request.getRequestURI()));
    }

    /**
     * 401 - Missing authentication
     */
    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ResponseEntity<APIResponse> handleInsufficientAuthenticationException(
            InsufficientAuthenticationException ex, HttpServletRequest request) {
        log.warn("Insufficient authentication for request: {} - {}", 
                request.getRequestURI(), ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(APIResponse.error(401, "Authentication required", request.getRequestURI()));
    }

    /**
     * 403 - Access denied (không có quyền)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<APIResponse> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied for request: {} - {}", request.getRequestURI(), ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(APIResponse.error(403, "You do not have permission to access this resource", 
                        request.getRequestURI()));
    }

    // ==================== VALIDATION ERRORS ====================

    /**
     * 400 - Validation failed (@Valid on @RequestBody)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Validation failed for request: {} - {}", request.getRequestURI(), ex.getMessage());
        
        List<ErrorDetail> errors = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    String fieldName = error instanceof FieldError 
                            ? ((FieldError) error).getField() 
                            : error.getObjectName();
                    String errorMessage = error.getDefaultMessage();
                    return ErrorDetail.of(fieldName, errorMessage);
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(APIResponse.error(400, "Validation failed", errors, request.getRequestURI()));
    }

    /**
     * 400 - Constraint violation (@Valid on @RequestParam/@PathVariable)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<APIResponse> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {
        log.warn("Constraint violation for request: {} - {}", request.getRequestURI(), ex.getMessage());
        
        List<ErrorDetail> errors = ex.getConstraintViolations().stream()
                .map(violation -> ErrorDetail.of(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()))
                .collect(Collectors.toList());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(APIResponse.error(400, "Validation failed", errors, request.getRequestURI()));
    }

    /**
     * 400 - Missing request parameter
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<APIResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        log.warn("Missing parameter for request: {} - {}", request.getRequestURI(), ex.getMessage());
        
        String message = String.format("Required parameter '%s' is missing", ex.getParameterName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(APIResponse.error(400, message, request.getRequestURI()));
    }

    /**
     * 400 - Type mismatch (e.g., String instead of Long)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<APIResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        log.warn("Type mismatch for request: {} - {}", request.getRequestURI(), ex.getMessage());
        
        String message = String.format("Parameter '%s' should be of type %s", 
                ex.getName(), 
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(APIResponse.error(400, message, request.getRequestURI()));
    }

    /**
     * 400 - Malformed JSON
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<APIResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Malformed JSON for request: {} - {}", request.getRequestURI(), ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(APIResponse.error(400, "Malformed JSON request", request.getRequestURI()));
    }

    // ==================== RESOURCE ERRORS ====================

    /**
     * 404 - Resource not found
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<APIResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found for request: {} - {}", request.getRequestURI(), ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(APIResponse.error(404, ex.getMessage(), request.getRequestURI()));
    }

    /**
     * 404 - No handler found
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<APIResponse> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpServletRequest request) {
        log.warn("No handler found for request: {} - {}", request.getRequestURI(), ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(APIResponse.error(404, "Endpoint not found", request.getRequestURI()));
    }

    /**
     * 409 - Resource already exists
     */
    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<APIResponse> handleAlreadyExistsException(
            AlreadyExistsException ex, HttpServletRequest request) {
        log.warn("Resource already exists for request: {} - {}", request.getRequestURI(), ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(APIResponse.error(409, ex.getMessage(), request.getRequestURI()));
    }

    // ==================== BUSINESS LOGIC ERRORS ====================

    /**
     * 400 - Product out of stock
     */
    @ExceptionHandler(ProductOutOfStockException.class)
    public ResponseEntity<APIResponse> handleProductOutOfStockException(
            ProductOutOfStockException ex, HttpServletRequest request) {
        log.warn("Product out of stock for request: {} - {}", request.getRequestURI(), ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(APIResponse.error(400, ex.getMessage(), request.getRequestURI()));
    }

    // ==================== HTTP METHOD ERRORS ====================

    /**
     * 405 - Method not allowed
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<APIResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.warn("Method not allowed for request: {} - {}", request.getRequestURI(), ex.getMessage());
        
        String message = String.format("Method '%s' is not supported for this endpoint", ex.getMethod());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(APIResponse.error(405, message, request.getRequestURI()));
    }

    // ==================== GENERIC ERROR (CATCH-ALL) ====================

    /**
     * 500 - Internal server error
     * 
     * IMPORTANT: Không expose chi tiết exception cho client để tránh information leakage
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error("Unexpected error for request: {} - {}", request.getRequestURI(), ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(APIResponse.error(500, "An unexpected error occurred. Please try again later.", 
                        request.getRequestURI()));
    }
}
