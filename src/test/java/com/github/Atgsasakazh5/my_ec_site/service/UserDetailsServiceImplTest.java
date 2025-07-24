package com.github.Atgsasakazh5.my_ec_site.service;

import com.github.Atgsasakazh5.my_ec_site.entity.User;
import com.github.Atgsasakazh5.my_ec_site.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("正しいメールアドレスでUserDetailsを返す")
    void loadUserByUsername_whenUserExists() {
        // Arrange
        String email = "test@example.com";
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail(email);
        user.setPassword("password123");
        user.setRoles(Set.of()); // ロールは空のセット

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        var userDetails = userDetailsService.loadUserByUsername(email);

        // Assert
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(email);
        assertThat(userDetails.getPassword()).isEqualTo("password123");
        assertThat(userDetails.getAuthorities()).isEmpty(); // ロールが空なので権限も空

    }

    @Test
    @DisplayName("存在しないメールアドレスでUsernameNotFoundExceptionをスローする")
    void loadUserByUsername_shouldThrowsUsernameNotFoundException_whenUserDoesNotExist() {
        // Arrange
        String email = "test@email.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("メールアドレスで登録済みユーザーが見つかりません: " + email);

    }
}