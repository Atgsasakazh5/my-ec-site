package com.github.Atgsasakazh5.my_ec_site.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequestDto(
        @NotBlank(message = "カテゴリー名は必須です") @Size(max = 50, message = "カテゴリー名は50文字以内で入力してください") String name) {

}
