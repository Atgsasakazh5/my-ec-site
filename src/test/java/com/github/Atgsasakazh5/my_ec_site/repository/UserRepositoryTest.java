package com.github.Atgsasakazh5.my_ec_site.repository;

import com.github.Atgsasakazh5.my_ec_site.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @DisplayName("ユーザー名でユーザーを検索できること")
    @Test
    void findByName_shouldReturnUser_whenNameExists() {
        // Arrange
        String name = "testUser";
        User user = new User();
        user.setName(name);
        user.setEmail("test@mail.com");
        user.setPassword("password123");
        user.setAddress("123 Test St");
        user.setSubscribingNewsletter(true);
        userRepository.save(user);

        // Act
        Optional<User> foundUser = userRepository.findByName(name);

        // Assert
        assertTrue(foundUser.isPresent(), "ユーザーが見つかるはず");
        assertEquals(name, foundUser.get().getName(), "ユーザー名が一致するはず");
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

        // Arrange
        String email = "test@mail.com";
        User user = new User();
        user.setName("testUser");
        user.setEmail(email);
        user.setPassword("password123");
        user.setAddress("123 Test St");
        user.setSubscribingNewsletter(true);
        userRepository.save(user);

        // Act
        Optional<User> foundUser = userRepository.findByEmail(email);

        // Assert
        assertTrue(foundUser.isPresent(), "ユーザーが見つかるはず");
        assertEquals(email, foundUser.get().getEmail(), "emailが一致するはず");
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

        // Arrange
        String email = "test@mail.com";
        User user = new User();
        user.setName("testUser");
        user.setEmail(email);
        user.setPassword("password123");
        user.setAddress("123 Test St");
        user.setSubscribingNewsletter(true);
        userRepository.save(user);

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