package com.github.Atgsasakazh5.my_ec_site.dto;

public record CartItemDto(
        Long cartItemId,
        String productName,
        String imageUrl,
        Long skuId,
        String size,
        String color,
        Integer price,
        Integer quantity,
        Integer stockQuantity
) {
}
