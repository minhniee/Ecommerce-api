
package com.example.auth_shop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {
    // Implementation of token blacklist service

    private static final String TOKEN_BLACKLIST_PREFIX = "blacklisted_token:";
    private final RedisTemplate<String, String> redisTemplate;



    public void blacklistToken(String token, Date expirationDate){
       try{ 
            long ttl = calculateTTL(expirationDate);
            if(ttl <= 0){
                log.warn("Token already expired, not adding to blacklist");
                return; // Thêm return để thoát sớm
            }

            String key = TOKEN_BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(key, "blacklisted", ttl, TimeUnit.MILLISECONDS);
            log.info("Token blacklisted with TTL: {} ms", ttl);   
        } catch(Exception e){
            log.error("Error blacklisting token: {}", e.getMessage());
        }
    }


    public boolean isTokenBlacklisted(String token){
        String key = TOKEN_BLACKLIST_PREFIX + token;
        boolean isBlacklisted = Boolean.TRUE.equals(redisTemplate.hasKey(key));
        log.info("Token {} blacklisted: {}", token, isBlacklisted);
        return isBlacklisted;
    }
    private long calculateTTL(Date expirationDate){
        long currentTime = System.currentTimeMillis();
        long expirationTime = expirationDate.getTime();
        return expirationTime - currentTime; // Trả về giá trị âm nếu đã expired
    }


}