package com.github.Atgsasakazh5.my_ec_site.dto;

public record OrderDetailDto(
        Long id,
        Long skuId,
        String productName,
        String size,
        String color,
        String imageUrl,
        int priceAtOrder,
        int quantity
) {
}
