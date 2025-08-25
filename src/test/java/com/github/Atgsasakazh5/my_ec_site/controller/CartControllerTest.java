package com.github.Atgsasakazh5.my_ec_site.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.Atgsasakazh5.my_ec_site.dto.AddCartItemRequestDto;
import com.github.Atgsasakazh5.my_ec_site.dto.CartDetailDto;
import com.github.Atgsasakazh5.my_ec_site.dto.UpdateCartItemRequestDto;
import com.github.Atgsasakazh5.my_ec_site.exception.ResourceNotFoundException;
import com.github.Atgsasakazh5.my_ec_site.service.CartService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CartControllerTest {

    @MockitoBean
    CartService cartService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("認証済みユーザーがカートにアイテムを追加できること")
    @WithMockUser(username = "test@email.com", roles = "USER")
    void addCartItem_shouldSucceed_whenUserIsAuthenticated() throws Exception {

        // Arrange
        var request = new AddCartItemRequestDto(1L, 2);

        when(cartService.addItemToCart(anyString(), any(AddCartItemRequestDto.class)))
                .thenReturn(new CartDetailDto(1L, List.of(), 0));

        // Act & Assert
        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("認証されていないユーザーがカートにアイテムを追加しようとすると403エラーが返ること")
    void addCartItem_shouldReturnUnauthorized_whenUserIsNotAuthenticated() throws Exception {
        // Arrange
        var request = new AddCartItemRequestDto(1L, 2);

        // Act & Assert
        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("カートにアイテムを追加する際、不正なリクエストでは400エラーが返ること")
    @WithMockUser(username = "test@email.com", roles = "USER")
    void addCartItem_shouldReturnBadRequest_whenRequestIsInvalid() throws Exception {
        // Arrange
        var request = new AddCartItemRequestDto(null, -1); // 無効なSKU IDと数量

        // Act & Assert
        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("カートにアイテムを追加する際、存在しないSKU IDを指定すると404エラーが返ること")
    @WithMockUser(username = "test@email.com", roles = "USER")
    void addCartItem_shouldReturnNotFound_whenSkuDoesNotExist() throws Exception {
        // Arrange
        var request = new AddCartItemRequestDto(999L, 2); // 存在しないSKU ID

        when(cartService.addItemToCart(anyString(), any(AddCartItemRequestDto.class)))
                .thenThrow(new ResourceNotFoundException("存在しないSKUです: 999"));

        // Act & Assert
        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("カートにアイテムを追加する際、在庫が不足している場合は409エラーが返ること")
    @WithMockUser(username = "test@email.com", roles = "USER")
    void addCartItem_shouldReturnConflict_whenInventoryIsInsufficient() throws Exception {
        // Arrange
        var request = new AddCartItemRequestDto(1L, 10); // 在庫が不足している数量

        when(cartService.addItemToCart(anyString(), any(AddCartItemRequestDto.class)))
                .thenThrow(new IllegalStateException("在庫が不足しています。"));

        // Act & Assert
        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("認証済みユーザーがカートの詳細を取得できること")
    @WithMockUser(username = "test@email.com", roles = "USER")
    void getCartDetail_shouldSucceed_whenUserIsAuthenticated() throws Exception {
        // Arrange
        when(cartService.getCartDetail(anyString()))
                .thenReturn(new CartDetailDto(1L, List.of(), 0));

        // Act & Assert
        mockMvc.perform(get("/api/cart")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1L))
                .andExpect(jsonPath("$.totalPrice").value(0))
                .andExpect(jsonPath("$.cartItems").isEmpty());

    }

    @Test
    @DisplayName("認証されていないユーザーがカートの詳細を取得しようとすると403エラーが返ること")
    void getCartDetail_shouldReturnUnauthorized_whenUserIsNotAuthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/cart")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("認証済みユーザーがカートのアイテム数量を変更できること")
    @WithMockUser(username = "test@email.com", roles = "USER")
    void updateCartItem_shouldSucceed_whenUserIsAuthed() throws Exception {

        // Arrange
        var cartItemId = 1L;
        var request = new UpdateCartItemRequestDto(1);

        when(cartService.updateCartItemQuantity(anyString(), anyLong(), any(UpdateCartItemRequestDto.class)))
                .thenReturn(new CartDetailDto(1L, List.of(), 0));

        // Act & Assert
        mockMvc.perform(put("/api/cart/items/{cartItemId}", cartItemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1L))
                .andExpect(jsonPath("$.totalPrice").value(0))
                .andExpect(jsonPath("$.cartItems").isEmpty());
    }

    @Test
    @DisplayName("カートアイテムの数量を0以下に設定すると400が返ること")
    @WithMockUser(username = "test@email.com", roles = "USER")
    void updateCartItem_shouldReturnBadRequest_whenQuantityIsZeroOrNegative() throws Exception {
        // Arrange
        var cartItemId = 1L;
        var request = new UpdateCartItemRequestDto(0); // 無効な数量

        // Act & Assert
        mockMvc.perform(put("/api/cart/items/{cartItemId}", cartItemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("認証されていないユーザーがカートのアイテム数量を変更しようとすると403エラーが返ること")
    void updateCartItem_shouldReturnUnauthorized_whenUserIsNotAuthenticated() throws Exception {
        // Arrange
        var cartItemId = 1L;
        var request = new UpdateCartItemRequestDto(1);

        // Act & Assert
        mockMvc.perform(put("/api/cart/items/{cartItemId}", cartItemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("カートアイテムの数量を更新する際、在庫数が不足している場合は409エラーが返ること")
    @WithMockUser(username = "test@email.com", roles = "USER")
    void updateCartItem_shouldReturnConflict_whenInventoryIsInsufficient() throws Exception {
        // Arrange
        var cartItemId = 1L;
        var request = new UpdateCartItemRequestDto(10); // 在庫が不足している数量

        when(cartService.updateCartItemQuantity(anyString(), eq(cartItemId), any(UpdateCartItemRequestDto.class)))
                .thenThrow(new IllegalStateException("在庫が不足しています。"));

        // Act & Assert
        mockMvc.perform(put("/api/cart/items/{cartItemId}", cartItemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("本人以外のカートアイテムを更新できないこと")
    @WithMockUser(username = "test@email.com", roles = "USER")
    void updateCartItem_shouldReturnForbidden_whenUserTriesToUpdateOtherUsersCartItem() throws Exception {
        // Arrange
        var cartItemId = 999L; // 他のユーザーのカートアイテムID
        var request = new UpdateCartItemRequestDto(1);

        when(cartService.updateCartItemQuantity(anyString(), eq(cartItemId), any(UpdateCartItemRequestDto.class)))
                .thenThrow(new SecurityException("他人のカートアイテムを操作する権限がありません。"));

        // Act & Assert
        mockMvc.perform(put("/api/cart/items/{cartItemId}", cartItemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("認証済みユーザーがカートアイテムを削除できること")
    @WithMockUser(username = "test@email.com", roles = "USER")
    void deleteCartItem_shouldSucceed_whenUserIsAuthenticated() throws Exception {
        // Arrange
        var cartItemId = 1L;

        // Act & Assert
        mockMvc.perform(delete("/api/cart/items/{cartItemId}", cartItemId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("認証されていないユーザーがカートアイテムを削除しようとすると403エラーが返ること")
    void deleteCartItem_shouldReturnUnauthorized_whenUserIsNotAuthenticated() throws Exception {
        // Arrange
        var cartItemId = 1L;

        // Act & Assert
        mockMvc.perform(delete("/api/cart/items/{cartItemId}", cartItemId))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("本人以外のカートアイテムを削除できないこと")
    @WithMockUser(username = "test@email.com", roles = "USER")
    void deleteCartItem_shouldReturnForbidden_whenUserTriesToDeleteOtherUsersCartItem() throws Exception {
        // Arrange
        var cartItemId = 999L;

        doThrow(new SecurityException("他人のカートアイテムを操作する権限がありません。"))
                .when(cartService).deleteItemFromCart(anyString(), anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/cart/items/{cartItemId}", cartItemId))
                .andExpect(status().isForbidden());
    }

}