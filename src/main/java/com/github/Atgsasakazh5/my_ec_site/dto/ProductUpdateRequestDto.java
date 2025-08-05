package com.github.Atgsasakazh5.my_ec_site.dto;

import jakarta.validation.constraints.*;

public record ProductUpdateRequestDto(
        @NotBlank @Size(min = 1, max = 255, message = "商品名は1文字以上255文字以下で入力してください" ) String name,
        @NotNull @PositiveOrZero Integer price,
        @NotBlank String description,
        String imageUrl,
        @NotNull Integer categoryId
) {
}
