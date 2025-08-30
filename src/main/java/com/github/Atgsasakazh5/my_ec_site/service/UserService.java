package com.github.Atgsasakazh5.my_ec_site.service;

import com.github.Atgsasakazh5.my_ec_site.dto.SignUpRequestDto;
import com.github.Atgsasakazh5.my_ec_site.dto.UserDto;

public interface UserService {
    UserDto register(SignUpRequestDto signUpRequestDto);
    UserDto findByEmail(String email);
    void verifyUser(String token);
}