package com.github.Atgsasakazh5.my_ec_site.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductUpdateRequestDto(
        @NotBlank @Max(255) String name,
        @NotNull @PositiveOrZero Integer price,
        @NotBlank String description,
        String imageUrl,
        @NotNull Integer categoryId
) {
}
