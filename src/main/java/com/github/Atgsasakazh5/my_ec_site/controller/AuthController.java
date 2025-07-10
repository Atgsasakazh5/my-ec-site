package com.github.Atgsasakazh5.my_ec_site.controller;

import com.github.Atgsasakazh5.my_ec_site.dto.SignUpRequestDto;
import com.github.Atgsasakazh5.my_ec_site.dto.ApiResponse;
import com.github.Atgsasakazh5.my_ec_site.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // ユーザー登録のエンドポイント
     @PostMapping("/signup")
     public ResponseEntity<ApiResponse> registerUser(@Valid @RequestBody SignUpRequestDto signUpRequestDto) {
         userService.register(signUpRequestDto);

         ApiResponse response = new ApiResponse(true, "ユーザー登録が成功しました");
         return new ResponseEntity<>(response, HttpStatus.CREATED);
     }
}
