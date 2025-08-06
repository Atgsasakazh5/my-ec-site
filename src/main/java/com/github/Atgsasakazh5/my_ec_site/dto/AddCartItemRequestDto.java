package com.github.Atgsasakazh5.my_ec_site.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddCartItemRequestDto(
        @NotNull @Positive Long skuId,
        @NotNull @Positive Integer quantity
) {
}
