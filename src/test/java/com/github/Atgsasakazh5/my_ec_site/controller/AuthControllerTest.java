package com.github.Atgsasakazh5.my_ec_site.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.Atgsasakazh5.my_ec_site.dto.SignUpRequestDto;
import com.github.Atgsasakazh5.my_ec_site.dto.UserDto;
import com.github.Atgsasakazh5.my_ec_site.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    UserService userService;

    @Test
    @DisplayName("ユーザー登録のテスト-正常系")
    void registerUser_return201AndUser_whenRequestCorrect() throws Exception {
        // Arrange
        var signUpRequestDto = new SignUpRequestDto("testuser", "test@email.com",
                "password123", "Tokyo", true);
        var expectedUserDto = new UserDto(1L, "testuser", "test@email.com");

        // Mock UserServiceのregisterメソッドの動作を定義
        when(userService.register(any(SignUpRequestDto.class))).thenReturn(expectedUserDto);

        // Act & Assert
        // objectMapperを使用してJSONに変換し、POSTリクエストを送信
        mockMvc.perform(post("/api/auth/signup")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(signUpRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(expectedUserDto.id()))
                .andExpect(jsonPath("$.name").value(expectedUserDto.name()))
                .andExpect(jsonPath("$.email").value(expectedUserDto.email()));

    }

    @Test
    @DisplayName("ユーザー登録のテスト-異常系 空のユーザー名")
    void registerUser_return400_whenEmptyName() throws Exception {
        // Arrange
        var signUpRequestDto = new SignUpRequestDto("", "test@email.com",
                "password123", "Tokyo", true);
        // Act & Assert
        mockMvc.perform(post("/api/auth/signup")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(signUpRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("name: 名前は必須です"));

        verify(userService, never()).register(any(SignUpRequestDto.class));
    }

    @Test
    @DisplayName("ユーザー登録のテスト-異常系 メールアドレスの重複")
    void registerUser_return409_whenEmailAlreadyExists() throws Exception {
        // Arrange
        var signUpRequestDto = new SignUpRequestDto("testuser", "test@email.com",
                "password123", "Tokyo", true);
        // Mock UserServiceのregisterメソッドがIllegalArgumentExceptionをスローするように設定
        doThrow(new IllegalStateException("メールアドレスはすでに使用されています"))
                .when(userService).register(any(SignUpRequestDto.class));
        // Act & Assert
        mockMvc.perform(post("/api/auth/signup")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(signUpRequestDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("メールアドレスはすでに使用されています"));
        verify(userService, times(1)).register(any(SignUpRequestDto.class));

        }
}