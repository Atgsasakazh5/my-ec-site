package com.github.Atgsasakazh5.my_ec_site.service;

import com.github.Atgsasakazh5.my_ec_site.dto.*;
import com.github.Atgsasakazh5.my_ec_site.entity.Cart;
import com.github.Atgsasakazh5.my_ec_site.entity.Inventory;
import com.github.Atgsasakazh5.my_ec_site.entity.Order;
import com.github.Atgsasakazh5.my_ec_site.entity.OrderStatus;
import com.github.Atgsasakazh5.my_ec_site.exception.ResourceNotFoundException;
import com.github.Atgsasakazh5.my_ec_site.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderServiceImpl orderService;

    @Mock
    private OrderDao orderDao;

    @Mock
    private UserService userService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private CartDao cartDao;

    @Mock
    private CartItemDao cartItemDao;

    @Mock
    private InventoryDao inventoryDao;

    @Mock
    private OrderDetailDao orderDetailDao;

    @Test
    @DisplayName("User情報の一致するorderIdを渡された時、orderDetailsDtoの取得に成功すること-正常系")
    void getOrderDetails_Success() {
        // Arrange
        String email = "test@email.com";
        Long orderId = 1L;

        when(userService.findByEmail(email)).thenReturn(new UserDto(1L, email, "Test User", Set.of("3", "ROLE_TEST")));
        when(orderDao.findOrderById(orderId)).thenReturn(Optional.of(
                new Order(
                        orderId,
                        1L,
                        OrderStatus.PENDING,
                        1000,
                        "123 Main St",
                        "Tokyo",
                        "123-4567",
                        LocalDateTime.now())));
        when(orderDetailDao.findByOrderId(orderId)).thenReturn(List.of(
                new OrderDetailDto(
                        1L,
                        orderId,
                        1L,
                        "Tシャツ",
                        "S",
                        "Red",
                        "image.jpg",
                        1500,
                        2)));

        // Act
        var resultList = orderService.getOrderDetails(email, orderId);

        // Assert
        assertThat(resultList).isNotNull();
        assertThat(resultList).hasSize(1);

        // 取得した注文詳細の内容を検証
        var orderDetail = resultList.get(0);
        assertThat(orderDetail.productName()).isEqualTo("Tシャツ");
        assertThat(orderDetail.quantity()).isEqualTo(2);
        assertThat(orderDetail.priceAtOrder()).isEqualTo(1500);

    }

    @Test
    @DisplayName("他人の注文IDを指定した場合にSecurityExceptionをスローすること")
    void getOrderDetails_shouldThrowSecurityException_whenOrderBelongsToOtherUser() {
        // Arrange
        String email = "test@example.com";
        Long orderId = 1L;
        Long myUserId = 10L;
        Long otherUserId = 20L;

        when(userService.findByEmail(email)).thenReturn(new UserDto(myUserId, "me", email, Set.of("3","ROLE_TEST")));

        when(orderDao.findOrderById(orderId)).thenReturn(Optional.of(
                new Order(
                        orderId,
                        otherUserId,
                        OrderStatus.PENDING,
                        1000,
                        "東京丸の内",
                        "123-4321",
                        "俣野",
                        LocalDateTime.now())
        ));

        // Act & Assert
        assertThrows(SecurityException.class, () -> {
            orderService.getOrderDetails(email, orderId);
        });
    }

    @Test
    @DisplayName("存在しない注文IDを指定した場合にResourceNotFoundExceptionをスローすること")
    void getOrderDetails_shouldThrowResourceNotFoundException_whenOrderDoesNotExist() {
        // Arrange
        String email = "test@email.com";
        Long orderId = 999L;

        when(userService.findByEmail(email)).thenReturn(new UserDto(1L, email, "Test User", Set.of("3", "ROLE_TEST")));
        when(orderDao.findOrderById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.getOrderDetails(email, orderId);
        });
    }

    @Test
    @DisplayName("注文の発行が成功すること-正常系")
    void placeOrder_shouldSucceed() {
        // Arrange
        String email = "test@example.com";
        Long userId = 1L;
        Long cartId = 10L;
        Long skuId = 101L;
        var request = new CreateOrderRequestDto("Tokyo", "100-0001", "Test User", "pm_test");

        when(cartDao.findCartByEmail(email)).thenReturn(Optional.of(new Cart(cartId, userId)));

        var cartItems = List.of(new CartItemDto(201L, "T-Shirt", null, skuId, "M", "Red", 1500, 2, 10));
        when(cartItemDao.findDetailedItemsByCartId(cartId)).thenReturn(cartItems);

        var lockedInventories = List.of(new Inventory(301L, skuId, 10, null));
        when(inventoryDao.findBySkuIdsWithLock(List.of(skuId))).thenReturn(lockedInventories);

        var savedOrder = new Order(501L, userId, OrderStatus.PAID, 3000, "Tokyo", "100-0001", "Test User", LocalDateTime.now());
        when(orderDao.saveOrder(any(Order.class))).thenReturn(savedOrder);

        // getOrderDetailsのモック
        when(orderDao.findOrderById(savedOrder.getId())).thenReturn(Optional.of(savedOrder));
        when(userService.findByEmail(email)).thenReturn(new UserDto(userId, "Test User", email, Set.of("3", "ROLE_TEST")));
        when(orderDetailDao.findByOrderId(savedOrder.getId())).thenReturn(List.of());

        // 2. Act
        orderService.placeOrder(email, request);

        // Assert
        verify(paymentService, times(1)).processPayment(any(PaymentRequestDto.class));
        verify(inventoryDao, times(1)).updateAll(anyList());
        verify(orderDetailDao, times(1)).save(anyList());
        verify(cartItemDao, times(1)).deleteByCartId(cartId);
    }

    @Test
    @DisplayName("カートが空の場合にIllegalStateExceptionをスローすること")
    void placeOrder_shouldThrowIllegalStateException_whenCartIsEmpty() {
        // Arrange
        String email = "test@email.com";

        when(cartDao.findCartByEmail(email)).thenReturn(Optional.of(new Cart(10L, 1L)));
        when(cartItemDao.findDetailedItemsByCartId(10L)).thenReturn(List.of());

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            orderService.placeOrder(email, new CreateOrderRequestDto("Tokyo", "100-0001", "Test User", "credit_card"));
        });
    }

    @Test
    @DisplayName("在庫が不足している場合にIllegalStateExceptionをスローすること")
    void placeOrder_shouldThrowIllegalStateException_whenInventoryIsInsufficient() {
        // Arrange
        String email = "test@email.com";
        Long userId = 1071L;
        Long cartId = 10L;
        Long skuId = 101L;

        var request = new CreateOrderRequestDto("Tokyo", "100-0001", "Test User", "credit_card");

        when(cartDao.findCartByEmail(email)).thenReturn(Optional.of(new Cart(cartId, userId)));
        var cartItems = List.of(new CartItemDto(201L, "T-Shirt", null, skuId, "M", "Red", 1500, 2, 10));
        when(cartItemDao.findDetailedItemsByCartId(cartId)).thenReturn(cartItems);
        var lockedInventories = List.of(new Inventory(301L, skuId, 1, null));
        when(inventoryDao.findBySkuIdsWithLock(List.of(skuId))).thenReturn(lockedInventories);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            orderService.placeOrder(email, request);
        });
        verify(inventoryDao, never()).updateAll(anyList());

    }

    @Test
    @DisplayName("自分のorder一覧を取得できること-正常系")
    void getOrderSummaries_shouldReturnOrderSummaries_whenUserHasOrders() {
        // Arrange
        String email = "test@email.com";
        Long userId = 1L;
        when(userService.findByEmail(email)).thenReturn(new UserDto(userId, "Test User", email, Set.of("3", "ROLE_TEST")));

        var now = LocalDateTime.now();
        var expectedSummaries = List.of(
                new OrderSummaryDto(1L, now, 3000, OrderStatus.PENDING),
                new OrderSummaryDto(2L, now, 5000, OrderStatus.SHIPPED)
        );
        when(orderDao.findOrderSummariesByUserId(userId)).thenReturn(expectedSummaries);

        // Act
        List<OrderSummaryDto> actualSummaries = orderService.getOrderSummaries(email);

        // Assert
        assertThat(actualSummaries).isNotEmpty();
        assertThat(actualSummaries).hasSize(2);
        assertThat(actualSummaries).isEqualTo(expectedSummaries);
    }

    @Test
    @DisplayName("自分の注文詳細を正しく取得できること")
    void getOrderDetail_shouldSucceed_whenOrderBelongsToUser() {
        // Arrange
        String email = "test@example.com";
        Long userId = 1L;
        Long orderId = 101L;

        when(userService.findByEmail(email)).thenReturn(new UserDto(userId, "Test User", email, Set.of("3", "ROLE_TEST")));
        when(orderDao.findOrderById(orderId)).thenReturn(Optional.of(new Order(
                orderId,
                userId,
                OrderStatus.PENDING,
                5000,
                "東京都文京区",
                "123-4567",
                "山崎",
                LocalDateTime.now()
        )));
        when(orderDetailDao.findByOrderId(orderId)).thenReturn(List.of(
                new OrderDetailDto(1L, orderId, 1L, "Tシャツ", "M", "Red", "image.jpg", 2500, 2)
        ));

        // Act
        OrderDetailResponseDto result = orderService.getOrderDetail(email, orderId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.orderId()).isEqualTo(orderId);
    }

    @Test
    @DisplayName("存在しない注文IDで検索すると例外をスローすること")
    void getOrderDetail_shouldThrowException_whenOrderNotFound() {
        // Arrange
        String email = "test@example.com";
        Long orderId = 999L;

        when(userService.findByEmail(email)).thenReturn(new UserDto(1L, "Test User", email, Set.of("3", "ROLE_TEST")));
        when(orderDao.findOrderById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.getOrderDetail(email, orderId);
        });
    }

    @Test
    @DisplayName("他人の注文IDで検索すると例外をスローすること")
    void getOrderDetail_shouldThrowException_whenOrderDoesNotBelongToUser() {
        // Arrange
        String email = "test@example.com";
        Long myUserId = 1L;
        Long otherUserId = 2L; // 他人のユーザーID
        Long orderId = 101L;

        when(userService.findByEmail(email)).thenReturn(new UserDto(myUserId, "Test User", email, Set.of("3", "ROLE_TEST")));

        when(orderDao.findOrderById(orderId)).thenReturn(Optional.of(new Order(
                orderId,
                otherUserId,
                OrderStatus.PENDING,
                5000,
                "東京都文京区",
                "123-4567",
                "山崎",
                LocalDateTime.now()
        )));

        // Act & Assert
        assertThrows(SecurityException.class, () -> {
            orderService.getOrderDetail(email, orderId);
        });
    }
}