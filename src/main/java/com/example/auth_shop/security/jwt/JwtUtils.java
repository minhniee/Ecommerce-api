package com.example.auth_shop.security.jwt;

import com.example.auth_shop.security.user.ShopUserDetails;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;

/**
 * JwtUtils - Utility class để xử lý JWT tokens
 * 
 * GIẢI THÍCH VỀ JWT (JSON Web Token):
 * ====================================
 * 
 * JWT là một chuẩn mở (RFC 7519) để truyền thông tin an toàn giữa các parties
 * dưới dạng JSON object. JWT được sử dụng rộng rãi cho authentication và authorization.
 * 
 * CẤU TRÚC JWT:
 * =============
 * JWT gồm 3 phần được phân cách bởi dấu chấm (.)
 * Format: header.payload.signature
 * 
 * 1. HEADER:
 *    {
 *      "alg": "HS256",  // Algorithm để sign token
 *      "typ": "JWT"     // Type của token
 *    }
 *    → Base64Url encoded
 * 
 * 2. PAYLOAD (Claims):
 *    Claims là các thông tin được lưu trong token
 *    - Registered claims: iss (issuer), exp (expiration), sub (subject), etc.
 *    - Public claims: có thể định nghĩa tùy ý
 *    - Private claims: claims riêng giữa parties
 *    
 *    Ví dụ:
 *    {
 *      "sub": "user@example.com",  // Subject (username)
 *      "id": 123,
 *      "role": ["ROLE_USER"],
 *      "iat": 1234567890,          // Issued at
 *      "exp": 1234567890            // Expiration time
 *    }
 *    → Base64Url encoded
 * 
 * 3. SIGNATURE:
 *    HMACSHA256(
 *      base64UrlEncode(header) + "." + base64UrlEncode(payload),
 *      secret
 *    )
 *    → Đảm bảo token không bị sửa đổi
 * 
 * TẠI SAO DÙNG JWT?
 * ================
 * 1. Stateless: Không cần lưu session trên server
 * 2. Scalable: Có thể dùng trên nhiều servers
 * 3. Portable: Có thể dùng với mobile apps, microservices
 * 4. Self-contained: Chứa tất cả thông tin cần thiết
 * 
 * SECURITY CONSIDERATIONS:
 * ========================
 * 1. Secret key phải đủ mạnh (ít nhất 256 bits cho HS256)
 * 2. Token phải có expiration time
 * 3. Không lưu sensitive data trong payload (chỉ encode, không encrypt)
 * 4. Luôn dùng HTTPS để truyền token
 * 5. Validate token trên mỗi request
 * 
 * ALGORITHMS:
 * ===========
 * - HS256 (HMAC-SHA256): Symmetric - dùng cùng secret để sign và verify
 * - RS256 (RSA-SHA256): Asymmetric - dùng private key để sign, public key để verify
 * - ES256 (ECDSA): Asymmetric - tương tự RS256 nhưng dùng elliptic curves
 */
@Slf4j
@Component
public class JwtUtils {
    
    @Value("${auth.token.jwtSecret}")
    private String jwtSecret;

    @Value("${auth.token.expirationInMils:3600000}") // Default: 1 hour
    private int expirationTime;
    
    @Value("${auth.token.refreshExpirationInMils:86400000}") // Default: 24 hours
    private int refreshExpirationTime;

    /**
     * PostConstruct - Validate JWT secret khi application khởi động
     * 
     * TẠI SAO CẦN VALIDATE?
     * ======================
     * - Đảm bảo secret key đủ mạnh
     * - Fail fast nếu config sai
     * - Tránh runtime errors
     */
    @PostConstruct
    public void validateJwtSecret() {
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new IllegalStateException("JWT secret must not be null or empty");
        }
        
        // Decode để kiểm tra độ dài
        try {
            byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
            if (keyBytes.length < 32) { // 32 bytes = 256 bits
                throw new IllegalStateException(
                    "JWT secret must be at least 256 bits (32 bytes) after Base64 decoding. " +
                    "Current length: " + keyBytes.length + " bytes"
                );
            }
            log.info("JWT secret validated successfully. Key length: {} bits", keyBytes.length * 8);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("JWT secret must be a valid Base64 encoded string", e);
        }
    }

    /**
     * Generate JWT token cho user sau khi authenticate thành công
     * 
     * FLOW:
     * 1. Extract user information từ Authentication object
     * 2. Build JWT với claims (sub, id, role, iat, exp)
     * 3. Sign token với secret key
     * 4. Return compact string
     * 
     * CLAIMS EXPLANATION:
     * - sub (subject): Username/email của user
     * - id: User ID để tránh query database
     * - role: Roles của user để authorization
     * - iat (issued at): Thời điểm token được tạo
     * - exp (expiration): Thời điểm token hết hạn
     */
    public String generateTokenForUser(Authentication authentication) {
        // Extract UserDetails từ Authentication principal
        ShopUserDetails userPrincipal = (ShopUserDetails) authentication.getPrincipal();
        
        // Extract roles từ authorities
        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        // Build JWT token
        return Jwts.builder()
                .setSubject(userPrincipal.getEmail())        // sub claim - username
                .claim("id", userPrincipal.getId())         // Custom claim - user ID
                .claim("role", roles)                       // Custom claim - user roles
                .setIssuedAt(now)                          // iat claim
                .setExpiration(expiryDate)                 // exp claim
                .signWith(key(), SignatureAlgorithm.HS256) // Sign với HMAC-SHA256
                .compact();                                 // Convert thành string
    }

    /**
     * Generate refresh token
     * 
     * REFRESH TOKEN PATTERN:
     * ======================
     * - Access token: Ngắn hạn (1 hour) - chứa nhiều claims
     * - Refresh token: Dài hạn (24 hours) - chỉ chứa subject và expiration
     * 
     * FLOW:
     * 1. User login → nhận cả access token và refresh token
     * 2. Dùng access token cho các API calls
     * 3. Khi access token hết hạn → dùng refresh token để lấy access token mới
     * 4. Refresh token cũng hết hạn → phải login lại
     * 
     * SECURITY BENEFITS:
     * - Giảm thời gian exposure nếu token bị steal
     * - Có thể revoke refresh token nếu bị compromise
     */
    public String generateRefreshToken(Authentication authentication) {
        ShopUserDetails userPrincipal = (ShopUserDetails) authentication.getPrincipal();
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpirationTime);

        return Jwts.builder()
                .setSubject(userPrincipal.getEmail())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Tạo Key object từ secret string
     * 
     * HMAC (Hash-based Message Authentication Code):
     * ==============================================
     * - Symmetric algorithm: dùng cùng key để sign và verify
     * - Fast và efficient
     * - Key phải được giữ bí mật
     * 
     * TẠI SAO DÙNG BASE64 DECODE?
     * ===========================
     * Secret key được lưu dưới dạng Base64 string trong config
     * Cần decode về byte array để tạo Key object
     */
    private Key key() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extract username (email) từ JWT token
     * 
     * FLOW:
     * 1. Parse token với signing key
     * 2. Extract claims body
     * 3. Get subject (sub claim) - đây là username
     * 
     * TẠI SAO CẦN SIGNING KEY?
     * ========================
     * Để verify signature và đảm bảo token không bị sửa đổi
     */
    public String getUserNameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())  // Verify signature với key
                .build()
                .parseClaimsJws(token) // Parse và validate
                .getBody();
        
        return claims.getSubject(); // Return sub claim
    }

    /**
     * Extract user ID từ token
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        return claims.get("id", Long.class);
    }

    /**
     * Extract roles từ token
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        return claims.get("role", List.class);
    }

    public Date getExpirationDateFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        return claims.getExpiration();
    }

    /**
     * Validate JWT token
     * 
     * VALIDATION CHECKS:
     * ==================
     * 1. Signature: Token phải được sign với đúng secret key
     * 2. Expiration: Token chưa hết hạn
     * 3. Format: Token có đúng cấu trúc (header.payload.signature)
     * 4. Algorithm: Token dùng đúng algorithm (HS256)
     * 
     * EXCEPTIONS:
     * - ExpiredJwtException: Token đã hết hạn
     * - UnsupportedJwtException: Token dùng algorithm không được support
     * - MalformedJwtException: Token không đúng format
     * - SignatureException: Signature không hợp lệ
     * - IllegalArgumentException: Token null hoặc empty
     */
    public boolean validateToken(String token) {
        try {
            // Parse và validate token
            // Nếu pass → token hợp lệ
            Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parseClaimsJws(token);
            
            return true;
            
        } catch (ExpiredJwtException e) {
            // Token đã hết hạn
            log.warn("JWT token expired: {}", e.getMessage());
            throw new JwtException("Token expired");
            
        } catch (UnsupportedJwtException e) {
            // Token dùng algorithm không được support
            log.warn("Unsupported JWT token: {}", e.getMessage());
            throw new JwtException("Unsupported token");
            
        } catch (MalformedJwtException e) {
            // Token không đúng format
            log.warn("Malformed JWT token: {}", e.getMessage());
            throw new JwtException("Invalid token format");
            
        } catch (SignatureException e) {
            // Signature không hợp lệ - token có thể bị sửa đổi
            log.warn("Invalid JWT signature: {}", e.getMessage());
            throw new JwtException("Invalid token signature");
            
        } catch (IllegalArgumentException e) {
            // Token null hoặc empty
            log.warn("JWT token is null or empty");
            throw new JwtException("Token cannot be empty");
        }
    }

    /**
     * Check nếu token sắp hết hạn (trong vòng 5 phút)
     * Dùng để refresh token trước khi hết hạn
     */
    public boolean isTokenExpiringSoon(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            Date expiration = claims.getExpiration();
            long timeUntilExpiration = expiration.getTime() - System.currentTimeMillis();
            
            // Nếu còn < 5 phút thì coi là sắp hết hạn
            return timeUntilExpiration < 300000; // 5 minutes in milliseconds
            
        } catch (Exception e) {
            return true; // Nếu không parse được thì coi như sắp hết hạn
        }
    }
}
