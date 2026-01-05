package com.example.auth_shop.exceptions;

public class AccountLockedException extends RuntimeException {
    private final long unlockTime; // Unix timestamp khi account sẽ được unlock
    
    public AccountLockedException(String message, long unlockTime) {
        super(message);
        this.unlockTime = unlockTime;
    }
    
    public long getUnlockTime() {
        return unlockTime;
    }
}

