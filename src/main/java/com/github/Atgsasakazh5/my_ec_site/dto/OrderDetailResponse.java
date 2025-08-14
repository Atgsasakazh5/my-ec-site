package com.github.Atgsasakazh5.my_ec_site.dto;

import com.github.Atgsasakazh5.my_ec_site.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailResponse(
        Long orderId,
        String shippingAddress,
        String postalCode,
        String shippingName,
        Integer totalPrice,
        OrderStatus status,
        LocalDateTime orderedAt,
        List<OrderDetailDto> orderDetails
) {
}
