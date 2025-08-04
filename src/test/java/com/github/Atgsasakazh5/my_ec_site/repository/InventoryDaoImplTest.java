package com.github.Atgsasakazh5.my_ec_site.repository;

import com.github.Atgsasakazh5.my_ec_site.entity.Inventory;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("h2")
@JdbcTest
@Import(InventoryDaoImpl.class)
@Transactional
class InventoryDaoImplTest {

    @Autowired
    private InventoryDaoImpl inventoryDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long testSkuId;
    private Long testSkuId2;

    @BeforeEach
    void setUp() {
        var now = LocalDateTime.now();
        // テスト用のカテゴリ、商品、SKUデータを挿入
        jdbcTemplate.update("INSERT INTO categories (name) VALUES (?);", "テストカテゴリー1");
        Integer testCategoryId = jdbcTemplate.queryForObject(
                "SELECT id FROM categories WHERE name = 'テストカテゴリー1'", Integer.class);

        jdbcTemplate.update("INSERT INTO products (name, price, description, image_url, category_id, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?);",
                "テスト商品", 1000, "テスト商品説明", "http://example.com/image.jpg", testCategoryId, now, now);
        Long testProductId = jdbcTemplate.queryForObject(
                "SELECT id FROM products WHERE name = 'テスト商品'", Long.class);

        jdbcTemplate.update("INSERT INTO skus (product_id, size, color, extra_price, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?);",
                testProductId, "M", "Red", 100, now, now);
        testSkuId = jdbcTemplate.queryForObject(
                "SELECT id FROM skus WHERE product_id = ? AND size = ? AND color = ?", Long.class, testProductId, "M", "Red");

        jdbcTemplate.update("INSERT INTO skus (product_id, size, color, extra_price, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?);",
                testProductId, "M", "Blue", 100, now, now);
        testSkuId2 = jdbcTemplate.queryForObject(
                "SELECT id FROM skus WHERE product_id = ? AND size = ? AND color = ?", Long.class, testProductId, "M", "Blue");
    }

    @Test
    @DisplayName("在庫を保存できること")
    void save_shouldSaveInventory() {
        // Arrange
        var inventory = new Inventory();
        inventory.setSkuId(testSkuId);
        inventory.setQuantity(10);

        // Act
        Inventory savedInventory = inventoryDao.save(inventory);

        // Assert
        assertNotNull(savedInventory);
        assertNotNull(savedInventory.getId());
        assertEquals(testSkuId, savedInventory.getSkuId());
        assertEquals(10, savedInventory.getQuantity());
        assertNotNull(savedInventory.getUpdatedAt());
    }

    @Test
    @DisplayName("在庫を更新できること")
    void update_shouldUpdateInventory() {
        // Arrange
        var inventory = new Inventory();
        inventory.setSkuId(testSkuId);
        inventory.setQuantity(10);
        inventory.setUpdatedAt(LocalDateTime.now());
        Inventory savedInventory = inventoryDao.save(inventory);

        // Act
        savedInventory.setQuantity(20);
        Inventory updatedInventory = inventoryDao.update(savedInventory);

        // Assert
        assertNotNull(updatedInventory);
        assertEquals(savedInventory.getId(), updatedInventory.getId());
        assertEquals(20, updatedInventory.getQuantity());
    }

    @Test
    @DisplayName("SKU IDで在庫を検索できること")
    void findBySkuId_shouldReturnInventory() {
        // Arrange
        var inventory = new Inventory();
        inventory.setSkuId(testSkuId);
        inventory.setQuantity(10);
        inventory.setUpdatedAt(LocalDateTime.now());
        inventoryDao.save(inventory);

        // Act
        var foundInventory = inventoryDao.findBySkuId(testSkuId);

        // Assert
        assertTrue(foundInventory.isPresent());
        assertEquals(testSkuId, foundInventory.get().getSkuId());
        assertEquals(10, foundInventory.get().getQuantity());
    }

    @Test
    @DisplayName("SKU IDで在庫が存在しない場合は空を返すこと")
    void findBySkuId_shouldReturnEmptyWhenNotFound() {
        // Act
        var foundInventory = inventoryDao.findBySkuId(999L);

        // Assert
        assertTrue(foundInventory.isEmpty());
    }

    @Test
    @DisplayName("在庫をIDで検索できること")
    void findById_shouldReturnInventory() {
        // Arrange
        var inventory = new Inventory();
        inventory.setSkuId(testSkuId);
        inventory.setQuantity(10);
        inventory.setUpdatedAt(LocalDateTime.now());
        Inventory savedInventory = inventoryDao.save(inventory);

        // Act
        var foundInventory = inventoryDao.findById(savedInventory.getId());

        // Assert
        assertTrue(foundInventory.isPresent());
        assertEquals(savedInventory.getId(), foundInventory.get().getId());
        assertEquals(testSkuId, foundInventory.get().getSkuId());
        assertEquals(10, foundInventory.get().getQuantity());
    }

    @Test
    @DisplayName("IDで在庫が存在しない場合は空を返すこと")
    void findById_shouldReturnEmptyWhenNotFound() {
        // Act
        var foundInventory = inventoryDao.findById(999L); // 存在しないID

        // Assert
        assertTrue(foundInventory.isEmpty());
    }

    @Test
    @DisplayName("SKU IDで在庫を削除できること")
    void deleteBySkuId_shouldDeleteInventory() {
        // Arrange
        var inventory = new Inventory();
        inventory.setSkuId(testSkuId);
        inventory.setQuantity(10);
        inventory.setUpdatedAt(LocalDateTime.now());
        Inventory savedInventory = inventoryDao.save(inventory);

        // Act
        inventoryDao.deleteBySkuId(testSkuId);

        // Assert
        var foundInventory = inventoryDao.findBySkuId(testSkuId);
        assertTrue(foundInventory.isEmpty());
    }

    @Test
    @DisplayName("複数のSKU IDで在庫を削除できること")
    void deleteBySkuIds_shouldDeleteInventories() {
        // Arrange
        var inventory1 = new Inventory();
        inventory1.setSkuId(testSkuId);
        inventory1.setQuantity(10);
        inventory1.setUpdatedAt(LocalDateTime.now());
        Inventory savedInventory1 = inventoryDao.save(inventory1);

        var inventory2 = new Inventory();
        inventory2.setSkuId(testSkuId2);
        inventory2.setQuantity(5);
        inventory2.setUpdatedAt(LocalDateTime.now());
        Inventory savedInventory2 = inventoryDao.save(inventory2);

        // Act
        inventoryDao.deleteBySkuIds(List.of(testSkuId, testSkuId2));

        // Assert
        var foundInventory1 = inventoryDao.findBySkuId(testSkuId);
        var foundInventory2 = inventoryDao.findBySkuId(testSkuId2);
        assertTrue(foundInventory1.isEmpty());
        assertTrue(foundInventory2.isEmpty());
    }

    @Test
    @DisplayName("SKU IDのリストで在庫を検索できること")
    void findBySkuIdIn_shouldReturnInventories() {
        // Arrange
        var inventory1 = new Inventory();
        inventory1.setSkuId(testSkuId);
        inventory1.setQuantity(10);
        inventory1.setUpdatedAt(LocalDateTime.now());
        inventoryDao.save(inventory1);

        var inventory2 = new Inventory();
        inventory2.setSkuId(testSkuId2);
        inventory2.setQuantity(5);
        inventory2.setUpdatedAt(LocalDateTime.now());
        inventoryDao.save(inventory2);

        // Act
        List<Inventory> foundInventories = inventoryDao.findBySkuIdIn(List.of(testSkuId, testSkuId2));

        // Assert
        assertThat(foundInventories).hasSize(2);
        assertThat(foundInventories).extracting("skuId").containsExactlyInAnyOrder(testSkuId, testSkuId2);
    }
}