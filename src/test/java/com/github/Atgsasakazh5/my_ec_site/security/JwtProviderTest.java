package com.github.Atgsasakazh5.my_ec_site.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class JwtProviderTest {

    @Mock
    Authentication authentication;

    @Mock
    UserDetails userDetails;

    @Autowired
    JwtProvider jwtProvider;

    @BeforeEach
    void setUp(){

        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Authenticationを受け取って正しくトークンを生成するか-正常系")
    void generateToken_shouldReturnValidToken() {
        // Arrange
        String email = "test@email.com";
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(email);

        // Act
        String token = jwtProvider.generateToken(authentication);

        // Assert
        assertThat(token).isNotNull();
        assertThat(jwtProvider.validateToken(token)).isTrue();
        assertThat(jwtProvider.getUsernameFromToken(token)).isEqualTo(email);
    }

    @Test
    @DisplayName("不正なトークンをinvalidと判定するか-異常系")
    void validateToken_shouldReturnFalse_whenTokenIsInvalid() {
        // Arrange
        String token = "invalid.token";

        // Act& Assert
        assertThat(jwtProvider.validateToken(token)).isFalse();
    }
}