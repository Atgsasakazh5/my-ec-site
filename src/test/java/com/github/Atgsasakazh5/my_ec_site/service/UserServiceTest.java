package com.github.Atgsasakazh5.my_ec_site.service;

import com.github.Atgsasakazh5.my_ec_site.dto.SignUpRequestDto;
import com.github.Atgsasakazh5.my_ec_site.dto.UserDto;
import com.github.Atgsasakazh5.my_ec_site.entity.Role;
import com.github.Atgsasakazh5.my_ec_site.entity.RoleName;
import com.github.Atgsasakazh5.my_ec_site.entity.User;
import com.github.Atgsasakazh5.my_ec_site.entity.VerificationToken;
import com.github.Atgsasakazh5.my_ec_site.repository.CartDao;
import com.github.Atgsasakazh5.my_ec_site.repository.RoleRepository;
import com.github.Atgsasakazh5.my_ec_site.repository.UserRepository;
import com.github.Atgsasakazh5.my_ec_site.repository.VerificationTokenDao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("h2")
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private CartDao cartDao;

    @Mock
    private VerificationTokenDao verificationTokenDao;

    @Mock
    private EmailService emailService;

    @Test
    @DisplayName("ユーザー登録のテスト-正常系")
    void register_shouldSaveUserAndReturnUserDto_whenEmailIsUnique() {
        // Arrange
        SignUpRequestDto signUpRequestDto = new SignUpRequestDto(
                "testuser",
                "test@email.com",
                "password123",
                "Tokyo",
                true);

        // テスト用のRoleオブジェクト
        Role userRole = new Role(1, RoleName.ROLE_USER);

        // モックの振る舞いを定義
        when(userRepository.existsByEmail(signUpRequestDto.email())).thenReturn(false);
        when(roleRepository.findByName(RoleName.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(signUpRequestDto.password())).thenReturn("hashedPassword");

        // userRepository.save()が呼ばれたときの振る舞いを定義
        // any()マッチャーで、どんなUserオブジェクトが来ても対応できるようにする
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            userToSave.setId(1L);
            return userToSave;
        });

        // Act
        UserDto result = userService.register(signUpRequestDto);

        // Assert
        // userRepository.save()が1回呼ばれたことを確認
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userArgumentCaptor.capture());

        // cartDao.saveCart()が1回呼ばれたことを確認
        verify(cartDao).saveCart(1L);

        // saveに渡されたUserオブジェクトを検証
        User savedUser = userArgumentCaptor.getValue();
        assertThat(savedUser.getName()).isEqualTo(signUpRequestDto.name());
        assertThat(savedUser.getEmail()).isEqualTo(signUpRequestDto.email());
        assertThat(savedUser.getPassword()).isEqualTo("hashedPassword");
        assertThat(savedUser.getRoles()).contains(userRole);
        assertThat(savedUser.isEmailVerified()).isFalse();

        // verificationTokenDao.save()が1回呼ばれたことを確認
        verify(verificationTokenDao).save(any(com.github.Atgsasakazh5.my_ec_site.entity.VerificationToken.class));

        // emailService.sendVerificationEmail()が1回呼ばれたことを確認
        verify(emailService).sendVerificationEmail(eq(signUpRequestDto.email()), anyString());

        // 戻り値のUserDtoを検証
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo(signUpRequestDto.name());
    }

    @Test
    @DisplayName("ユーザー登録のテスト-異常系: メールアドレスが重複している場合")
    void register_shouldThrowException_whenEmailExists() {
        // Arrange
        SignUpRequestDto dto = new SignUpRequestDto(
                "testuser",
                "test@email.com",
                "password123",
                "Tokyo",
                true);

        // userRepository.existsByEmailが呼ばれたらtrueを返すように設定
        when(userRepository.existsByEmail(dto.email())).thenReturn(true);

        // Act /Assert
        assertThatThrownBy(() -> userService.register(dto))
                .isInstanceOf(IllegalStateException.class) // スローされた例外の型を検証
                .hasMessage("メールアドレスはすでに使用されています"); // スローされた例外のメッセージを検証

        // Assert
        // 例外がスローされた後、他のリポジトリのメソッドが一切呼ばれていないことを確認
        verify(roleRepository, never()).findByName(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("ユーザー認証のテスト-正常系")
    void verifyUser_shouldSetEmailVerifiedToTrue_whenTokenIsValid() {
        // Arrange
        String token = "valid-token";
        VerificationToken verificationToken = new VerificationToken(1L, token, 1L, java.time.LocalDateTime.now().plusHours(1));
        User user = new User();
        user.setId(1L);
        user.setEmailVerified(false);

        when(verificationTokenDao.findByToken(token)).thenReturn(Optional.of(verificationToken));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        userService.verifyUser(token);

        // Assert
        assertThat(user.isEmailVerified()).isTrue();
        verify(userRepository).update(user);
        verify(verificationTokenDao).delete(verificationToken);
    }

    @Test
    @DisplayName("ユーザー認証のテスト-異常系: 無効なトークン")
    void verifyUser_shouldThrowException_whenTokenIsInvalid() {
        // Arrange
        String token = "invalid-token";
        when(verificationTokenDao.findByToken(token)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.verifyUser(token))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("無効なトークンです");
    }

    @Test
    @DisplayName("ユーザー認証のテスト-異常系: 期限切れのトークン")
    void verifyUser_shouldThrowException_whenTokenIsExpired() {
        // Arrange
        String token = "expired-token";
        VerificationToken verificationToken = new VerificationToken(1L, token, 1L, java.time.LocalDateTime.now().minusHours(1));

        when(verificationTokenDao.findByToken(token)).thenReturn(Optional.of(verificationToken));

        // Act & Assert
        assertThatThrownBy(() -> userService.verifyUser(token))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("トークンの有効期限が切れています");
    }
}