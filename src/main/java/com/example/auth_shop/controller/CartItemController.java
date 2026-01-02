package com.example.auth_shop.controller;

import com.example.auth_shop.model.Cart;
import com.example.auth_shop.model.User;
import com.example.auth_shop.response.APIResponse;
import com.example.auth_shop.service.cart.ICartItemService;
import com.example.auth_shop.service.cart.ICartService;
import com.example.auth_shop.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/cart-items")
@RequiredArgsConstructor
public class CartItemController {
    private final ICartItemService cartItemService;
    private final ICartService cartService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<APIResponse> addItemToCart(
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        User user = userService.getAuthenticatedUser();
        Cart cart = cartService.initializeNewCart(user);
        cartItemService.addItemToCart(cart.getId(), productId, quantity);
        return ResponseEntity.ok(APIResponse.success("Item added to cart successfully"));
    }

    @DeleteMapping("/{cartId}/items/{itemId}")
    public ResponseEntity<APIResponse> removeItemFromCart(
            @PathVariable Long cartId, 
            @PathVariable Long itemId) {
        cartItemService.removeItemFromCart(cartId, itemId);
        return ResponseEntity.ok(APIResponse.success("Item removed from cart successfully"));
    }

    @PutMapping("/{cartId}/items/{itemId}")
    public ResponseEntity<APIResponse> updateItemQuantity(
            @PathVariable Long cartId,
            @PathVariable Long itemId,
            @RequestParam Integer quantity) {
        cartItemService.updateItemQuantity(cartId, itemId, quantity);
        return ResponseEntity.ok(APIResponse.success("Item quantity updated successfully"));
    }
}
