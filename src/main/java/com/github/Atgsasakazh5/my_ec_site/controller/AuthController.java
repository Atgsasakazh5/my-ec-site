package com.github.Atgsasakazh5.my_ec_site.controller;

import com.github.Atgsasakazh5.my_ec_site.dto.*;
import com.github.Atgsasakazh5.my_ec_site.security.JwtProvider;
import com.github.Atgsasakazh5.my_ec_site.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    private final AuthenticationManager authenticationManager;

    private final JwtProvider jwtProvider;

    public AuthController(UserService userService,
                          AuthenticationManager authenticationManager,
                          JwtProvider jwtProvider) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtProvider = jwtProvider;
    }

    // ユーザー登録のエンドポイント
    @PostMapping("/signup")
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody SignUpRequestDto signUpRequestDto) {
        UserDto createdUser = userService.register(signUpRequestDto);

        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    // ユーザーログインのエンドポイント
    @PostMapping("/login")
    public ResponseEntity<JwtAuthenticationResponseDto> loginUser(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequestDto.email(), loginRequestDto.password());
        // 認証を実行
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        // 認証が成功した場合、JWTトークンを生成
        String jwt = jwtProvider.generateToken(authentication);
        // レスポンスとしてJWTトークンを返す
        return ResponseEntity.ok(new JwtAuthenticationResponseDto(jwt));

    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyUser(@RequestParam("token") String token) {
        userService.verifyUser(token);
        return ResponseEntity.ok("メールアドレスの認証が完了しました。再度ログインしてください。");
    }
}
