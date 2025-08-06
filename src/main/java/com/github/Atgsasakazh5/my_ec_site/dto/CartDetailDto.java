package com.github.Atgsasakazh5.my_ec_site.dto;

import java.util.List;

public record CartDetailDto(
        Long cartId,
        List<CartItemDto> cartItems,
        Integer totalPrice
) {
}
