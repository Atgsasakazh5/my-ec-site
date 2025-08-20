package com.github.Atgsasakazh5.my_ec_site.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record ProductCreateRequestDto(
        @NotBlank(message = "商品名は必須です") @Size(min = 1, max = 255, message = "商品名は1文字以上255文字以下で入力してください" ) String name,
        @NotNull(message = "カテゴリIDは必須です") @Min(value = 1, message = "カテゴリIDは1以上で指定してください") Integer categoryId,
        @NotBlank @Size(min = 1, max = 1000, message = "商品説明は1文字以上1000文字以下で入力してください" ) String description,
        @Size(min = 1, max = 255) String imageUrl,
        @NotNull(message = "価格は必須です") @PositiveOrZero @Max(1000000) Integer price,
        @NotEmpty @Valid List<SkuCreateRequestDto> skus
        ) {
}
