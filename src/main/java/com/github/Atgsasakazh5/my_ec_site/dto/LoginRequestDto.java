package com.github.Atgsasakazh5.my_ec_site.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;

public record LoginRequestDto(@NotNull @Max(255) String email, @NotNull @Max(255) String password) {
}
