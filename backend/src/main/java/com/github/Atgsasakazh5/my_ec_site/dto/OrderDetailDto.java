package com.github.Atgsasakazh5.my_ec_site.dto;

public record OrderDetailDto(
        Long id,
        Long orderId,
        Long skuId,
        String productName,
        String size,
        String color,
        String imageUrl,
        int priceAtOrder,
        int quantity
) {
}
