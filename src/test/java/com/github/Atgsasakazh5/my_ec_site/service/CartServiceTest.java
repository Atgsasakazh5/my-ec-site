package com.github.Atgsasakazh5.my_ec_site.service;

import com.github.Atgsasakazh5.my_ec_site.dto.AddCartItemRequestDto;
import com.github.Atgsasakazh5.my_ec_site.dto.CartItemDto;
import com.github.Atgsasakazh5.my_ec_site.dto.UpdateCartItemRequestDto;
import com.github.Atgsasakazh5.my_ec_site.entity.Cart;
import com.github.Atgsasakazh5.my_ec_site.entity.CartItem;
import com.github.Atgsasakazh5.my_ec_site.entity.Inventory;
import com.github.Atgsasakazh5.my_ec_site.entity.Sku;
import com.github.Atgsasakazh5.my_ec_site.exception.ResourceNotFoundException;
import com.github.Atgsasakazh5.my_ec_site.repository.CartDao;
import com.github.Atgsasakazh5.my_ec_site.repository.CartItemDao;
import com.github.Atgsasakazh5.my_ec_site.repository.InventoryDao;
import com.github.Atgsasakazh5.my_ec_site.repository.SkuDao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @InjectMocks
    private CartService cartService;

    @Mock
    private CartDao cartDao;

    @Mock
    private CartItemDao cartItemDao;

    @Mock
    private SkuDao skuDao;

    @Mock
    private InventoryDao inventoryDao;

    @Test
    @DisplayName("cartIDでカートの詳細を取得するテスト")
    void getCartDetailById_shouldReturnCartDetailDto_whenCartExists() {
        // Arrange
        Long cartId = 1L;

        when(cartItemDao.findDetailedItemsByCartId(cartId)).thenReturn(List.of(
                new CartItemDto(1L,
                        "Test Item",
                        "item-img.jpg",
                        2L,
                        "S",
                        "Red",
                        1000,
                        3)
        ));

        // Act
        var cartDetail = cartService.getCartDetail(cartId);

        // Assert
        assertNotNull(cartDetail);
        assertEquals(cartId, cartDetail.cartId());
        assertEquals(1, cartDetail.cartItems().size());
        assertEquals("Test Item", cartDetail.cartItems().get(0).productName());
        assertEquals(1000, cartDetail.cartItems().get(0).price());
        assertEquals(3, cartDetail.cartItems().get(0).quantity());
        assertEquals(3000, cartDetail.totalPrice());

    }

    @Test
    @DisplayName("メールアドレスでカートの詳細を取得できること-正常系")
    void getCartDetailByEmail_shouldReturnCartDetailDto_whenCartExists() {
        // Arrange
        String email = "cart-item-test@email.com";

        when(cartDao.findCartByEmail(email)).thenReturn(java.util.Optional.of(new Cart(1L, 1L)));
        when(cartItemDao.findDetailedItemsByCartId(1L)).thenReturn(List.of(
                new CartItemDto(1L,
                        "Test Item",
                        "item-img.jpg",
                        2L,
                        "S",
                        "Red",
                        1000,
                        3)
        ));

        // Act
        var cartDetail = cartService.getCartDetail(email);

        // Assert
        assertNotNull(cartDetail);
        assertEquals(1L, cartDetail.cartId());
        assertEquals(1, cartDetail.cartItems().size());
        assertEquals("Test Item", cartDetail.cartItems().get(0).productName());
        assertEquals(1000, cartDetail.cartItems().get(0).price());
        assertEquals(3, cartDetail.cartItems().get(0).quantity());
        assertEquals(3000, cartDetail.totalPrice());

    }

    @Test
    @DisplayName("メールアドレスでカートが見つからない場合、例外をスローすること-異常系")
    void getCartDetailByEmail_shouldThrowResourceNotFoundException_whenCartDoesNotExist() {
        // Arrange
        String email = "cart-item-test@email.com";

        when(cartDao.findCartByEmail(email)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            cartService.getCartDetail(email);
        });
    }

    @Test
    @DisplayName("カートにまだ入れていないSKUを追加できること-正常系")
    void addItemToCart_shouldAddItemToCart_whenSkuExists() {
        // Arrange
        var email = "cart-item-test@email.cpm";
        var cartId = 1L;
        var skuId = 1L;
        var requestDto = new AddCartItemRequestDto(skuId, 2);

        var sku = new Sku();
        sku.setId(skuId);
        sku.setProductId(1L);
        sku.setSize("M");
        sku.setColor("Blue");
        sku.setExtraPrice(100);
        when(skuDao.findById(skuId)).thenReturn(Optional.of(sku));

        var inventory = new Inventory();
        inventory.setSkuId(skuId);
        inventory.setQuantity(10);
        when(inventoryDao.findBySkuId(skuId)).thenReturn(Optional.of(inventory));

        when(cartDao.findCartByEmail(email)).thenReturn(Optional.of(new Cart(1L, 1L)));

        when(cartItemDao.findByCartIdAndSkuId(cartId, skuId)).thenReturn(Optional.empty());

        var finalCartItems = List.of(new CartItemDto(301L, "Test Product", null, skuId, "M", "Red", 1100, 2));
        when(cartItemDao.findDetailedItemsByCartId(cartId)).thenReturn(finalCartItems);

        // Act
        var result = cartService.addItemToCart(email, requestDto);

        // Assert
        verify(cartItemDao, times(1)).save(any(CartItem.class)); // saveが呼ばれることを確認
        assertThat(result.totalPrice()).isEqualTo(2200);
        assertThat(result.cartItems()).hasSize(1);
    }

    @Test
    @DisplayName("カート内の既存SKUの数量を増やすこと-正常系")
    void addItemToCart_shouldIncreaseQuantity_whenItemAlreadyExists() {
        // Arrange
        var email = "test@example.com";
        var cartId = 1L;
        var skuId = 101L;
        var request = new AddCartItemRequestDto(skuId, 2);

        var existingItem = new CartItem(301L, cartId, skuId, 3);

        when(skuDao.findById(skuId)).thenReturn(Optional.of(new Sku(skuId, 1L, "M", "Red", 100, null, null)));
        when(inventoryDao.findBySkuId(skuId)).thenReturn(Optional.of(new Inventory(201L, skuId, 10, null)));
        when(cartDao.findCartByEmail(email)).thenReturn(Optional.of(new Cart(cartId, 1L)));

        when(cartItemDao.findByCartIdAndSkuId(cartId, skuId)).thenReturn(Optional.of(existingItem));

        var finalCartItems = List.of(new CartItemDto(301L, "Test Product", null, skuId, "M", "Red", 1100, 5));
        when(cartItemDao.findDetailedItemsByCartId(cartId)).thenReturn(finalCartItems);

        // Act
        var result = cartService.addItemToCart(email, request);

        // Assert
        verify(cartItemDao, times(1)).update(any(CartItem.class));
        assertThat(result.totalPrice()).isEqualTo(5500);
        assertThat(result.cartItems().get(0).quantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("カートにSKUを追加する際、存在しないSKUIDの場合は例外をスローすること-異常系")
    void addItemToCart_shouldThrowException_whenSkuDoesNotExist() {
        // Arrange
        String email = "";
        var requestDto = new AddCartItemRequestDto(999L, 1);
        when(skuDao.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            cartService.addItemToCart(email, requestDto);
        });
    }

    @Test
    @DisplayName("カートにSKUを追加する際、在庫が不足している場合は例外をスローすること-異常系")
    void addItemToCart_shouldThrowException_whenInventoryIsInsufficient() {
        // Arrange
        String email = "test@email.com";
        var requestDto = new AddCartItemRequestDto(1L, 5);

        when(skuDao.findById(1L)).thenReturn(Optional.of(new Sku(1L, 1L, "M", "Red", 100, null, null)));
        when(inventoryDao.findBySkuId(1L)).thenReturn(Optional.of(new Inventory(1L, 1L, 3, null)));
        when(cartDao.findCartByEmail(email)).thenReturn(Optional.of(new Cart(1L, 1L)));

        when(cartItemDao.findByCartIdAndSkuId(1L, 1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            cartService.addItemToCart(email, requestDto);
        });
    }

    @Test
    @DisplayName("カートアイテムの数量を更新できること-正常系")
    void updateCartItemQuantity_shouldUpdateQuantity_whenItemExists() {
        // Arrange
        String email = "test@email.com";
        Long cartItemId = 1L;
        var request = new UpdateCartItemRequestDto(3);
        var existingItem = new CartItem(1L, 1L, 1L, 2);

        when(cartDao.findCartByEmail(email)).thenReturn(Optional.of(new Cart(1L, 1L)));
        when(cartItemDao.findById(cartItemId)).thenReturn(Optional.of(existingItem));
        when(inventoryDao.findBySkuId(existingItem.getSkuId()))
                .thenReturn(Optional.of(new Inventory(1L, existingItem.getSkuId(), 10, null)));

        var finalCartItems = List.of(new CartItemDto(1L, "Test Item", null, 1L, "M", "Red", 1100, 3));
        when(cartItemDao.findDetailedItemsByCartId(1L)).thenReturn(finalCartItems);

        // Act
        var result = cartService.updateCartItemQuantity(email, cartItemId, request);

        // Assert
        ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemDao).update(captor.capture());
        assertThat(captor.getValue().getQuantity()).isEqualTo(3);

        // 返されたDTOの検証
        assertThat(result.totalPrice()).isEqualTo(3300);
        assertThat(result.cartItems().get(0).quantity()).isEqualTo(3);

    }

    @Test
    @DisplayName("ユーザーのカートと異なるカートアイテムを更新しようとすると例外がスローされること-異常系")
    void updateCartItemQuantity_shouldThrowException_whenCartItemDoesNotBelongToUser() {
        // Arrange
        String email = "test@email.com";
        Long cartItemId = 1L;
        var request = new UpdateCartItemRequestDto(3);

        when(cartDao.findCartByEmail(email)).thenReturn(Optional.of(new Cart(1L, 1L)));
        when(cartItemDao.findById(cartItemId)).thenReturn(Optional.of(new CartItem(1L, 100L, 1L, 2)));

        // Act & Assert
        assertThrows(SecurityException.class, () -> {
            cartService.updateCartItemQuantity(email, cartItemId, request);
        });
    }

    @Test
    @DisplayName("カートアイテムの数量更新リクエストよりも在庫が不足している場合は例外をスローすること-異常系")
    void updateCartItemQuantity_shouldThrowException_whenInventoryIsInsufficient() {
        // Arrange
        String email = "test@email.com";
        Long cartItemId = 1L;
        var request = new UpdateCartItemRequestDto(5);
        var existingItem = new CartItem(1L, 1L, 1L, 2);

        when(cartDao.findCartByEmail(email)).thenReturn(Optional.of(new Cart(1L, 1L)));
        when(cartItemDao.findById(cartItemId)).thenReturn(Optional.of(existingItem));
        when(inventoryDao.findBySkuId(existingItem.getSkuId()))
                .thenReturn(Optional.of(new Inventory(1L, existingItem.getSkuId(), 3, null)));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            cartService.updateCartItemQuantity(email, cartItemId, request);
        });
    }

    @Test
    @DisplayName("アイテムをカートから削除できること")
    void deleteItemFromCart_shouldSucceed_whenItemExists() {
        // Arrange
        String email = "test@email.com";
        Long cartItemId = 1L;
        var existingItem = new CartItem(1L, 1L, 1L, 2);

        when(cartDao.findCartByEmail(email)).thenReturn(Optional.of(new Cart(1L, 1L)));
        when(cartItemDao.findById(cartItemId)).thenReturn(Optional.of(existingItem));

        // Act
        cartService.deleteItemFromCart(email, cartItemId);

        // Assert
        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
        verify(cartItemDao).deleteById(captor.capture());
        assertThat(captor.getValue()).isEqualTo(1L);
    }

    @Test
    @DisplayName("不正なCartItemIDで削除ができないこと")
    void deleteFromCart_sholdThrowException_whenCartItemDoesNotBelongToUser() {
        // Arrange
        String email = "test@email.com";
        Long cartItemId = 1L;

        when(cartDao.findCartByEmail(email)).thenReturn(Optional.of(new Cart(1L, 1L)));
        when(cartItemDao.findById(cartItemId)).thenReturn(Optional.of(new CartItem(1L, 100L, 1L, 2)));

        // Act & Assert
        assertThrows(SecurityException.class, () -> {
            cartService.deleteItemFromCart(email, cartItemId);
        });
    }

    @Test
    @DisplayName("存在しないCartItemIDでResorceNotFoundExceptionがスローされること")
    void deleteFromCart_shuldThrowException_whenCartItemIdNotExist() {
        // Arrange
        String email = "test@email.com";
        Long cartItemId = 1L;

        when(cartDao.findCartByEmail(email)).thenReturn(Optional.of(new Cart(1L, 1L)));
        when(cartItemDao.findById(cartItemId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            cartService.deleteItemFromCart(email, cartItemId);
        });
    }
}