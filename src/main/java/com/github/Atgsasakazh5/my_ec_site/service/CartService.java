package com.github.Atgsasakazh5.my_ec_site.service;

import com.github.Atgsasakazh5.my_ec_site.dto.AddCartItemRequestDto;
import com.github.Atgsasakazh5.my_ec_site.dto.CartDetailDto;
import com.github.Atgsasakazh5.my_ec_site.dto.UpdateCartItemRequestDto;

public interface CartService {
    CartDetailDto getCartDetail(Long cartId);
    CartDetailDto getCartDetail(String email);
    CartDetailDto addItemToCart(String email, AddCartItemRequestDto requestDto);
    CartDetailDto updateCartItemQuantity(String email, Long cartItemId, UpdateCartItemRequestDto request);
    void deleteItemFromCart(String email, Long cartItemId);
    void deleteAllItems(Long cartId);
    void validateCartInventory(String email);
}