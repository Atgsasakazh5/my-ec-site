package com.github.Atgsasakazh5.my_ec_site.service;

import com.github.Atgsasakazh5.my_ec_site.entity.Category;
import com.github.Atgsasakazh5.my_ec_site.exception.ResourceNotFoundException;
import com.github.Atgsasakazh5.my_ec_site.repository.CategoryDao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Mock
    private CategoryDao categoryDao;

    @Test
    @DisplayName("カテゴリが登録され、dtoで返されること-正常系")
    void createCategory_shouldReturnCategoryDto_whenCategoryIsCreated() {
        // Arrange
        String categoryName = "トップス";
        var mockCategory = new Category(1, categoryName);

        when(categoryDao.findByName(categoryName)).thenReturn(Optional.empty());
        when(categoryDao.save(categoryName)).thenReturn(mockCategory).thenReturn(mockCategory);

        // Act
        var createdCategory = categoryService.createCategory(categoryName);

        // Assert
        assertNotNull(createdCategory);
        assertEquals(1, createdCategory.id());
        assertEquals(categoryName, createdCategory.name());
    }

    @Test
    @DisplayName("既に存在するカテゴリ名で登録しようとするとIllegalStateExceptionが発生すること-異常系")
    void createCategory_shouldThrowIllegalStateException_whenCategoryNameAlreadyExists() {
        // Arrange
        String categoryName = "トップス";
        when(categoryDao.findByName(categoryName)).thenReturn(Optional.of(new Category(1, categoryName)));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> categoryService.createCategory(categoryName));
    }

    @Test
    @DisplayName("全てのカテゴリが取得できること")
    void getAllCategories_shouldReturnListOfCategoryDto() {

        // Arrange
        var mockCategory1 = new Category(1, "トップス");
        var mockCategory2 = new Category(2, "ボトムス");
        when(categoryDao.findAll()).thenReturn(List.of(mockCategory1, mockCategory2));

        // Act
        var categories = categoryService.getAllCategories();

        // Assert
        assertNotNull(categories);
        assertEquals(2, categories.size());
        assertEquals("トップス", categories.get(0).name());
        assertEquals("ボトムス", categories.get(1).name());
    }

    @Test
    @DisplayName("存在するカテゴリを更新できること-正常系")
    void updateCategory_shouldReturnUpdatedCategoryDto_whenCategoryExists() {
        // Arrange
        int categoryId = 1;
        String newCategoryName = "アウター";
        var existingCategory = new Category(categoryId, "トップス");
        var updatedCategory = new Category(categoryId, newCategoryName);

        when(categoryDao.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        when(categoryDao.update(categoryId, newCategoryName)).thenReturn(updatedCategory);

        // Act
        var result = categoryService.updateCategory(categoryId, newCategoryName);

        // Assert
        assertNotNull(result);
        assertEquals(categoryId, result.id());
        assertEquals(newCategoryName, result.name());

    }

    @Test
    @DisplayName("存在しないカテゴリを更新しようとするとResourceNotFoundExceptionが発生すること-異常系")
    void updateCategory_shouldThrowResourceNotFoundException_whenCategoryDoesNotExist() {
        // Arrange
        int categoryId = 999;
        String newCategoryName = "アウター";

        when(categoryDao.findById(categoryId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> categoryService.updateCategory(categoryId, newCategoryName));
    }


    @Test
    @DisplayName("存在するカテゴリを削除できること-正常系")
    void deleteCategory_shouldDeleteCategory_whenCategoryExists() {
        // Arrange
        int categoryId = 1;
        var existingCategory = new Category(categoryId, "トップス");
        when(categoryDao.findById(categoryId)).thenReturn(Optional.of(existingCategory));

        // Act
        categoryService.deleteCategory(categoryId);

        // Assert
        verify(categoryDao, times(1)).delete(categoryId);
    }

    @Test
    @DisplayName("存在しないカテゴリを削除しようとするとResourceNotFoundExceptionが発生すること-異常系")
    void deleteCategory_shouldThrowResourceNotFoundException_whenCategoryDoesNotExist() {
        // Arrange
        int categoryId = 999;
        when(categoryDao.findById(categoryId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteCategory(categoryId));
    }

    @Test
    @DisplayName("IDでカテゴリを検索し、dtoで返されること-正常系")
    void searchCategoryById_shouldReturnCategoryDto_whenCategoryExists() {
        // Arrange
        int categoryId = 1;
        var existingCategory = new Category(categoryId, "トップス");
        when(categoryDao.findById(categoryId)).thenReturn(Optional.of(existingCategory));

        // Act
        var result = categoryService.searchCategoryById(categoryId);

        // Assert
        assertNotNull(result);
        assertEquals(categoryId, result.id());
        assertEquals("トップス", result.name());
    }

    @Test
    @DisplayName("存在しないIDでカテゴリを検索しようとするとResourceNotFoundExceptionが発生すること-異常系")
    void searchCategoryById_shouldThrowResourceNotFoundException_whenCategoryDoesNotExist() {
        // Arrange
        int categoryId = 999;
        when(categoryDao.findById(categoryId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> categoryService.searchCategoryById(categoryId));
    }
}