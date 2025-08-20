package com.github.Atgsasakazh5.my_ec_site.dto;

import jakarta.validation.constraints.NotNull;

public record PaymentRequestDto(
        @NotNull Long orderId,
        @NotNull String paymentMethodId
) {
}
