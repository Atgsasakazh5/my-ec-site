package com.github.Atgsasakazh5.my_ec_site.repository;

import com.github.Atgsasakazh5.my_ec_site.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("h2")
@JdbcTest
@Import(ProductDaoImpl.class)
@Transactional
class ProductDaoImplTest {

    @Autowired
    private ProductDaoImpl productDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Integer testCategoryId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO categories (name) VALUES (?);", "テストカテゴリー1");
        // 挿入したカテゴリのIDを動的に取得してフィールドに保存
        this.testCategoryId = jdbcTemplate.queryForObject(
                "SELECT id FROM categories WHERE name = 'テストカテゴリー1'", Integer.class);
    }

    @Test
    @DisplayName("商品を保存できること")
    void save_shouldSaveProduct() {
        // Arrange
        Product product = new Product();
        product.setName("テスト商品1");
        product.setPrice(1000);
        product.setDescription("テスト商品です");
        product.setImageUrl("http://example.com/image.jpg");
        product.setCategoryId(testCategoryId);

        // Act
        Product savedProduct = productDao.save(product);

        // Assert
        assertNotNull(savedProduct);
        assertNotNull(savedProduct.getId());

        var foundProduct = productDao.findById(savedProduct.getId()).orElse(null);
        assertNotNull(foundProduct);

        assertEquals("テスト商品1", savedProduct.getName());
        assertEquals(1000, savedProduct.getPrice());
        assertEquals("テスト商品です", savedProduct.getDescription());
        assertEquals("http://example.com/image.jpg", savedProduct.getImageUrl());
        assertEquals(testCategoryId, savedProduct.getCategoryId());
    }

    @Test
    @DisplayName("商品IDで商品を検索できること")
    void findById_shouldReturnProductForExistingId() {
        // Arrange
        Product product = new Product();
        product.setName("テスト商品1");
        product.setPrice(1000);
        product.setDescription("テスト商品です");
        product.setImageUrl("http://example.com/image.jpg");
        product.setCategoryId(testCategoryId);

        Product savedProduct = productDao.save(product);

        // Act
        var result = productDao.findById(savedProduct.getId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals(savedProduct.getId(), result.get().getId());
        assertEquals("テスト商品1", result.get().getName());
        assertEquals(1000, result.get().getPrice());
        assertEquals("テスト商品です", result.get().getDescription());
        assertEquals("http://example.com/image.jpg", result.get().getImageUrl());
        assertEquals(testCategoryId, result.get().getCategoryId());

        //assertThat(result.get()).usingRecursiveComparison().isEqualTo(savedProduct);
        //上記で全てのフィールドが一致することを確認できる
    }

    @Test
    @DisplayName("存在しない商品IDで商品を検索すると空のOptionalが返ること")
    void findById_shouldReturnEmptyOptionalForNonExistentId() {
        // Act
        var result = productDao.findById(999L);
        // Assert
        assertTrue(result.isEmpty(), "存在しないIDで検索した場合、空のOptionalが返るべきです");
    }

    @Test
    @DisplayName("全ての商品を取得できること")
    void findAll_shouldReturnAllProducts() {
        // Arrange
        Product product1 = new Product();
        product1.setName("テスト商品1");
        product1.setPrice(1000);
        product1.setDescription("テスト商品1の説明");
        product1.setImageUrl("http://example.com/image1.jpg");
        product1.setCategoryId(testCategoryId);

        Product product2 = new Product();
        product2.setName("テスト商品2");
        product2.setPrice(2000);
        product2.setDescription("テスト商品2の説明");
        product2.setImageUrl("http://example.com/image2.jpg");
        product2.setCategoryId(testCategoryId);

        productDao.save(product1);
        productDao.save(product2);

        // Act
        var products = productDao.findAll();

        // Assert
        assertThat(products).isNotNull();
        assertThat(products).hasSize(2);
        // 名前だけを抽出して、順序を問わず内容が一致するかを検証
        assertThat(products)
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("テスト商品1", "テスト商品2");
    }

    @Test
    @DisplayName("商品を更新できること")
    void update_shouldUpdateProduct() {
        // Arrange
        Product product = new Product();
        product.setName("テスト商品1");
        product.setPrice(1000);
        product.setDescription("テスト商品です");
        product.setImageUrl("http://example.com/image.jpg");
        product.setCategoryId(testCategoryId);

        Product savedProduct = productDao.save(product);

        // Act
        savedProduct.setName("更新された商品名");
        savedProduct.setPrice(1500);
        savedProduct.setDescription("更新された商品説明");
        savedProduct.setImageUrl("http://example.com/updated_image.jpg");
        Product updatedProduct = productDao.update(savedProduct);

        // Assert
        assertNotNull(updatedProduct);
        assertEquals(savedProduct.getId(), updatedProduct.getId());
        assertEquals("更新された商品名", updatedProduct.getName());
        assertEquals(1500, updatedProduct.getPrice());
        assertEquals("更新された商品説明", updatedProduct.getDescription());
        assertEquals("http://example.com/updated_image.jpg", updatedProduct.getImageUrl());
        assertEquals(testCategoryId, updatedProduct.getCategoryId());
    }

    @Test
    @DisplayName("商品を削除できること")
    void delete_shouldDeleteProduct() {
        // Arrange
        Product product = new Product();
        product.setName("テスト商品1");
        product.setPrice(1000);
        product.setDescription("テスト商品です");
        product.setImageUrl("http://example.com/image.jpg");
        product.setCategoryId(testCategoryId);

        Product savedProduct = productDao.save(product);

        // Act
        productDao.delete(savedProduct.getId());
        // Assert
        var result = productDao.findById(savedProduct.getId());
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("指定されたページの商品を取得できること")
    void findAll_shouldReturnPaginatedProducts() {
        // Arrange: batchUpdateで100件のテストデータを一括登録
        List<Object[]> batchArgs = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 1; i <= 100; i++) {
            batchArgs.add(new Object[]{
                    "テスト商品" + i, 1000 + i, "説明" + i, "url" + i, testCategoryId,
                    Timestamp.valueOf(now),
                    Timestamp.valueOf(now)
            });
        }
        jdbcTemplate.batchUpdate("INSERT INTO products (name, price, description, image_url, category_id, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)", batchArgs);

        // Act: 2ページ目を20件取得
        int page = 1;
        int size = 20;
        List<Product> products = productDao.findAll(page, size);

        // Assert
        assertThat(products).hasSize(size);

        // 期待される商品名のリストを作成 (80番目から61番目)
        List<String> expectedNames = IntStream.rangeClosed(61, 80)
                .mapToObj(i -> "テスト商品" + i)
                .sorted(Comparator.reverseOrder()) // 降順にソート
                .toList();

        // 実際のリストから商品名を抽出
        List<String> actualNames = products.stream().map(Product::getName).toList();

        // 順序も含めて、期待されるリストと完全に一致することを検証
        assertThat(actualNames).containsExactlyElementsOf(expectedNames);
    }

    @Test
    @DisplayName("商品数を取得できること")
    void count_shouldReturnTotalProductCount() {
        // Arrange: batchUpdateで100件のテストデータを一括登録
        List<Object[]> batchArgs = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            batchArgs.add(new Object[]{
                    "テスト商品" + i, 1000 + i, "説明" + i, "url" + i, testCategoryId,
                    Timestamp.valueOf(LocalDateTime.now()),
                    Timestamp.valueOf(LocalDateTime.now())
            });
        }

        jdbcTemplate.batchUpdate("INSERT INTO products (name, price, description, image_url, category_id, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)", batchArgs);

        // Act
        int count = productDao.countAll();

        // Assert
        assertEquals(100, count);
    }
}