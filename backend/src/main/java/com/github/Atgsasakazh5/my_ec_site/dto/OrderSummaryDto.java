package com.github.Atgsasakazh5.my_ec_site.dto;

import com.github.Atgsasakazh5.my_ec_site.entity.OrderStatus;

import java.time.LocalDateTime;

public record OrderSummaryDto(
        Long orderId,
        LocalDateTime orderedAt,
        Integer totalPrice,
        OrderStatus status
) {
}
