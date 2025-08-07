package com.github.Atgsasakazh5.my_ec_site.repository;

import com.github.Atgsasakazh5.my_ec_site.entity.Cart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("h2")
@JdbcTest
@Import(CartDaoImpl.class)
@Transactional
class CartDaoImplTest {

    @Autowired
    private CartDaoImpl cartDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long userId;

    @BeforeEach
    void setUp() {
        // テスト用のデータをセットアップ
        jdbcTemplate.update("INSERT INTO users\n" +
                        "    (id, name, email, password, address, subscribing_newsletter, created_at, updated_at)\n" +
                        "VALUES\n" +
                        "    (1010, ?, ?, ?,\n" +
                        "     ?, ?, '2023-10-01 12:00:00', '2023-10-01 12:00:00');",
                "cartTestUser", "testuser01@email.com", "$2a$10$DlinxRyxBS2JQbTVSYWKEOGxT5nJ7wZZd/MGpz72O4imXgGbLYdhC",
                "Tokyo", true);
        userId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE email = ?", Long.class, "testuser01@email.com");
    }

    @Test
    @DisplayName("カートを保存できること")
    void saveCart() {
        // act
        var savedCart = cartDao.saveCart(userId);

        // assert
        assertThat(savedCart).isNotNull();
        assertThat(savedCart.getId()).isNotNull();
        assertThat(savedCart.getUserId()).isEqualTo(userId);

        // カートがデータベースに保存されていることを確認
        var foundCart = cartDao.findCartByUserId(userId);
        assertThat(foundCart).isPresent();
        assertThat(foundCart.get().getId()).isEqualTo(savedCart.getId());

    }

    @Test
    @DisplayName("ユーザーIDからカートを取得できること")
    void findCartByUserId() {
        // arrange
        var savedCart = cartDao.saveCart(userId);

        // act
        var foundCart = cartDao.findCartByUserId(userId);

        // assert
        assertThat(foundCart).isPresent();
        assertThat(foundCart.get().getId()).isEqualTo(savedCart.getId());
        assertThat(foundCart.get().getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("メールアドレスからカートを取得できること")
    void findCartByEmail() {
        // arrange
        var savedCart = cartDao.saveCart(userId);

        // act
        var foundCart = cartDao.findCartByEmail("testuser01@email.com");

        // assert
        assertThat(foundCart).isPresent();
        assertThat(foundCart.get().getId()).isEqualTo(savedCart.getId());
        assertThat(foundCart.get().getUserId()).isEqualTo(userId);
    }

}