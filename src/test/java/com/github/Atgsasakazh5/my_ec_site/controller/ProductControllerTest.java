package com.github.Atgsasakazh5.my_ec_site.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.Atgsasakazh5.my_ec_site.dto.*;
import com.github.Atgsasakazh5.my_ec_site.exception.ResourceNotFoundException;
import com.github.Atgsasakazh5.my_ec_site.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerTest {

    @MockitoBean
    ProductService productService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("ページネーション対応の商品一覧を取得できること")
    void getProductSummaries_shouldReturnPaginatedProductSummaries() throws Exception {
        // Arrange
        int page = 0;
        int size = 20;

        var content = List.of(new ProductSummaryDto(
                1L,
                "テスト商品",
                1000,
                "テスト商品です。",
                "https://example.com/image.jpg",
                new CategoryDto(1, "テストカテゴリ"),
                null, null)
        );
        var totalElements = 1;
        var expectedResponse = new PageResponseDto<>(
                content,
                page,
                size,
                totalElements);

        when(productService.findAllPaginated(page, size)).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(get("/api/products")
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.page").value(page))
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("テスト商品"));
    }

    @Test
    @DisplayName("存在しない商品IDで商品詳細を取得しようとすると404エラーが返ること")
    void getProductDetail_shouldReturnNotFoundForNonExistentProduct() throws Exception {
        // Arrange
        Long nonExistentProductId = 999L;
        when(productService.getProductDetail(nonExistentProductId)).
                thenThrow(new ResourceNotFoundException("商品が見つかりません"));

        // Act & Assert
        mockMvc.perform(get("/api/products/{productId}", nonExistentProductId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("商品が見つかりません"));
    }

    @Test
    @DisplayName("商品詳細を取得できること")
    void getProductDetail_shouldReturnProductDetail() throws Exception {
        // Arrange
        Long productId = 1L;
        var expectedDetail = new ProductDetailDto(
                productId,
                "テスト商品",
                1000,
                "テスト商品です。",
                "https://example.com/image.jpg",
                new CategoryDto(1, "テストカテゴリ"),
                List.of(new SkuDto(
                        1L,
                        "S",
                        "Red",
                        1000,
                        new InventoryDto(10)
                )),
                null, null);

        when(productService.getProductDetail(productId)).thenReturn(expectedDetail);

        // Act & Assert
        mockMvc.perform(get("/api/products/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.name").value("テスト商品"))
                .andExpect(jsonPath("$.price").value(1000))
                .andExpect(jsonPath("$.description").value("テスト商品です。"))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/image.jpg"))
                .andExpect(jsonPath("$.category.id").value(1))
                .andExpect(jsonPath("$.category.name").value("テストカテゴリ"))
                .andExpect(jsonPath("$.skus[0].id").value(1L))
                .andExpect(jsonPath("$.skus[0].size").value("S"))
                .andExpect(jsonPath("$.skus[0].color").value("Red"))
                .andExpect(jsonPath("$.skus[0].extraPrice").value(1000))
                .andExpect(jsonPath("$.skus[0].inventory.quantity").value(10));

    }

}