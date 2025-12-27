package com.example.auth_shop.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * StrongPassword - Custom validation annotation cho password mạnh
 * 
 * GIẢI THÍCH VỀ CUSTOM VALIDATOR:
 * ===============================
 * 
 * Jakarta Bean Validation cho phép tạo custom validators để validate
 * các business rules phức tạp mà các built-in validators không hỗ trợ.
 * 
 * CÁCH TẠO CUSTOM VALIDATOR:
 * ==========================
 * 1. Tạo annotation interface (@StrongPassword)
 * 2. Tạo validator class (StrongPasswordValidator)
 * 3. Link annotation với validator bằng @Constraint
 * 4. Sử dụng annotation trên fields
 * 
 * PASSWORD STRENGTH REQUIREMENTS:
 * ==============================
 * - Tối thiểu 8 ký tự
 * - Ít nhất 1 chữ hoa (A-Z)
 * - Ít nhất 1 chữ thường (a-z)
 * - Ít nhất 1 số (0-9)
 * - Ít nhất 1 ký tự đặc biệt (@$!%*?&)
 * 
 * TẠI SAO CẦN PASSWORD MẠNH?
 * ===========================
 * - Chống lại brute force attacks
 * - Chống lại dictionary attacks
 * - Đảm bảo account security
 * - Tuân thủ security best practices
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StrongPasswordValidator.class)
@Documented
public @interface StrongPassword {
    
    /**
     * Error message khi validation fail
     * Có thể override bằng cách set message trong annotation
     */
    String message() default "Password must be at least 8 characters long and contain uppercase, lowercase, digit and special character";
    
    /**
     * Validation groups
     * Cho phép group các validations lại với nhau
     */
    Class<?>[] groups() default {};
    
    /**
     * Payload
     * Cho phép attach metadata vào constraint
     */
    Class<? extends Payload>[] payload() default {};
}

