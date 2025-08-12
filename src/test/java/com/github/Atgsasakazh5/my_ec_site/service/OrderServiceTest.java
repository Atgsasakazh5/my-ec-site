package com.github.Atgsasakazh5.my_ec_site.service;

import com.github.Atgsasakazh5.my_ec_site.dto.CartItemDto;
import com.github.Atgsasakazh5.my_ec_site.dto.CreateOrderRequestDto;
import com.github.Atgsasakazh5.my_ec_site.dto.OrderDetailDto;
import com.github.Atgsasakazh5.my_ec_site.dto.UserDto;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderDao orderDao;

    @Mock
    private UserService userService;

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

        when(userService.findByEmail(email)).thenReturn(new UserDto(1L, email, "Test User"));
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

        when(userService.findByEmail(email)).thenReturn(new UserDto(myUserId, "me", email));

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

        when(userService.findByEmail(email)).thenReturn(new UserDto(1L, email, "Test User"));
        when(orderDao.findOrderById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.getOrderDetails(email, orderId);
        });
    }

    @Test
    @DisplayName("注文の発行が成功すること-正常系")
    void placeOrder_shouldSucceed_whenCartIsNotEmptyAndInventoryIsSufficient() {
        // Arrange
        String email = "test@example.com";
        Long userId = 1L;
        Long cartId = 10L;
        Long skuId = 101L;
        var request = new CreateOrderRequestDto("Tokyo", "100-0001", "Test User");

        when(userService.findByEmail(email)).thenReturn(new UserDto(userId, "Test User", email));

        when(cartDao.findCartByEmail(email)).thenReturn(Optional.of(new Cart(cartId, userId)));

        var cartItems = List.of(new CartItemDto(201L, "T-Shirt", null, skuId, "M", "Red", 1500, 2, 10));
        when(cartItemDao.findDetailedItemsByCartId(cartId)).thenReturn(cartItems);

        var lockedInventories = List.of(new Inventory(301L, skuId, 10, null));
        when(inventoryDao.findBySkuIdsWithLock(List.of(skuId))).thenReturn(lockedInventories);

        var savedOrder = new Order(501L, userId, OrderStatus.PENDING, 3000, "Tokyo", "100-0001", "Test User", LocalDateTime.now());

        when(orderDao.findOrderById(savedOrder.getId())).thenReturn(Optional.of(savedOrder));
        when(orderDao.saveOrder(any(Order.class))).thenReturn(savedOrder);

        when(orderDetailDao.findByOrderId(savedOrder.getId())).thenReturn(List.of());

        // Act
        orderService.placeOrder(email, request);

        // Assert
        // 在庫、注文、注文明細、カートアイテム削除がそれぞれ1回ずつ呼ばれたことを検証
        verify(inventoryDao, times(1)).updateAll(anyList());
        verify(orderDao, times(1)).saveOrder(any(Order.class));
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
            orderService.placeOrder(email, new CreateOrderRequestDto("Tokyo", "100-0001", "Test User"));
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

        var request = new CreateOrderRequestDto("Tokyo", "100-0001", "Test User");

        when(cartDao.findCartByEmail(email)).thenReturn(Optional.of(new Cart(cartId, userId)));
        var cartItems = List.of(new CartItemDto(201L, "T-Shirt", null, skuId, "M", "Red", 1500, 2, 10));
        when(cartItemDao.findDetailedItemsByCartId(cartId)).thenReturn(cartItems);
        var lockedInventories = List.of(new Inventory(301L, skuId, 1, null)); // 在庫が1しかない
        when(inventoryDao.findBySkuIdsWithLock(List.of(skuId))).thenReturn(lockedInventories);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            orderService.placeOrder(email, request);
        });
        verify(inventoryDao, never()).updateAll(anyList());

    }
}