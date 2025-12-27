package com.example.auth_shop.security.config;

import java.time.LocalDateTime;

public class GlobalVariable {

    /**
     * Lấy thời gian hiện tại.
     * Dùng method thay vì static field để đảm bảo luôn lấy thời gian mới.
     */
    public static LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }
}
