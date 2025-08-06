package com.github.Atgsasakazh5.my_ec_site.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LoginRequestDto(@NotNull @Size(max = 100) @Email String email,
                              @NotNull @Size(max = 255) String password) {
}
