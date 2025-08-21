package com.github.Atgsasakazh5.my_ec_site.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateOrderRequestDto(
        @NotBlank(message = "住所は入力必須です") @Size(max = 255) String shippingAddress,
        @NotBlank(message = "郵便番号は入力必須です") @Size(max = 20) String postalCode,
        @NotBlank(message = "配送先名は入力必須です") @Size(max = 100) String shippingName,
        @NotNull(message = "支払い方法は入力必須です") String paymentMethodId
) {
}
