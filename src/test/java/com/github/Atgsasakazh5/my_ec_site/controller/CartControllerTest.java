package com.github.Atgsasakazh5.my_ec_site.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.Atgsasakazh5.my_ec_site.dto.AddCartItemRequestDto;
import com.github.Atgsasakazh5.my_ec_site.dto.CartDetailDto;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
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
}