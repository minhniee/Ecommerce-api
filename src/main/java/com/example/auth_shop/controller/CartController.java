package com.example.auth_shop.controller;

import com.example.auth_shop.dto.CartDto;
import com.example.auth_shop.model.Cart;
import com.example.auth_shop.response.APIResponse;
import com.example.auth_shop.service.cart.ICartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/carts")
public class CartController {
    private final ICartService cartService;

    @GetMapping("/{cartId}")
    public ResponseEntity<APIResponse> getCart(@PathVariable Long cartId) {
        Cart cart = cartService.getCart(cartId);
        CartDto cartDto = cartService.convertToCartDto(cart);
        return ResponseEntity.ok(APIResponse.success("Cart retrieved successfully", cartDto));
    }

    @DeleteMapping("/{cartId}")
    public ResponseEntity<APIResponse> clearCart(@PathVariable Long cartId) {
        cartService.clearCart(cartId);
        return ResponseEntity.ok(APIResponse.success("Cart cleared successfully"));
    }

    @GetMapping("/{cartId}/total")
    public ResponseEntity<APIResponse> getTotalAmount(@PathVariable Long cartId) {
        BigDecimal totalPrice = cartService.getTotalPrice(cartId);
        return ResponseEntity.ok(APIResponse.success("Total price retrieved successfully", totalPrice));
    }
}
