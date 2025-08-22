package com.github.Atgsasakazh5.my_ec_site.dto;

import java.util.Set;

public record UserDto(Long id, String name, String email, Set<String> roles) {
}
