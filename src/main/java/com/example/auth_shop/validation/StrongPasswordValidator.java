package com.example.auth_shop.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * StrongPasswordValidator - Validator implementation cho @StrongPassword
 * 
 * GIẢI THÍCH VỀ ConstraintValidator:
 * ===================================
 * 
 * ConstraintValidator là interface để implement validation logic.
 * 
 * GENERICS:
 * ========
 * - T1: Annotation type (@StrongPassword)
 * - T2: Type của field được validate (String)
 * 
 * METHODS:
 * ========
 * - initialize(): Được gọi một lần khi validator được khởi tạo
 * - isValid(): Được gọi mỗi khi cần validate value
 * 
 * VALIDATION LOGIC:
 * ================
 * Sử dụng regex pattern để check password strength:
 * 
 * Pattern breakdown:
 * ^                          - Start of string
 * (?=.*[a-z])               - At least one lowercase letter (positive lookahead)
 * (?=.*[A-Z])               - At least one uppercase letter (positive lookahead)
 * (?=.*\\d)                 - At least one digit (positive lookahead)
 * (?=.*[@$!%*?&])          - At least one special character (positive lookahead)
 * [A-Za-z\\d@$!%*?&]       - Only allowed characters
 * {8,}                      - At least 8 characters
 * $                          - End of string
 * 
 * POSITIVE LOOKAHEAD:
 * ==================
 * (?=.*[a-z]) là positive lookahead, nghĩa là:
 * - Tìm một vị trí trong string
 * - Từ vị trí đó, có thể match .*[a-z] (bất kỳ ký tự nào + lowercase)
 * - Nhưng không consume characters (không di chuyển cursor)
 * - Cho phép check nhiều điều kiện trên cùng một string
 */
public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {
    
    /**
     * Regex pattern để validate password strength
     * 
     * Requirements:
     * - At least 8 characters
     * - At least one lowercase letter
     * - At least one uppercase letter
     * - At least one digit
     * - At least one special character from @$!%*?&
     */
    private static final String PASSWORD_PATTERN = 
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
    
    @Override
    public void initialize(StrongPassword constraintAnnotation) {
        // Có thể đọc config từ annotation nếu cần
        // Ví dụ: minLength, requiredSpecialChars, etc.
    }
    
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        // Null check - nếu null thì để @NotNull handle
        if (password == null) {
            return true; // Return true để @NotNull có thể handle
        }
        
        // Empty check
        if (password.isEmpty()) {
            return false;
        }
        
        // Validate với regex pattern
        boolean isValid = password.matches(PASSWORD_PATTERN);
        
        // Customize error message nếu validation fail
        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Password must be at least 8 characters long and contain: " +
                "uppercase letter, lowercase letter, digit, and special character (@$!%*?&)"
            ).addConstraintViolation();
        }
        
        return isValid;
    }
}

