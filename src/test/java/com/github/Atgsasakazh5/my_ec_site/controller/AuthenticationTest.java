package com.github.Atgsasakazh5.my_ec_site.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.Atgsasakazh5.my_ec_site.config.SecurityConfig;
import com.github.Atgsasakazh5.my_ec_site.dto.JwtAuthenticationResponseDto;
import com.github.Atgsasakazh5.my_ec_site.dto.LoginRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(SecurityConfig.class)
@Sql(scripts = {"/schema-mysql.sql", "/data-mysql.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class AuthenticationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("ログインテスト - 正常系")
    void loginUser_return200AndJwt_whenRequestCorrect() throws Exception {
        // Arrange
        var loginRequestDto = new LoginRequestDto("test@email.com", "password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.accessToken").isString());

    }

    @Test
    @DisplayName("不正なパスワードによるログインテスト - 異常系")
    void loginUser_return401_whenPasswordIncorrect() throws Exception {
        // Arrange
        var loginRequestDto = new LoginRequestDto("test@email.com", "wrongpassword");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("保護されたAPIへのアクセス成功 - 正常系")
    void accessProtectedApi_return200_whenAuthenticated() throws Exception {
        // Arrange
        var loginRequestDto = new LoginRequestDto("test@email.com", "password123");

        // ログインしてJWTトークンを取得
        var result = mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        JwtAuthenticationResponseDto responseDto = objectMapper.readValue(responseBody, JwtAuthenticationResponseDto.class);
        String jwtToken = responseDto.accessToken();

        // Act & Assert
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@email.com"));
    }

    @Test
    @DisplayName("保護されたAPIへのアクセス失敗 - 異常系")
    void accessProtectedApi_return401_whenNotAuthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
