package com.example.auth_shop.exceptions;
// src/main/java/com/example/auth_shop/exceptions/NotOwnerException.java
public class NotOwnerException extends RuntimeException {
    public NotOwnerException(String message) {
        super(message);
    }
}