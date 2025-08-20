package com.github.Atgsasakazh5.my_ec_site.repository;

import com.github.Atgsasakazh5.my_ec_site.entity.Sku;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("h2")
@JdbcTest
@Import(SkuDaoImpl.class)
@Transactional
class SkuDaoImplTest {

    @Autowired
    private SkuDaoImpl skuDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long testProductId;

    @BeforeEach
    void setUp() {
        // テスト用のカテゴリデータを挿入
        jdbcTemplate.update("INSERT INTO categories (name) VALUES (?);", "テストカテゴリー1");
        // 挿入したカテゴリのIDを動的に取得してフィールドに保存
        Integer testCategoryId = jdbcTemplate.queryForObject(
                "SELECT id FROM categories WHERE name = 'テストカテゴリー1'", Integer.class);
        // テスト用の商品データを挿入
        jdbcTemplate.update("INSERT INTO products (name, price, description, image_url, category_id, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?);",
                "テスト商品", 1000, "テスト商品説明", "http://example.com/image.jpg", testCategoryId, LocalDateTime.now(), LocalDateTime.now());
        // 挿入した商品のIDを動的に取得してフィールドに保存
        this.testProductId = jdbcTemplate.queryForObject(
                "SELECT id FROM products WHERE name = 'テスト商品'", Long.class);
    }

    @Test
    @DisplayName("SKUを保存できること")
    void save_shouldSaveSku() {
        // Arrange
        Sku sku = new Sku();
        sku.setProductId(testProductId);
        sku.setSize("M");
        sku.setColor("Red");
        sku.setExtraPrice(100);

        // Act
        Sku savedSku = skuDao.save(sku);

        // Assert
        assertNotNull(savedSku.getId());
        assertEquals(testProductId, savedSku.getProductId());
        assertEquals("M", savedSku.getSize());
        assertEquals("Red", savedSku.getColor());
        assertEquals(100, savedSku.getExtraPrice());
        assertNotNull(savedSku.getCreatedAt());
        assertNotNull(savedSku.getUpdatedAt());
    }

    @Test
    @DisplayName("同じ商品ID、サイズ、色のSKUを保存すると例外が発生すること")
    void save_shouldThrowExceptionForDuplicateSku() {
        // Arrange
        Sku sku = new Sku();
        sku.setProductId(testProductId);
        sku.setSize("M");
        sku.setColor("Red");
        sku.setExtraPrice(100);

        skuDao.save(sku); // 最初の保存

        // Act & Assert
        Sku duplicateSku = new Sku();
        duplicateSku.setProductId(testProductId);
        duplicateSku.setSize("M");
        duplicateSku.setColor("Red");
        duplicateSku.setExtraPrice(150);
        assertThrows(DataIntegrityViolationException.class, () -> {
            skuDao.save(duplicateSku); // 重複保存
        });
    }

    @Test
    @DisplayName("SKUをIDで検索できること")
    void findById_shouldReturnSku() {
        // Arrange
        Sku sku = new Sku();
        sku.setProductId(testProductId);
        sku.setSize("M");
        sku.setColor("Red");
        sku.setExtraPrice(100);
        Sku savedSku = skuDao.save(sku);

        // Act
        var foundSku = skuDao.findById(savedSku.getId());

        // Assert
        assertTrue(foundSku.isPresent());
        assertEquals(savedSku.getId(), foundSku.get().getId());
        assertEquals(testProductId, foundSku.get().getProductId());
        assertEquals("M", foundSku.get().getSize());
        assertEquals("Red", foundSku.get().getColor());
        assertEquals(100, foundSku.get().getExtraPrice());
        assertNotNull(foundSku.get().getCreatedAt());
        assertNotNull(foundSku.get().getUpdatedAt());
    }

    @Test
    @DisplayName("SKUを存在しないIDで検索すると空のOptionalが返ること")
    void findById_shouldReturnEmptyOptionalForNonExistentId() {
        // Act
        var result = skuDao.findById(999L); // 存在しないID

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("SKUを商品IDで検索できること")
    void findByProductId_shouldReturnSkus() {
        // Arrange
        Sku sku1 = new Sku();
        sku1.setProductId(testProductId);
        sku1.setSize("M");
        sku1.setColor("Red");
        sku1.setExtraPrice(100);
        skuDao.save(sku1);

        Sku sku2 = new Sku();
        sku2.setProductId(testProductId);
        sku2.setSize("L");
        sku2.setColor("Blue");
        sku2.setExtraPrice(200);
        skuDao.save(sku2);

        // Act
        var skus = skuDao.findByProductId(testProductId);

        // Assert
        assertEquals(2, skus.size());
        assertTrue(skus.stream().anyMatch(sku -> sku.getSize().equals("M") && sku.getColor().equals("Red")));
        assertTrue(skus.stream().anyMatch(sku -> sku.getSize().equals("L") && sku.getColor().equals("Blue")));
    }

    @Test
    @DisplayName("SKUを更新できること")
    void update_shouldUpdateSku() {
        // Arrange
        Sku sku = new Sku();
        sku.setProductId(testProductId);
        sku.setSize("M");
        sku.setColor("Red");
        sku.setExtraPrice(100);
        Sku savedSku = skuDao.save(sku);

        // Act
        savedSku.setSize("L");
        savedSku.setColor("Blue");
        savedSku.setExtraPrice(150);
        Sku updatedSku = skuDao.update(savedSku);

        // Assert
        assertEquals(savedSku.getId(), updatedSku.getId());
        assertEquals("L", updatedSku.getSize());
        assertEquals("Blue", updatedSku.getColor());
        assertEquals(150, updatedSku.getExtraPrice());
        assertNotNull(updatedSku.getUpdatedAt());
    }

    @Test
    @DisplayName("SKUを削除できること")
    void delete_shouldDeleteSku() {
        // Arrange
        Sku sku = new Sku();
        sku.setProductId(testProductId);
        sku.setSize("M");
        sku.setColor("Red");
        sku.setExtraPrice(100);
        Sku savedSku = skuDao.save(sku);

        // Act
        skuDao.delete(savedSku.getId());

        // Assert
        var result = skuDao.findById(savedSku.getId());
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("SKUを商品IDで削除できること")
    void deleteByProductId_shouldDeleteSkus() {
        // Arrange
        Sku sku1 = new Sku();
        sku1.setProductId(testProductId);
        sku1.setSize("M");
        sku1.setColor("Red");
        sku1.setExtraPrice(100);
        skuDao.save(sku1);

        Sku sku2 = new Sku();
        sku2.setProductId(testProductId);
        sku2.setSize("L");
        sku2.setColor("Blue");
        sku2.setExtraPrice(200);
        skuDao.save(sku2);

        // Act
        skuDao.deleteByProductId(testProductId);

        // Assert
        var skus = skuDao.findByProductId(testProductId);
        assertTrue(skus.isEmpty());
    }

    @Test
    @DisplayName("SKUを商品ID、サイズ、色で検索できること")
    void findByProductIdAndSizeAndColor_shouldReturnSku() {
        // Arrange
        Sku sku = new Sku();
        sku.setProductId(testProductId);
        sku.setSize("M");
        sku.setColor("Red");
        sku.setExtraPrice(100);
        Sku savedSku = skuDao.save(sku);

        // Act
        var foundSku = skuDao.findByProductIdAndSizeAndColor(testProductId, "M", "Red");

        // Assert
        assertTrue(foundSku.isPresent());
        assertEquals(savedSku.getId(), foundSku.get().getId());
        assertEquals(testProductId, foundSku.get().getProductId());
        assertEquals("M", foundSku.get().getSize());
        assertEquals("Red", foundSku.get().getColor());
        assertEquals(100, foundSku.get().getExtraPrice());
        assertNotNull(foundSku.get().getCreatedAt());
        assertNotNull(foundSku.get().getUpdatedAt());
    }

    @Test
    @DisplayName("SKUを商品ID、サイズ、色で検索すると存在しない場合は空のOptionalが返ること")
    void findByProductIdAndSizeAndColor_shouldReturnEmptyOptionalForNonExistentSku() {
        // Act
        var result = skuDao.findByProductIdAndSizeAndColor(testProductId, "XL", "Green");
        // Assert
        assertTrue(result.isEmpty());
    }
}