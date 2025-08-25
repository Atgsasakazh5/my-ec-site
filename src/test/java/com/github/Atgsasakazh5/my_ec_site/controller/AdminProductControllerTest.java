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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminProductControllerTest {

    @MockitoBean
    ProductService productService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("Adminユーザーが商品を作成できること")
    @WithMockUser(roles = "ADMIN")
    void createProduct_return201AndProduct_whenRequestCorrect() throws Exception {
        // Arrange
        var request = new ProductCreateRequestDto(
                "test商品", 1, "商品説明", "image.jpg", 1000,
                List.of(new SkuCreateRequestDto("S", "Red", 100, 10)));

        var responseDto = new ProductDetailDto(
                1L,
                request.name(),
                request.price(),
                request.description(),
                request.imageUrl(),
                new CategoryDto(request.categoryId(), "testカテゴリ"),
                List.of(new SkuDto(
                        101L,
                        request.skus().get(0).size(),
                        request.skus().get(0).color(),
                        request.skus().get(0).extraPrice(),
                        new InventoryDto(10)
                )),
                null, null
        );

        when(productService.createProduct(any(ProductCreateRequestDto.class)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/admin/products")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("test商品"))
                .andExpect(jsonPath("$.category.id").value(1))
                .andExpect(jsonPath("$.description").value("商品説明"))
                .andExpect(jsonPath("$.imageUrl").value("image.jpg"))
                .andExpect(jsonPath("$.price").value(1000))
                .andExpect(jsonPath("$.skus[0].size").value("S"))
                .andExpect(jsonPath("$.skus[0].color").value("Red"))
                .andExpect(jsonPath("$.skus[0].inventory.quantity").value(10));
    }

    @Test
    @DisplayName("Admin以外のユーザーが商品登録しようとすると403エラーが返されること")
    @WithMockUser(roles = "USER")
    void createProduct_return403_whenUserIsNotAdmin() throws Exception {
        // Arrange
        var request = new ProductCreateRequestDto(
                "test商品", 1, "商品説明", "image.jpg", 1000,
                List.of(new SkuCreateRequestDto("S", "Red", 100, 10)));

        // Act & Assert
        mockMvc.perform(post("/api/admin/products")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("商品名が空の場合、400エラーが返されること")
    @WithMockUser(roles = "ADMIN")
    void createProduct_return400_whenNameIsEmpty() throws Exception {
        // Arrange
        var request = new ProductCreateRequestDto(
                "", 1, "商品説明", "image.jpg", 1000,
                List.of(new SkuCreateRequestDto("S", "Red", 100, 10)));

        // Act & Assert
        mockMvc.perform(post("/api/admin/products")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", allOf(
                        containsString("商品名は必須です"),
                        containsString("商品名は1文字以上255文字以下で入力してください")
                )));
    }

    @Test
    @DisplayName("商品名が255文字を超える場合、400エラーが返されること")
    @WithMockUser(roles = "ADMIN")
    void createProduct_return400_whenNameExceedsMaxLength() throws Exception {
        // Arrange
        String longName = "a".repeat(256); // 256文字の文字列
        var request = new ProductCreateRequestDto(
                longName, 1, "商品説明", "image.jpg", 1000,
                List.of(new SkuCreateRequestDto("S", "Red", 100, 10)));

        // Act & Assert
        mockMvc.perform(post("/api/admin/products")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message",
                        containsString("商品名は1文字以上255文字以下で入力してください")
                ));
    }

    @Test
    @DisplayName("商品価格が0未満の場合、400エラーが返されること")
    @WithMockUser(roles = "ADMIN")
    void createProduct_return400_whenPriceIsZeroOrNegative() throws Exception {
        // Arrange
        var request = new ProductCreateRequestDto(
                "test商品", 1, "商品説明", "image.jpg", -1,
                List.of(new SkuCreateRequestDto("S", "Red", 100, 10)));

        // Act & Assert
        mockMvc.perform(post("/api/admin/products")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("商品価格が1000000を超える場合、400エラーが返されること")
    @WithMockUser(roles = "ADMIN")
    void createProduct_return400_whenPriceExceedsMaxValue() throws Exception {
        // Arrange
        var request = new ProductCreateRequestDto(
                "test商品", 1, "商品説明", "image.jpg", 1000001,
                List.of(new SkuCreateRequestDto("S", "Red", 100, 10)));

        // Act & Assert
        mockMvc.perform(post("/api/admin/products")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("カテゴリIDがnullの場合、400エラーが返されること")
    @WithMockUser(roles = "ADMIN")
    void createProduct_return400_whenCategoryIdIsNull() throws Exception {
        // Arrange
        var request = new ProductCreateRequestDto(
                "test商品", null, "商品説明", "image.jpg", 1000,
                List.of(new SkuCreateRequestDto("S", "Red", 100, 10)));

        // Act & Assert
        mockMvc.perform(post("/api/admin/products")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("カテゴリIDは必須です")));

    }

    @Test
    @DisplayName("skusが空の場合、400エラーが返されること")
    @WithMockUser(roles = "ADMIN")
    void createProduct_return400_whenSkusIsEmpty() throws Exception {
        // Arrange
        var request = new ProductCreateRequestDto(
                "test商品", 1, "商品説明", "image.jpg", 1000,
                List.of());
        // Act & Assert
        mockMvc.perform(post("/api/admin/products")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Adminユーザーが商品詳細を取得できること")
    @WithMockUser(roles = "ADMIN")
    void getProductDetail_return200AndProductDetail_whenRequestCorrect() throws Exception {
        // Arrange
        var responseDto = new ProductDetailDto(
                1L,
                "test商品",
                1000,
                "商品説明",
                "image.jpg",
                new CategoryDto(1, "testカテゴリ"),
                List.of(new SkuDto(
                        101L,
                        "S",
                        "Red",
                        100,
                        new InventoryDto(10)
                )),
                null, null
        );

        when(productService.getProductDetail(1L)).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(get("/api/admin/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("test商品"))
                .andExpect(jsonPath("$.price").value(1000))
                .andExpect(jsonPath("$.description").value("商品説明"))
                .andExpect(jsonPath("$.imageUrl").value("image.jpg"))
                .andExpect(jsonPath("$.category.id").value(1))
                .andExpect(jsonPath("$.category.name").value("testカテゴリ"))
                .andExpect(jsonPath("$.skus[0].id").value(101))
                .andExpect(jsonPath("$.skus[0].size").value("S"))
                .andExpect(jsonPath("$.skus[0].color").value("Red"))
                .andExpect(jsonPath("$.skus[0].extraPrice").value(100))
                .andExpect(jsonPath("$.skus[0].inventory.quantity").value(10));
    }

    @Test
    @DisplayName("存在しない商品IDで商品詳細を取得しようとすると404エラーが返されること")
    @WithMockUser(roles = "ADMIN")
    void getProductDetail_return404_whenProductNotFound() throws Exception {
        // Arrange
        when(productService.getProductDetail(999L))
                .thenThrow(new ResourceNotFoundException("商品が見つかりません。ID: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/admin/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("商品が見つかりません。ID: 999"));
    }

    @Test
    @DisplayName("Adminユーザーが商品を更新できること")
    @WithMockUser(roles = "ADMIN")
    void updateProduct_return200AndUpdatedProduct_whenRequestCorrect() throws Exception {
        // Arrange
        Long productId = 1L;
        var request = new ProductUpdateRequestDto(
                "updated商品", 1500, "更新された商品説明", "updated_image.jpg",
                1
        );

        var responseDto = new ProductDetailDto(
                productId,
                request.name(),
                request.price(),
                request.description(),
                request.imageUrl(),
                new CategoryDto(request.categoryId(), "testカテゴリ"),
                List.of(new SkuDto(
                        101L,
                        "M",
                        "Blue",
                        200,
                        new InventoryDto(20)
                )),
                null, null
        );

        when(productService.updateProduct(productId, request)).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(put("/api/admin/products/{id}", productId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.name").value("updated商品"))
                .andExpect(jsonPath("$.price").value(1500))
                .andExpect(jsonPath("$.description").value("更新された商品説明"))
                .andExpect(jsonPath("$.imageUrl").value("updated_image.jpg"))
                .andExpect(jsonPath("$.category.id").value(1))
                .andExpect(jsonPath("$.category.name").value("testカテゴリ"))
                .andExpect(jsonPath("$.skus[0].id").value(101))
                .andExpect(jsonPath("$.skus[0].size").value("M"))
                .andExpect(jsonPath("$.skus[0].color").value("Blue"))
                .andExpect(jsonPath("$.skus[0].extraPrice").value(200))
                .andExpect(jsonPath("$.skus[0].inventory.quantity").value(20));
    }

    @Test
    @DisplayName("存在しない商品IDで商品を更新しようとすると404エラーが返されること")
    @WithMockUser(roles = "ADMIN")
    void updateProduct_return404_whenProductNotFound() throws Exception {
        // Arrange
        Long productId = 999L;
        var request = new ProductUpdateRequestDto(
                "updated商品", 1500, "更新された商品説明", "updated_image.jpg",
                1
        );

        when(productService.updateProduct(productId, request))
                .thenThrow(new ResourceNotFoundException("商品が見つかりません。ID: " + productId));

        // Act & Assert
        mockMvc.perform(put("/api/admin/products/{id}", productId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("商品が見つかりません。ID: 999"));
    }

    @Test
    @DisplayName("Adminユーザーが商品を削除できること")
    @WithMockUser(roles = "ADMIN")
    void deleteProduct_return204_whenRequestCorrect() throws Exception {
        // Arrange
        Long productId = 1L;

        // Act & Assert
        mockMvc.perform(delete("/api/admin/products/{id}", productId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("存在しない商品IDで商品を削除しようとすると404エラーが返されること")
    @WithMockUser(roles = "ADMIN")
    void deleteProduct_return404_whenProductNotFound() throws Exception {
        // Arrange
        Long productId = 999L;

        doThrow(new ResourceNotFoundException("商品が見つかりません。ID: " + productId))
                .when(productService).deleteProduct(productId);

        // Act & Assert
        mockMvc.perform(delete("/api/admin/products/{id}", productId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("商品が見つかりません。ID: 999"));
    }

    @Test
    @DisplayName("AdminユーザーがSKUを作成できること")
    @WithMockUser(roles = "ADMIN")
    void createSku_return201AndSku_whenRequestCorrect() throws Exception {
        // Arrange
        Long productId = 1L;
        var request = new SkuCreateRequestDto("M", "Blue", 200, 20);

        var responseDto = new SkuDto(
                101L,
                request.size(),
                request.color(),
                request.extraPrice(),
                new InventoryDto(request.quantity())
        );

        when(productService.createSku(productId, request)).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/admin/products/{id}/skus", productId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(101))
                .andExpect(jsonPath("$.size").value("M"))
                .andExpect(jsonPath("$.color").value("Blue"))
                .andExpect(jsonPath("$.extraPrice").value(200))
                .andExpect(jsonPath("$.inventory.quantity").value(20));
    }

    @Test
    @DisplayName("存在しない商品IDでSKUを作成しようとすると404エラーが返されること")
    @WithMockUser(roles = "ADMIN")
    void createSku_return404_whenProductNotFound() throws Exception {
        // Arrange
        Long productId = 999L;
        var request = new SkuCreateRequestDto("M", "Blue", 200, 20);
        when(productService.createSku(productId, request))
                .thenThrow(new ResourceNotFoundException("商品が見つかりません。ID: " + productId));

        // Act & Assert
        mockMvc.perform(post("/api/admin/products/{id}/skus", productId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("商品が見つかりません。ID: 999"));
    }

}