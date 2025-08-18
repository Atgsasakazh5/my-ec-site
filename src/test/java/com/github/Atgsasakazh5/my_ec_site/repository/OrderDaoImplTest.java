package com.github.Atgsasakazh5.my_ec_site.repository;

import com.github.Atgsasakazh5.my_ec_site.dto.OrderSummaryDto;
import com.github.Atgsasakazh5.my_ec_site.entity.Order;
import com.github.Atgsasakazh5.my_ec_site.entity.OrderStatus;
import com.github.Atgsasakazh5.my_ec_site.exception.ResourceNotFoundException;
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
@Import(OrderDaoImpl.class)
@Transactional
class OrderDaoImplTest {

    @Autowired
    private OrderDaoImpl orderDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private long userId = 1050L;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update(
                "INSERT INTO users (id, name, email, password, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)",
                userId, "Test User", "test@example.com", "password", LocalDateTime.now(), LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("注文を保存できること")
    void saveOrder() {

        // Arrange
        OrderStatus status = OrderStatus.PENDING;
        int totalPrice = 1000;
        String shippingAddress = "Tokyo, Japan";
        String postalCode = "100-0001";
        String shippingName = "Test User";

        var order = new Order();
        order.setUserId(userId);
        order.setStatus(status);
        order.setTotalPrice(totalPrice);
        order.setShippingAddress(shippingAddress);
        order.setPostalCode(postalCode);
        order.setShippingName(shippingName);

        // Act
        var savedOrder = orderDao.saveOrder(order);

        // Assert
        assertNotNull(savedOrder);
        assertNotNull(savedOrder.getId());
        assertEquals(userId, savedOrder.getUserId());
        assertEquals(status, savedOrder.getStatus());
        assertEquals(totalPrice, savedOrder.getTotalPrice());
        assertEquals(shippingAddress, savedOrder.getShippingAddress());
        assertEquals(postalCode, savedOrder.getPostalCode());
        assertEquals(shippingName, savedOrder.getShippingName());
        assertNotNull(savedOrder.getOrderedAt());

    }

    @Test
    @DisplayName("orderIdから注文を取得できること")
    void findOrderById() {
        // Arrange
        var status = OrderStatus.PENDING;
        int totalPrice = 1000;
        String shippingAddress = "Tokyo, Japan";
        String postalCode = "100-0001";
        String shippingName = "Test User";

        var order = new Order();
        order.setUserId(userId);
        order.setStatus(status);
        order.setTotalPrice(totalPrice);
        order.setShippingAddress(shippingAddress);
        order.setPostalCode(postalCode);
        order.setShippingName(shippingName);

        var savedOrder = orderDao.saveOrder(order);

        // Act
        var foundOrder = orderDao.findOrderById(savedOrder.getId());

        // Assert
        assertTrue(foundOrder.isPresent());
        assertEquals(savedOrder.getId(), foundOrder.get().getId());
        assertEquals(userId, foundOrder.get().getUserId());
        assertEquals(status, foundOrder.get().getStatus());
        assertEquals(totalPrice, foundOrder.get().getTotalPrice());
        assertEquals(shippingAddress, foundOrder.get().getShippingAddress());
        assertEquals(postalCode, foundOrder.get().getPostalCode());
        assertEquals(shippingName, foundOrder.get().getShippingName());
        assertNotNull(foundOrder.get().getOrderedAt());
    }

    @Test
    @DisplayName("ユーザーIDから全ての注文を取得できること")
    void findAllOrdersByUserId() {
        // Arrange
        var status = OrderStatus.PENDING;
        int totalPrice = 1000;
        String shippingAddress = "Tokyo, Japan";
        String postalCode = "100-0001";
        String shippingName = "Test User";

        var order1 = new Order();
        order1.setUserId(userId);
        order1.setStatus(status);
        order1.setTotalPrice(totalPrice);
        order1.setShippingAddress(shippingAddress);
        order1.setPostalCode(postalCode);
        order1.setShippingName(shippingName);

        var savedOrder1 = orderDao.saveOrder(order1);

        var order2 = new Order();
        order2.setUserId(userId);
        order2.setStatus(OrderStatus.DELIVERED);
        order2.setTotalPrice(2000);
        order2.setShippingAddress("Osaka, Japan");
        order2.setPostalCode("530-0001");
        order2.setShippingName("Another User");

        var savedOrder2 = orderDao.saveOrder(order2);

        // Act
        var orders = orderDao.findAllOrdersByUserId(userId);

        // Assert
        assertThat(orders)
                .isNotNull()
                .hasSize(2);

        assertThat(orders)
                .usingRecursiveFieldByFieldElementComparator() // オブジェクトの中身を比較
                .containsExactlyInAnyOrder(savedOrder1, savedOrder2);
    }

    @Test
    @DisplayName("注文情報を更新できること")
    void updateOrder_shouldUpdateOrder_whenOrderExists() {
        // Arrange
        var status = OrderStatus.PENDING;
        int totalPrice = 1000;
        String shippingAddress = "Tokyo, Japan";
        String postalCode = "100-0001";
        String shippingName = "Test User";

        var order1 = new Order();
        order1.setUserId(userId);
        order1.setStatus(status);
        order1.setTotalPrice(totalPrice);
        order1.setShippingAddress(shippingAddress);
        order1.setPostalCode(postalCode);
        order1.setShippingName(shippingName);

        var existingOrder = orderDao.saveOrder(order1);

        existingOrder.setStatus(OrderStatus.SHIPPED);
        existingOrder.setShippingAddress("新しい住所");

        // Act
        Order updatedOrder = orderDao.updateOrder(existingOrder);

        // Assert
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        assertThat(updatedOrder.getShippingAddress()).isEqualTo("新しい住所");

        Order foundOrder = orderDao.findOrderById(existingOrder.getId()).get();
        assertThat(foundOrder.getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    @DisplayName("存在しない注文IDで更新しようとすると例外をスローすること")
    void updateOrder_shouldThrowException_whenOrderDoesNotExist() {
        // Arrange
        Order nonExistentOrder = new Order();
        nonExistentOrder.setId(999L);
        nonExistentOrder.setStatus(OrderStatus.PENDING);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            orderDao.updateOrder(nonExistentOrder);
        });
    }

    @Test
    @DisplayName("ユーザーIDで注文概要リストを取得できること")
    void findOrderSummariesByUserId_shouldReturnOrderSummaries() {
        // Arrange
        jdbcTemplate.update("INSERT INTO orders (user_id, status, total_price, ordered_at," +
                " shipping_address, shipping_postal_code, shipping_name) " +
                "VALUES (?, 'PAID', 1500, '2025-08-18 10:00:00', '東京都新宿区', '100-0003', '田中')", userId);
        jdbcTemplate.update("INSERT INTO orders (user_id, status, total_price, ordered_at," +
                " shipping_address, shipping_postal_code, shipping_name) " +
                "VALUES (?, 'SHIPPED', 2500, '2025-08-17 12:00:00', '東京都新宿区', '100-0003', '田中')", userId);

        // Act
        List<OrderSummaryDto> summaries = orderDao.findOrderSummariesByUserId(userId);

        // Assert
        assertThat(summaries).hasSize(2);

        // ソートされているので、新しい方の注文が先頭に来る
        assertThat(summaries.get(0).totalPrice()).isEqualTo(1500);
        assertThat(summaries.get(0).status()).isEqualTo(OrderStatus.PAID);

        assertThat(summaries.get(1).totalPrice()).isEqualTo(2500);
        assertThat(summaries.get(1).status()).isEqualTo(OrderStatus.SHIPPED);
    }
}