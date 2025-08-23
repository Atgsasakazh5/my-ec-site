package com.github.Atgsasakazh5.my_ec_site.repository;

import com.github.Atgsasakazh5.my_ec_site.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@Import(UserRepository.class)
@ActiveProfiles("h2")
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    protected Long testUserId = 1L;
    protected String name = "testuser";
    protected String email = "test@email.com";

    @DisplayName("ユーザー名でユーザーを検索できること")
    @Test
    void findByName_shouldReturnUser_whenNameExists() {

        // Act
        Optional<User> foundUserOpt = userRepository.findByName(name);

        // Assert
        assertThat(foundUserOpt).isPresent();
        User foundUser = foundUserOpt.get();
        assertThat(foundUser.getName()).isEqualTo(name);

        assertThat(foundUser.getRoles()).isNotNull();
        assertThat(foundUser.getRoles()).hasSize(1);
        assertThat(foundUser.getRoles())
                .extracting(role -> role.getName().name()) // RoleオブジェクトからEnum名(String)を抽出
                .contains("ROLE_USER");
    }

    @DisplayName("ユーザー名が見つからなければ空のOptionalを返すこと")
    @Test
    void findByName_shouldReturnEmpty_whenNameNotExists() {
        // Arrange
        String name = "nonExistentUser";

        // Act
        Optional<User> foundUser = userRepository.findByName(name);

        // Assert
        assertThat(foundUser).isEmpty();
    }

    @DisplayName("メールアドレスでユーザーを検索できること")
    @Test
    void findByEmail_shouldReturnUser_whenEmailExists() {

        // Act
        Optional<User> foundUserOpt = userRepository.findByEmail(email);

        // Assert
        assertTrue(foundUserOpt.isPresent(), "ユーザーが見つかるはず");
        assertEquals(email, foundUserOpt.get().getEmail(), "emailが一致するはず");

        User foundUser = foundUserOpt.get();
        assertThat(foundUser.getRoles()).isNotNull();
        assertThat(foundUser.getRoles()).hasSize(1);
        assertThat(foundUser.getRoles())
                .extracting(role -> role.getName().name()) // RoleオブジェクトからEnum名(String)を抽出
                .contains("ROLE_USER");
    }

    @DisplayName("Emailが見つからなければ空のOptionalを返すこと")
    @Test
    void findByEmail_shouldReturnEmpty_whenEmailNotExists() {
        // Arrange
        String email = "nonExistentEmail";

        // Act
        Optional<User> foundUser = userRepository.findByEmail(email);

        // Assert
        assertThat(foundUser).isEmpty();
    }

    @DisplayName("存在するEmailでチェックした場合、trueが返ること")
    @Test
    void existsByEmail_shouldReturnTrue_whenEmailExists() {
        // Act
        boolean exists = userRepository.existsByEmail(email);

        // Assert
        assertTrue(exists, "Emailが存在する場合、trueを返すはず");
    }

    @DisplayName("存在しないEmailでチェックした場合、falseが返ること")
    @Test
    void existsByEmail_shouldReturnFalse_whenEmailNotExists() {
        // Arrange
        String email = "nonExistentEmail";

        // Act
        boolean exists = userRepository.existsByEmail(email);

        // Assert
        assertFalse(exists, "Emailが存在しない場合、falseを返すはず");
    }
}