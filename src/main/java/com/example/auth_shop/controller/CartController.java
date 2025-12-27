package com.example.auth_shop.controller;

import com.example.auth_shop.security.config.GlobalVariable;
import com.example.auth_shop.dto.CartDto;
import com.example.auth_shop.exceptions.ResourceNotFoundException;
import com.example.auth_shop.model.Cart;
import com.example.auth_shop.response.APIResponse;
import com.example.auth_shop.service.cart.ICartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/carts")
public class CartController {
    private final ICartService cartService;
    private final LocalDateTime CURRENT_DATE = LocalDateTime.now();

    @GetMapping("/{cartId}/my-cart")
    public ResponseEntity<APIResponse> getCarts(@PathVariable Long cartId) {
        try {
            Cart cart = cartService.getCart(cartId);
            CartDto cartDto = cartService.convertToCartDto(cart);
            return ResponseEntity.ok(new APIResponse(GlobalVariable.getCurrentDateTime(), "Success", cartDto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new APIResponse(e.getMessage(), null));
        }
    }

    //    @GetMapping("/{cartId}/my-cart")
//    public Cart getCarts(@PathVariable Long cartId) {
////        try {
//            Cart cart = cartService.getCart(cartId);
//            return cart;
////            return ResponseEntity.ok(new APIResponse("Success", cart));
////        } catch (ResourceNotFoundException e) {
////            return ResponseEntity.status(NOT_FOUND).body(new APIResponse(e.getMessage(), null));
////        }
////        }
//    }
    @DeleteMapping("{cartId}/clear")
    public ResponseEntity<APIResponse> createCart(@PathVariable Long cartId) {
        try {
            cartService.clearCart(cartId);
            return ResponseEntity.ok(new APIResponse("Clear Cart Success", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new APIResponse(e.getMessage(), null));
        }
    }

    @GetMapping("{cartId}/cart/total-price")
    public ResponseEntity<APIResponse> getTotalAmount(@PathVariable Long cartId) {
        try {
            BigDecimal totalPrice = cartService.getTotalPrice(cartId);
            return ResponseEntity.ok(new APIResponse("Success", totalPrice));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new APIResponse(e.getMessage(), null));
        }
    }
}
