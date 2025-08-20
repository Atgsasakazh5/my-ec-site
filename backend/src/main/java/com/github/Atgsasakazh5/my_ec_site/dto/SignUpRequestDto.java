package com.github.Atgsasakazh5.my_ec_site.dto;

import jakarta.validation.constraints.*;

public record SignUpRequestDto(@NotBlank(message = "名前は必須です") @Size(max = 50) String name,
                               @NotBlank(message = "メールアドレスは必須です") @Email @Size(max = 255) String email,
                               @NotBlank(message = "パスワードは必須です") @Size(min = 8, max = 100) String password,
                               @NotBlank(message = "住所は必須です") String address,
                               @NotNull boolean subscribingNewsletter) {
}
