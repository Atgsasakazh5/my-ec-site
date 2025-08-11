package com.github.Atgsasakazh5.my_ec_site.repository;

import com.github.Atgsasakazh5.my_ec_site.entity.OrderDetail;
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
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("h2")
@JdbcTest
@Import(OrderDetailDaoImpl.class)
@Transactional
class OrderDetailDaoImplTest {

    @Autowired
    private OrderDetailDaoImpl orderDetailDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // テスト用の親IDを保持
    private Long testOrderId;
    private Long testSkuId1;
    private Long testSkuId2;

    @BeforeEach
    void setUp() {
        var now = LocalDateTime.now();

        jdbcTemplate.update("INSERT INTO users (id, name, email, password, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                1060L, "test user", "test@example.com", "password", now, now);

        jdbcTemplate.update("INSERT INTO categories (id, name) VALUES (?, ?)",
                501, "Test Category");

        jdbcTemplate.update("INSERT INTO products (id, name, price, description, category_id, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)",
                3001L, "Test Product", 1000, "description", 501, now, now);

        jdbcTemplate.update("INSERT INTO orders (id, user_id, status, total_price, shipping_address, shipping_postal_code, shipping_name, ordered_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                1001L, 1060L, "PENDING", 0, "Tokyo", "100-0001", "Test User", now);

        jdbcTemplate.update("INSERT INTO skus (id, product_id, size, color, extra_price, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)",
                2001L, 3001L, "M", "Red", 100, now, now);

        jdbcTemplate.update("INSERT INTO skus (id, product_id, size, color, extra_price, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)",
                2002L, 3001L, "L", "Blue", 200, now, now);

        this.testOrderId = 1001L;
        this.testSkuId1 = 2001L;
        this.testSkuId2 = 2002L;
    }

    @Test
    @DisplayName("注文詳細をバッチ保存できること")
    void save_shouldBatchInsertOrderDetails() {
        // Arrange
        var orderDetailsToSave = List.of(
                // idはnullにする
                new OrderDetail(null, testOrderId, testSkuId1, 3, 2500),
                new OrderDetail(null, testOrderId, testSkuId2, 2, 2500)
        );

        // Act
        orderDetailDao.save(orderDetailsToSave);

        // Assert
        var savedDetails = orderDetailDao.findByOrderId(testOrderId);
        assertThat(savedDetails).hasSize(2);
        assertThat(savedDetails)
                .extracting("skuId", "quantity")
                .containsExactlyInAnyOrder(
                        tuple(testSkuId1, 3),
                        tuple(testSkuId2, 2)
                );
    }

    @Test
    @DisplayName("注文IDから注文詳細を取得できること")
    void findByOrderId_shouldReturnOrderDetails() {
        // Arrange
        var orderDetailsToSave = List.of(
                new OrderDetail(null, testOrderId, testSkuId1, 3, 2500),
                new OrderDetail(null, testOrderId, testSkuId2, 2, 2500)
        );
        orderDetailDao.save(orderDetailsToSave);

        // Act
        var foundDetails = orderDetailDao.findByOrderId(testOrderId);

        // Assert
        assertThat(foundDetails).hasSize(2);
        assertThat(foundDetails)
                .extracting("orderId", "skuId", "quantity", "priceAtOrder")
                .containsExactlyInAnyOrder(
                            tuple(testOrderId, testSkuId1, 3, 2500),
                        tuple(testOrderId, testSkuId2, 2, 2500)
                );
    }
}