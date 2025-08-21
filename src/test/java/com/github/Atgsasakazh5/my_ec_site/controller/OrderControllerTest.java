package com.github.Atgsasakazh5.my_ec_site.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.Atgsasakazh5.my_ec_site.dto.*;
import com.github.Atgsasakazh5.my_ec_site.entity.OrderStatus;
import com.github.Atgsasakazh5.my_ec_site.service.OrderService;
import com.github.Atgsasakazh5.my_ec_site.service.PaymentService;
import com.stripe.exception.CardException;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class OrderControllerTest {

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private PaymentService paymentService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("認証済みユーザーが注文を作成できること")
    @WithMockUser(username = "test@email.com", roles = "USER")
    void createOrderWithAuthenticatedUser() throws Exception {

        // Arrange
        var request = new CreateOrderRequestDto(
                "東京都千代田区",
                "101-0111",
                "島田",
                "credit_card");

        var responseDtoList = List.of(
                new OrderDetailDto(1L, 1L, 101L, "Tシャツ", "S", "Red", null, 1500, 2)
        );

        when(orderService.placeOrder(anyString(), any(CreateOrderRequestDto.class)))
                .thenReturn(responseDtoList);

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].productName").value("Tシャツ"));

    }

    @Test
    @DisplayName("認証されていないユーザーが注文を作成しようとすると403エラーが返ること")
    void createOrder_throws403_whenUserIsUnauthorized() throws Exception {
        // Arrange
        var request = new CreateOrderRequestDto(
                "東京都千代田区",
                "101-0111",
                "島田",
                "credit_card");

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("認証済みユーザーが注文を作成する際に、無効なリクエストボディを送信すると400エラーが返ること")
    @WithMockUser(username = "test@email.com", roles = "USER")
    void createOrderWithInvalidRequest() throws Exception {
        // Arrange
        var invalidRequest = new CreateOrderRequestDto(
                "",
                "101-0111",
                "島田",
                "credit_card");

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("住所は入力必須です")));
    }

    @Test
    @DisplayName("カートが空の状態で注文しようとすると409エラーが返ること")
    @WithMockUser(username = "test@email.com", roles = "USER")
    void createOrder_shouldReturnConflict_whenCartIsEmpty() throws Exception {
        // Arrange
        var request = new CreateOrderRequestDto("Tokyo", "100-0001", "Test User", "credit_card");

        when(orderService.placeOrder(anyString(), any(CreateOrderRequestDto.class)))
                .thenThrow(new IllegalStateException("カートが空です"));

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("カートが空です"));
    }

    @Test
    @DisplayName("認証済みユーザーが注文を作成する際に、在庫不足で注文できない場合は409エラーが返ること")
    @WithMockUser(username = "test@email.com", roles = "USER")
    void createOrder_shouldReturnConflict_whenInventoryIsInsufficient() throws Exception {
        // Arrange
        var request = new CreateOrderRequestDto("Tokyo", "100-0001", "Test User", "credit_card");

        when(orderService.placeOrder(anyString(), any(CreateOrderRequestDto.class)))
                .thenThrow(new IllegalStateException("在庫が不足しています"));

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("在庫が不足しています"));
    }

    @Test
    @DisplayName("自分の注文概要一覧を取得できること")
    @WithMockUser(username = "test@example.com", roles = "USER")
    void getOrderSummaries_shouldSucceed() throws Exception {
        // Arrange
        var summaries = List.of(new OrderSummaryDto(1L, LocalDateTime.now(), 5000, OrderStatus.PAID));
        when(orderService.getOrderSummaries("test@example.com")).thenReturn(summaries);

        // Act & Assert
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].orderId").value(1L));
    }

    @Test
    @DisplayName("自分の注文詳細を取得できること")
    @WithMockUser(username = "test@example.com", roles = "USER")
    void getOrderDetails_shouldSucceed() throws Exception {
        // Arrange
        var orderId = 1L;
        var email = "test@example.com";
        var detailResponse = new OrderDetailResponseDto(
                orderId,
                "Tokyo",
                "100-0001",
                "Test User",
                5000,
                OrderStatus.PAID,
                LocalDateTime.now(),
                List.of(new OrderDetailDto(1L, 1L, 101L, "Tシャツ", "S", "Red", null, 1500, 2)
                ));
        when(orderService.getOrderDetail(email, orderId)).thenReturn(detailResponse);

        // Act & Assert
        mockMvc.perform(get("/api/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.shippingAddress").value("Tokyo"));
    }

    @Test
    @DisplayName("他人の注文詳細を取得しようとすると403エラーになること")
    @WithMockUser(username = "test@example.com", roles = "USER")
    void getOrderDetails_shouldReturnForbidden_forOtherUsersOrder() throws Exception {
        // Arrange
        var otherUserOrderId = 2L;
        var email = "test@example.com";

        // サービス層でSecurityExceptionがスローされる状況をモック
        when(orderService.getOrderDetail(email, otherUserOrderId))
                .thenThrow(new SecurityException("この注文にアクセスする権限がありません。"));

        // Act & Assert
        mockMvc.perform(get("/api/orders/{orderId}", otherUserOrderId))
                .andExpect(status().isForbidden());
    }
}