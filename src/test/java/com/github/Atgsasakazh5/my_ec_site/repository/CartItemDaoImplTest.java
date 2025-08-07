package com.github.Atgsasakazh5.my_ec_site.repository;

import com.github.Atgsasakazh5.my_ec_site.entity.CartItem;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("h2")
@JdbcTest
@Import(CartItemDaoImpl.class)
@Transactional
class CartItemDaoImplTest {

    @Autowired
    private CartItemDaoImpl cartItemDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long testUserId;
    private Long testCartId;
    private Long testProductId;
    private Long testSkuId1;
    private Long testSkuId2;


    @BeforeEach
    void setUp() {
        // UserとCartの準備
        jdbcTemplate.update("INSERT INTO users (name, email, password, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
                "test user", "cartTest@example.com", "password", LocalDateTime.now(), LocalDateTime.now());
        this.testUserId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, "cartTest@example.com");

        jdbcTemplate.update("INSERT INTO carts (user_id) VALUES (?)", this.testUserId);
        this.testCartId = jdbcTemplate.queryForObject("SELECT id FROM carts WHERE user_id = ?", Long.class, this.testUserId);

        // Category, Product, SKUの準備
        jdbcTemplate.update("INSERT INTO categories (name) VALUES (?)", "Test Category");
        Integer testCategoryId = jdbcTemplate.queryForObject("SELECT id FROM categories WHERE name = ?", Integer.class, "Test Category");

        jdbcTemplate.update("INSERT INTO products (name, price, description, category_id, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)",
                "Test Product", 1000, "description", testCategoryId, LocalDateTime.now(), LocalDateTime.now());
        this.testProductId = jdbcTemplate.queryForObject("SELECT id FROM products WHERE name = ?", Long.class, "Test Product");

        jdbcTemplate.update("INSERT INTO skus (product_id, size, color, extra_price, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)",
                this.testProductId, "M", "Red", 100, LocalDateTime.now(), LocalDateTime.now());
        this.testSkuId1 = jdbcTemplate.queryForObject("SELECT id FROM skus WHERE product_id = ? AND size = ? AND color = ?", Long.class, this.testProductId, "M", "Red");

        jdbcTemplate.update("INSERT INTO skus (product_id, size, color, extra_price, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)",
                this.testProductId, "L", "Blue", 200, LocalDateTime.now(), LocalDateTime.now());
        this.testSkuId2 = jdbcTemplate.queryForObject("SELECT id FROM skus WHERE product_id = ? AND size = ? AND color = ?", Long.class, this.testProductId, "L", "Blue");
    }

    @Test
    @DisplayName("カートアイテムを保存できること")
    void saveCartItem() {
        // Arrange
        var cartItem = new CartItem();
        cartItem.setCartId(testCartId);
        cartItem.setSkuId(testSkuId1);
        cartItem.setQuantity(2);

        // Act
        var savedCartItem = cartItemDao.save(cartItem);

        // Assert
        assertNotNull(savedCartItem);
        assertNotNull(savedCartItem.getId());
        assertEquals(testCartId, savedCartItem.getCartId());
        assertEquals(testSkuId1, savedCartItem.getSkuId());
        assertEquals(2, savedCartItem.getQuantity());

        // データベースに保存されていることを確認
        var foundCartItem = cartItemDao.findById(savedCartItem.getId());
        assertTrue(foundCartItem.isPresent());
        assertEquals(savedCartItem, foundCartItem.get());
    }

    @Test
    @DisplayName("cartitemをIDで取得できること")
    void findById() {
        // Arrange
        var cartItem = new CartItem();
        cartItem.setCartId(testCartId);
        cartItem.setSkuId(testSkuId1);
        cartItem.setQuantity(2);
        var savedCartItem = cartItemDao.save(cartItem);

        // Act
        var foundCartItem = cartItemDao.findById(savedCartItem.getId());

        // Assert
        assertTrue(foundCartItem.isPresent());
        assertEquals(savedCartItem, foundCartItem.get());
    }

    @Test
    @DisplayName("カートIDとSKU IDでカートアイテムを取得できること")
    void findByCartIdAndSkuId() {
        // Arrange
        var cartItem = new CartItem();
        cartItem.setCartId(testCartId);
        cartItem.setSkuId(testSkuId1);
        cartItem.setQuantity(3);

        var savedCartItem = cartItemDao.save(cartItem);

        // Act
        var foundCartItem = cartItemDao.findByCartIdAndSkuId(testCartId, testSkuId1);

        // Assert
        assertTrue(foundCartItem.isPresent());
        assertEquals(savedCartItem.getId(), foundCartItem.get().getId());
        assertEquals(testCartId, foundCartItem.get().getCartId());
        assertEquals(testSkuId1, foundCartItem.get().getSkuId());
        assertEquals(3, foundCartItem.get().getQuantity());

    }

    @Test
    @DisplayName("カートIDでカートアイテムのリストを取得できること")
    void findByCartId_shouldReturnListOfCartItems() {
        // Arrange
        var cartItem1 = new CartItem();
        cartItem1.setCartId(testCartId);
        cartItem1.setSkuId(testSkuId1);
        cartItem1.setQuantity(2);
        cartItemDao.save(cartItem1);

        var cartItem2 = new CartItem();
        cartItem2.setCartId(testCartId);
        cartItem2.setSkuId(testSkuId2);
        cartItem2.setQuantity(3);
        cartItemDao.save(cartItem2);

        // Act
        var foundCartItems = cartItemDao.findByCartId(testCartId);

        // Assert
        assertThat(foundCartItems)
                .isNotNull()
                .hasSize(2)
                .extracting(CartItem::getSkuId)
                .containsExactlyInAnyOrder(testSkuId1, testSkuId2);
    }

    @Test
    @DisplayName("カートアイテムを削除できること")
    void deleteById() {
        // Arrange
        var cartItem = new CartItem();
        cartItem.setCartId(testCartId);
        cartItem.setSkuId(testSkuId1);
        cartItem.setQuantity(2);
        var savedCartItem = cartItemDao.save(cartItem);

        // Act
        cartItemDao.deleteById(savedCartItem.getId());

        // Assert
        var foundCartItem = cartItemDao.findById(savedCartItem.getId());
        assertFalse(foundCartItem.isPresent());
    }

    @Test
    @DisplayName("カートアイテムの数量を更新できること")
    void updateCartItem() {
        // Arrange
        var cartItem = new CartItem();
        cartItem.setCartId(testCartId);
        cartItem.setSkuId(testSkuId1);
        cartItem.setQuantity(2);
        var savedCartItem = cartItemDao.save(cartItem);

        // Act
        savedCartItem.setQuantity(5);
        var updatedCartItem = cartItemDao.update(savedCartItem);

        // Assert
        assertNotNull(updatedCartItem);
        assertEquals(5, updatedCartItem.getQuantity());
        assertEquals(savedCartItem.getId(), updatedCartItem.getId());
        assertEquals(testCartId, updatedCartItem.getCartId());
        assertEquals(testSkuId1, updatedCartItem.getSkuId());

        // データベースに保存されていることを確認
        var foundCartItem = cartItemDao.findById(updatedCartItem.getId());
        assertTrue(foundCartItem.isPresent());
        assertEquals(5, foundCartItem.get().getQuantity());
    }

    @Test
    @DisplayName("カートIDでcartitemdtoのリストを取得できること")
    void findCartItemDtoByCartId() {
        // Arrange
        var cartItem1 = new CartItem();
        cartItem1.setCartId(testCartId);
        cartItem1.setSkuId(testSkuId1);
        cartItem1.setQuantity(2);
        cartItemDao.save(cartItem1);

        var cartItem2 = new CartItem();
        cartItem2.setCartId(testCartId);
        cartItem2.setSkuId(testSkuId2);
        cartItem2.setQuantity(3);
        cartItemDao.save(cartItem2);

        // Act
        var foundCartItems = cartItemDao.findDetailedItemsByCartId(testCartId);

        // Assert
        assertThat(foundCartItems)
                .isNotNull()
                .hasSize(2);
        assertThat(foundCartItems)
                .extracting("skuId", "quantity", "productName", "price")
                .containsExactlyInAnyOrder(
                        tuple(testSkuId1, 2, "Test Product", 1100),
                        tuple(testSkuId2, 3, "Test Product", 1200)
                );
    }

}