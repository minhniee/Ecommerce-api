package com.example.auth_shop.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * APIResponse - Cấu trúc response chuẩn cho tất cả APIs
 * 
 * BEST PRACTICES:
 * ===============
 * 1. Consistent structure cho cả success và error responses
 * 2. Bao gồm HTTP status code trong body (giúp client không cần parse header)
 * 3. Timestamp theo ISO 8601 format
 * 4. Request path để dễ debug
 * 5. Chỉ include các fields có giá trị (null fields sẽ bị bỏ qua)
 * 
 * SUCCESS RESPONSE EXAMPLE:
 * {
 *   "success": true,
 *   "status": 200,
 *   "message": "Products retrieved successfully",
 *   "data": [...],
 *   "timestamp": "2024-01-15T10:30:00"
 * }
 * 
 * ERROR RESPONSE EXAMPLE:
 * {
 *   "success": false,
 *   "status": 400,
 *   "message": "Validation failed",
 *   "errors": [
 *     {"field": "email", "message": "Email is required"},
 *     {"field": "password", "message": "Password must be at least 6 characters"}
 *   ],
 *   "path": "/api/v1/users/create",
 *   "timestamp": "2024-01-15T10:30:00"
 * }
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Không include null fields trong JSON
public class APIResponse {
    
    /**
     * Indicates if the request was successful
     */
    private boolean success;
    
    /**
     * HTTP status code (200, 201, 400, 401, 403, 404, 500, etc.)
     */
    private int status;
    
    /**
     * Human-readable message describing the result
     */
    private String message;
    
    /**
     * Response data (for successful requests)
     */
    private Object data;
    
    /**
     * List of error details (for failed requests)
     */
    private List<ErrorDetail> errors;
    
    /**
     * Request path (useful for debugging)
     */
    private String path;
    
    /**
     * Timestamp when the response was generated
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // ==================== STATIC FACTORY METHODS ====================

    /**
     * Create a success response with data
     */
    public static APIResponse success(String message, Object data) {
        return APIResponse.builder()
                .success(true)
                .status(200)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create a success response with status code and data
     */
    public static APIResponse success(int status, String message, Object data) {
        return APIResponse.builder()
                .success(true)
                .status(status)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create a success response without data (e.g., for DELETE operations)
     */
    public static APIResponse success(String message) {
        return APIResponse.builder()
                .success(true)
                .status(200)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create a created response (HTTP 201)
     */
    public static APIResponse created(String message, Object data) {
        return APIResponse.builder()
                .success(true)
                .status(201)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create an error response
     */
    public static APIResponse error(int status, String message) {
        return APIResponse.builder()
                .success(false)
                .status(status)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create an error response with path
     */
    public static APIResponse error(int status, String message, String path) {
        return APIResponse.builder()
                .success(false)
                .status(status)
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create an error response with validation errors
     */
    public static APIResponse error(int status, String message, List<ErrorDetail> errors, String path) {
        return APIResponse.builder()
                .success(false)
                .status(status)
                .message(message)
                .errors(errors)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create a 400 Bad Request response
     */
    public static APIResponse badRequest(String message) {
        return error(400, message);
    }

    /**
     * Create a 401 Unauthorized response
     */
    public static APIResponse unauthorized(String message) {
        return error(401, message);
    }

    /**
     * Create a 403 Forbidden response
     */
    public static APIResponse forbidden(String message) {
        return error(403, message);
    }

    /**
     * Create a 404 Not Found response
     */
    public static APIResponse notFound(String message) {
        return error(404, message);
    }

    /**
     * Create a 409 Conflict response
     */
    public static APIResponse conflict(String message) {
        return error(409, message);
    }

    /**
     * Create a 500 Internal Server Error response
     */
    public static APIResponse internalError(String message) {
        return error(500, message);
    }

    // ==================== INNER CLASS FOR ERROR DETAILS ====================

    /**
     * ErrorDetail - Chi tiết lỗi cho validation và các lỗi khác
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ErrorDetail {
        /**
         * Field name that has the error (for validation errors)
         */
        private String field;
        
        /**
         * Error message
         */
        private String message;
        
        /**
         * Error code (optional, for client-side error handling)
         */
        private String code;

        public ErrorDetail(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public static ErrorDetail of(String field, String message) {
            return new ErrorDetail(field, message);
        }

        public static ErrorDetail of(String message) {
            return ErrorDetail.builder().message(message).build();
        }
    }
}
