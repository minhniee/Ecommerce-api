# Security Improvements Documentation

## Tổng quan

Tài liệu này giải thích các cải thiện bảo mật đã được triển khai trong dự án và cách chúng hoạt động.

## Các cải thiện đã thực hiện

### 1. ✅ AuthTokenFilter - JWT Authentication Filter

**File:** `src/main/java/com/example/auth_shop/security/jwt/AuthTokenFilter.java`

**Cải thiện:**
- Error handling tốt hơn với JSON response chuẩn
- Không expose chi tiết exception để tránh information leakage
- Logging đầy đủ cho debugging
- Giải thích chi tiết về Spring Security Filter Chain

**Kiến thức học được:**
- Spring Security Filter Chain và thứ tự các filters
- SecurityContext và cách lưu Authentication
- OncePerRequestFilter và tại sao cần dùng
- JWT token parsing và validation flow

### 2. ✅ JwtUtils - JWT Token Utilities

**File:** `src/main/java/com/example/auth_shop/security/jwt/JwtUtils.java`

**Cải thiện:**
- Validate JWT secret strength khi khởi động
- Thêm refresh token support
- Cải thiện error handling với các exception types cụ thể
- Thêm methods để extract claims từ token
- Thêm method check token sắp hết hạn

**Kiến thức học được:**
- Cấu trúc JWT (Header.Payload.Signature)
- HMAC-SHA256 algorithm và cách hoạt động
- JWT claims và cách sử dụng
- Token expiration và refresh token pattern
- Base64 encoding/decoding

### 3. ✅ CORS Configuration

**File:** `src/main/java/com/example/auth_shop/security/config/CorsConfig.java`

**Cải thiện:**
- Chỉ allow các origins cụ thể thay vì "*"
- Chỉ allow các headers và methods cần thiết
- Set max-age để cache preflight requests
- Tách riêng CorsConfigurationSource và CorsFilter

**Kiến thức học được:**
- CORS là gì và tại sao cần
- Same-Origin Policy
- Preflight requests (OPTIONS)
- CORS headers và cách hoạt động
- Security considerations khi config CORS

### 4. ✅ ShopConfig - Main Security Configuration

**File:** `src/main/java/com/example/auth_shop/security/config/ShopConfig.java`

**Cải thiện:**
- Giải thích chi tiết về SecurityFilterChain
- CSRF configuration với lý do disable cho API
- Session management (STATELESS)
- Authorization rules
- AuthenticationProvider setup

**Kiến thức học được:**
- @EnableWebSecurity và @EnableMethodSecurity
- SecurityFilterChain và cách config
- Authentication vs Authorization
- CSRF protection và khi nào cần
- Session management strategies
- BCryptPasswordEncoder và password hashing

### 5. ✅ GlobalExceptionHandler

**File:** `src/main/java/com/example/auth_shop/exceptions/GlobalExceptionHandler.java`

**Cải thiện:**
- Xử lý tất cả security exceptions
- Consistent error response format
- Không expose chi tiết exception trong production
- Logging đầy đủ cho debugging

**Kiến thức học được:**
- @ControllerAdvice và centralized exception handling
- Exception hierarchy trong Spring Security
- HTTP status codes phù hợp
- Information leakage prevention

### 6. ✅ SecurityHeadersConfig

**File:** `src/main/java/com/example/auth_shop/security/config/SecurityHeadersConfig.java`

**Cải thiện:**
- Thêm các security headers quan trọng
- Bảo vệ khỏi XSS, clickjacking, MIME sniffing
- Content-Security-Policy
- Referrer-Policy và Permissions-Policy

**Kiến thức học được:**
- Các loại attacks phổ biến (XSS, clickjacking, etc.)
- Security headers và cách chúng bảo vệ
- Content-Security-Policy và cách config
- HSTS và khi nào dùng

### 7. ✅ Application Properties với Environment Variables

**Files:**
- `src/main/resources/application.properties`
- `src/main/resources/application-dev.properties`
- `src/main/resources/application-prod.properties`

**Cải thiện:**
- Sử dụng environment variables cho secrets
- Tách config theo profiles (dev/prod)
- Giảm SQL logging trong production
- Secure Actuator endpoints

**Kiến thức học được:**
- Spring Profiles
- Environment variables trong Spring Boot
- Secret management best practices
- Configuration externalization

### 8. ✅ StrongPassword Validator

**Files:**
- `src/main/java/com/example/auth_shop/validation/StrongPassword.java`
- `src/main/java/com/example/auth_shop/validation/StrongPasswordValidator.java`

**Cải thiện:**
- Custom validator cho password mạnh
- Regex pattern validation
- Custom error messages

**Kiến thức học được:**
- Custom validators trong Jakarta Bean Validation
- ConstraintValidator interface
- Regex patterns và positive lookahead
- Password strength requirements

## Cách sử dụng

### 1. Development

```bash
# Chạy với dev profile
java -jar app.jar --spring.profiles.active=dev

# Hoặc set environment variable
export SPRING_PROFILES_ACTIVE=dev
java -jar app.jar
```

### 2. Production

```bash
# Set environment variables
export DB_URL=jdbc:mysql://prod-db:3306/auth_shop
export DB_USERNAME=prod_user
export DB_PASSWORD=secure_password
export JWT_SECRET=your_base64_encoded_secret_key
export SPRING_PROFILES_ACTIVE=prod

# Chạy application
java -jar app.jar
```

### 3. Generate JWT Secret

```bash
# Linux/Mac
openssl rand -base64 64

# Hoặc online tool
# https://www.allkeysgenerator.com/Random/Security-Encryption-Key-Generator.aspx
```

## Testing Security

### 1. Test JWT Validation

```bash
# Valid token
curl -H "Authorization: Bearer <valid_token>" http://localhost:8082/api/v1/carts

# Invalid token
curl -H "Authorization: Bearer invalid_token" http://localhost:8082/api/v1/carts
```

### 2. Test Password Validation

```bash
# Weak password (should fail)
curl -X POST http://localhost:8082/api/v1/users/user/create \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"123456","firstName":"Test","lastName":"User"}'

# Strong password (should pass)
curl -X POST http://localhost:8082/api/v1/users/user/create \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"Test123!@#","firstName":"Test","lastName":"User"}'
```

### 3. Test CORS

```javascript
// From browser console (different origin)
fetch('http://localhost:8082/api/v1/products/all', {
  method: 'GET',
  headers: {
    'Authorization': 'Bearer <token>'
  }
})
.then(response => response.json())
.then(data => console.log(data));
```

## Best Practices

### 1. Secrets Management

- ✅ Sử dụng environment variables
- ✅ Không commit secrets vào Git
- ✅ Sử dụng secret management tools (AWS Secrets Manager, HashiCorp Vault)
- ✅ Rotate secrets định kỳ

### 2. Password Security

- ✅ Enforce strong passwords
- ✅ Hash passwords với BCrypt
- ✅ Không log passwords
- ✅ Implement password reset flow

### 3. JWT Security

- ✅ Use strong secret keys (256+ bits)
- ✅ Set reasonable expiration times
- ✅ Implement refresh tokens
- ✅ Validate tokens trên mỗi request
- ✅ Use HTTPS để truyền tokens

### 4. Error Handling

- ✅ Không expose chi tiết exception
- ✅ Log đầy đủ cho debugging
- ✅ Consistent error response format
- ✅ Proper HTTP status codes

## Tài liệu tham khảo

- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/index.html)
- [JWT.io](https://jwt.io/)
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [CORS MDN](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS)
- [Jakarta Bean Validation](https://beanvalidation.org/)

## Câu hỏi thường gặp

### Q: Tại sao disable CSRF cho API?
A: CSRF protection chủ yếu cho form-based authentication. API dùng JWT tokens trong headers không bị ảnh hưởng bởi CSRF attacks.

### Q: Tại sao cần refresh token?
A: Refresh tokens có thời gian sống dài hơn, giúp user không phải login lại thường xuyên. Access tokens ngắn hạn giảm risk nếu bị steal.

### Q: Có thể dùng "*" cho CORS không?
A: Không nên, đặc biệt khi allowCredentials=true. Browser sẽ reject. Nên chỉ allow các origins cụ thể.

### Q: Tại sao cần security headers?
A: Security headers bảo vệ khỏi các attacks phổ biến như XSS, clickjacking, MIME sniffing. Chúng là defense-in-depth layer.

## Kết luận

Các cải thiện này giúp ứng dụng an toàn hơn và tuân thủ security best practices. Hãy đọc code và comments để hiểu sâu hơn về từng phần.

