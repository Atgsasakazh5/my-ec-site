package com.github.Atgsasakazh5.my_ec_site.controller;

import com.github.Atgsasakazh5.my_ec_site.dto.AddCartItemRequestDto;
import com.github.Atgsasakazh5.my_ec_site.dto.CartDetailDto;
import com.github.Atgsasakazh5.my_ec_site.dto.UpdateCartItemRequestDto;
import com.github.Atgsasakazh5.my_ec_site.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/items")
    public ResponseEntity<CartDetailDto> addCartItem(Authentication authentication,
                                                     @Valid @RequestBody AddCartItemRequestDto request) {

        var email = authentication.getName();
        var cartDetail = cartService.addItemToCart(email, request);
        return ResponseEntity.ok(cartDetail);
    }

    @GetMapping
    public ResponseEntity<CartDetailDto> getCartDetail(Authentication authentication) {
        var email = authentication.getName();
        var cartDetail = cartService.getCartDetail(email);
        return ResponseEntity.ok(cartDetail);
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartDetailDto> updateCartItem(Authentication authentication,
                                                        @PathVariable Long cartItemId,
                                                        @Valid @RequestBody UpdateCartItemRequestDto request) {
        var email = authentication.getName();
        var cartDetail = cartService.updateCartItemQuantity(email, cartItemId, request);
        return ResponseEntity.ok(cartDetail);
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Void> deleteCartItem(Authentication authentication,
                                               @PathVariable Long cartItemId) {
        var email = authentication.getName();
        cartService.deleteItemFromCart(email, cartItemId);
        return ResponseEntity.noContent().build();
    }
    
}
