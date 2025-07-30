package com.github.Atgsasakazh5.my_ec_site.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.Atgsasakazh5.my_ec_site.dto.CategoryDto;
import com.github.Atgsasakazh5.my_ec_site.dto.CategoryRequestDto;
import com.github.Atgsasakazh5.my_ec_site.exception.ResourceNotFoundException;
import com.github.Atgsasakazh5.my_ec_site.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
//@ActiveProfiles("h2")
class AdminCategoryControllerTest {

    @MockitoBean
    CategoryService categoryService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("adminユーザーがカテゴリーを作成できること")
    @WithMockUser(roles = "ADMIN")
    void createCategory_return201AndCategory_whenRequestCorrect() throws Exception {
        // Arrange
        var request = new CategoryRequestDto("ボトムス");
        var expectedResponse = new CategoryDto(1, "ボトムス");

        when(categoryService.createCategory(request.name())).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/api/admin/categories")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(expectedResponse.id()))
                .andExpect(jsonPath("$.name").value(expectedResponse.name()));
    }

    @Test
    @DisplayName("admin以外のユーザーがカテゴリーを作成しようとすると403エラーが返されること")
    @WithMockUser(roles = "USER")
    void createCategory_return403_whenUserIsNotAdmin() throws Exception {
        // Arrange
        var request = new CategoryRequestDto("ボトムス");

        // Act & Assert
        mockMvc.perform(post("/api/admin/categories")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("カテゴリ名が空の場合、400エラーが返されること")
    @WithMockUser(roles = "ADMIN")
    void createCategory_return400_whenNameIsEmpty() throws Exception {
        // Arrange
        var request = new CategoryRequestDto("");

        // Act & Assert
        mockMvc.perform(post("/api/admin/categories")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("name: カテゴリー名は必須です"));
    }

    @Test
    @DisplayName("カテゴリ名が50文字を超える場合、400エラーが返されること")
    @WithMockUser(roles = "ADMIN")
    void createCategory_return400_whenNameExceedsMaxLength() throws Exception {
        // Arrange
        String longName = "a".repeat(51); // 51文字の文字列
        var request = new CategoryRequestDto(longName);

        // Act & Assert
        mockMvc.perform(post("/api/admin/categories")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("name: カテゴリー名は50文字以内で入力してください"));
    }

    @Test
    @DisplayName("カテゴリー名が重複する場合、409エラーが返されること")
    @WithMockUser(roles = "ADMIN")
    void createCategory_return409_whenNameAlreadyExists() throws Exception {
        // Arrange
        String name = "ボトムス";
        var request = new CategoryRequestDto(name);

        doThrow(new IllegalStateException("カテゴリー名はすでに存在します: " + name))
                .when(categoryService).createCategory(name);

        // Act & Assert
        mockMvc.perform(post("/api/admin/categories")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("カテゴリー名はすでに存在します: " + name));
    }

    @Test
    @DisplayName("カテゴリー一覧の取得が成功すること")
    @WithMockUser(roles = "ADMIN")
    void getAllCategories_return200AndCategoryList() throws Exception {
        // Arrange
        var category1 = new CategoryDto(1, "トップス");
        var category2 = new CategoryDto(2, "ボトムス");
        when(categoryService.getAllCategories()).thenReturn(List.of(category1, category2));

        // Act & Assert
        mockMvc.perform(get("/api/admin/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(category1.id()))
                .andExpect(jsonPath("$[0].name").value(category1.name()))
                .andExpect(jsonPath("$[1].id").value(category2.id()))
                .andExpect(jsonPath("$[1].name").value(category2.name()));
    }

    @Test
    @DisplayName("存在するカテゴリーの更新が成功すること")
    @WithMockUser(roles = "ADMIN")
    void updateCategory_return200AndUpdatedCategory_whenRequestCorrect() throws Exception {
        // Arrange
        int categoryId = 1;
        var request = new CategoryRequestDto("新しいボトムス");
        var expectedResponse = new CategoryDto(categoryId, "新しいボトムス");

        when(categoryService.updateCategory(categoryId, request.name())).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/admin/categories/{id}", categoryId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedResponse.id()))
                .andExpect(jsonPath("$.name").value(expectedResponse.name()));
    }

    @Test
    @DisplayName("存在しないカテゴリーの更新を試みると404エラーが返されること")
    @WithMockUser(roles = "ADMIN")
    void updateCategory_return404_whenCategoryNotFound() throws Exception {
        // Arrange
        int categoryId = 999; // 存在しないカテゴリーID
        var request = new CategoryRequestDto("新しいボトムス");
        doThrow(new ResourceNotFoundException("カテゴリーが見つかりません。ID: " + categoryId))
                .when(categoryService).updateCategory(categoryId, request.name());

        // Act & Assert
        mockMvc.perform(put("/api/admin/categories/{id}", categoryId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("カテゴリーが見つかりません。ID: " + categoryId));
    }

    @Test
    @DisplayName("存在するカテゴリーの削除が成功すること")
    @WithMockUser(roles = "ADMIN")
    void deleteCategory_return204_whenCategoryExists() throws Exception {
        // Arrange
        int categoryId = 1;

        // Act & Assert
        mockMvc.perform(delete("/api/admin/categories/{id}", categoryId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("存在しないカテゴリーの削除を試みると404エラーが返されること")
    @WithMockUser(roles = "ADMIN")
    void deleteCategory_return404_whenCategoryNotFound() throws Exception {
        // Arrange
        int categoryId = 999; // 存在しないカテゴリーID
        doThrow(new ResourceNotFoundException("カテゴリーが見つかりません。ID: " + categoryId))
                .when(categoryService).deleteCategory(categoryId);

        // Act & Assert
        mockMvc.perform(delete("/api/admin/categories/{id}", categoryId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("カテゴリーが見つかりません。ID: " + categoryId));
    }
}