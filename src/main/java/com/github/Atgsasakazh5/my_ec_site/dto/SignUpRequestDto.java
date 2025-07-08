package com.github.Atgsasakazh5.my_ec_site.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignUpRequestDto(@NotBlank(message = "名前は必須です") String name,
                               @NotBlank(message = "メールアドレスは必須です") @Email String email,
                               @NotBlank(message = "パスワードは必須です") String password,
                               @NotBlank(message = "住所は必須です") String address,
                               boolean subscribingNewsletter) {
}
