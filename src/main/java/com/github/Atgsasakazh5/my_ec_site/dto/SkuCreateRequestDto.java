package com.github.Atgsasakazh5.my_ec_site.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record SkuCreateRequestDto(
        @NotNull @Size(max = 100, message = "サイズは100文字以内で入力してください") String size,
        @NotNull @Size(max = 100, message = "カラーは100文字以内で入力してください") String color,
        @NotNull @PositiveOrZero Integer extraPrice,
        @NotNull @PositiveOrZero Integer quantity
) {
}
