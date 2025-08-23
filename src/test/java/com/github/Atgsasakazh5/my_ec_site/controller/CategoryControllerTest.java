package com.github.Atgsasakazh5.my_ec_site.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.Atgsasakazh5.my_ec_site.dto.CategoryDto;
import com.github.Atgsasakazh5.my_ec_site.dto.PageResponseDto;
import com.github.Atgsasakazh5.my_ec_site.dto.ProductSummaryDto;
import com.github.Atgsasakazh5.my_ec_site.service.CategoryService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class CategoryControllerTest {

    @MockitoBean
    ProductService productService;

    @MockitoBean
    CategoryService categoryService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("カテゴリ一覧を取得できること")
    void getAllCategories_shouldReturnCategoryList() throws Exception {
        // Arrange
        var expectedCategories = List.of(
                new CategoryDto(1, "トップス"),
                new CategoryDto(2, "ボトムス")
        );

        when(categoryService.getAllCategories()).thenReturn(expectedCategories);

        // Act & Assert
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("トップス"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("ボトムス"));
    }

    @Test
    @DisplayName("特定のカテゴリの商品一覧をページネーション対応で取得できること")
    void getProductsByCategoryId_shouldReturnPaginatedProductList() throws Exception {
        // Arrange
        int categoryId = 1;
        int page = 0;
        int size = 20;

        var content = List.of(
                new ProductSummaryDto(
                        1L,
                        "テスト商品",
                        1000,
                        "テスト商品です。",
                        "https://example.com/image.jpg",
                        new CategoryDto(1, "トップス"),
                        null, null)
        );
        var totalElements = 1;
        var expectedResponse = new PageResponseDto<>(
                content,
                page,
                size,
                totalElements);

        when(productService.searchProductsByCategory(categoryId, page, size)).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(get("/api/categories/{categoryId}/products", categoryId)
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.page").value(page))
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("テスト商品"))
                .andExpect(jsonPath("$.content[0].price").value(1000))
                .andExpect(jsonPath("$.content[0].description").value("テスト商品です。"))
                .andExpect(jsonPath("$.content[0].imageUrl").value("https://example.com/image.jpg"))
                .andExpect(jsonPath("$.content[0].category.id").value(1))
                .andExpect(jsonPath("$.content[0].category.name").value("トップス"));
    }

    @Test
    @DisplayName("IDでカテゴリを取得できること")
    void getCategoryById_shouldReturnCategory() throws Exception {
        // Arrange
        int categoryId = 1;
        var expectedCategory = new CategoryDto(categoryId, "トップス");

        when(categoryService.searchCategoryById(categoryId)).thenReturn(expectedCategory);

        // Act & Assert
        mockMvc.perform(get("/api/categories/{id}", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId))
                .andExpect(jsonPath("$.name").value("トップス"));
    }
}