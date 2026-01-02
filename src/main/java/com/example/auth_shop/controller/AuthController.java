package com.example.auth_shop.controller;

import com.example.auth_shop.request.LoginRequest;
import com.example.auth_shop.response.APIResponse;
import com.example.auth_shop.response.JwtResponse;
import com.example.auth_shop.security.jwt.JwtUtils;
import com.example.auth_shop.security.user.ShopUserDetails;
import com.example.auth_shop.service.TokenBlacklistService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final TokenBlacklistService tokenBlacklistService;

    @PostMapping("/login")
    public ResponseEntity<APIResponse> login(@Valid @RequestBody LoginRequest req) {
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(
                            req.getEmail(), req.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateTokenForUser(authentication);
            ShopUserDetails userDetails = (ShopUserDetails) authentication.getPrincipal();
            JwtResponse jwtResponse = new JwtResponse(userDetails.getId(), jwt);
            
            return ResponseEntity.ok(APIResponse.success("Login successful", jwtResponse));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(APIResponse.unauthorized("Invalid email or password"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<APIResponse> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization"); 
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            Date expirationDate = jwtUtils.getExpirationDateFromToken(token);
            tokenBlacklistService.blacklistToken(token, expirationDate);
            return ResponseEntity.ok(APIResponse.success("Logout successful"));
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(APIResponse.badRequest("Invalid Authorization header. Must start with 'Bearer '"));
    }
}
