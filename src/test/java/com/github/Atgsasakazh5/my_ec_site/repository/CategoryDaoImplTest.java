package com.github.Atgsasakazh5.my_ec_site.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@JdbcTest
@Import(CategoryDaoImpl.class)
@ActiveProfiles("h2")
class CategoryDaoImplTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private CategoryDaoImpl categoryDao;

    @Test
    @DisplayName("カテゴリー一覧を取得できること")
    void findAll_shouldReturnCategoryList() {

        // Arrange
        jdbcTemplate.update("INSERT INTO categories (name) VALUES (?);", "Test Category 1");
        jdbcTemplate.update("INSERT INTO categories (name) VALUES (?);", "Test Category 2");

        // Act
        var categories = categoryDao.findAll();

        // Assert
        assertThat(categories).isNotNull();
        assertThat(categories.size()).isEqualTo(2);
        assertThat(categories.get(0).getName()).isEqualTo("Test Category 1");
        assertThat(categories.get(1).getName()).isEqualTo("Test Category 2");


    }

    @Test
    @DisplayName("存在するIDでカテゴリーを更新できること")
    void update_shouldUpdate_whenIdExits() {
        // Arrange
        jdbcTemplate.update("INSERT INTO categories (name) VALUES (?);", "Old Category Name");
        Integer id = jdbcTemplate.queryForObject("SELECT id FROM categories WHERE name = ?", Integer.class, "Old Category Name");

        // Act
        var updatedCategory = categoryDao.update(id, "Updated Category Name");

        // Assert
        assertThat(updatedCategory).isNotNull();
        assertThat(updatedCategory.getId()).isEqualTo(id);
        assertThat(updatedCategory.getName()).isEqualTo("Updated Category Name");
    }

    @Test
    @DisplayName("存在するIDでカテゴリーを削除できること")
    void delete_shouldDelete_whenIdExists() {
        // Arrange
        jdbcTemplate.update("INSERT INTO categories (name) VALUES (?);", "Category to Delete");
        Integer id = jdbcTemplate.queryForObject("SELECT id FROM categories WHERE name = ?", Integer.class, "Category to Delete");

        // Act
        categoryDao.delete(id);

        // Assert
        var category = categoryDao.findById(id);
        assertThat(category).isEmpty();
    }

    @Test
    @DisplayName("カテゴリーを保存できること-正常系")
    void save_shouldReturnSavedCategory() {
        //Arange
        String categoryName = "New Category";

        // Act
        var savedCategory = categoryDao.save(categoryName);

        // Assert
        assertThat(savedCategory).isNotNull();
        assertThat(savedCategory.getName()).isEqualTo(categoryName);
        assertThat(savedCategory.getId()).isNotNull();

    }

    @Test
    @DisplayName("既に存在するカテゴリ名を渡された時にDataIntegrityViolationExceptionをスローすること-異常系")
    void save_shouldThrowDataIntegrityViolationException_whenCategoryNameExists() {
        // Arrange
        String categoryName = "Existing Category";
        jdbcTemplate.update("INSERT INTO categories (name) VALUES (?);", categoryName);

        // Act & Assert
        assertThatThrownBy(() -> categoryDao.save(categoryName))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("カテゴリ名にnullが渡されたとき、DataIntegrityViolationExceptionをスローすること-異常系")
    void save_shouldThrowDataIntegrityViolationException_whenCategoryNameIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> categoryDao.save(null))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("存在するIDでカテゴリーを取得できること")
    void findById_shouldReturnCategory_whenIdExists() {

        // Arrange
        jdbcTemplate.update("INSERT INTO categories (name) VALUES (?);", "Category to Find");
        Integer id = jdbcTemplate.queryForObject("SELECT id FROM categories WHERE name = ?", Integer.class, "Category to Find");

        // Act
        var category = categoryDao.findById(id);

        // Assert
        assertThat(category).isPresent();
        assertThat(category.get().getId()).isEqualTo(id);
        assertThat(category.get().getName()).isEqualTo("Category to Find");

    }

    @Test
    @DisplayName("存在しないIDでカテゴリーを取得すると空が返ること")
    void findById_shouldReturnEmpty_whenIdDoesNotExist() {
        // Act
        var category = categoryDao.findById(9999); // Assuming 9999 does not exist
        // Assert
        assertThat(category).isEmpty();
    }

    @Test
    @DisplayName("存在する名前でカテゴリーを取得できること")
    void findByName_shouldReturnCategory_whenNameExists() {
        // Arrange
        String categoryName = "カテゴリ名テスト";
        jdbcTemplate.update("INSERT INTO categories (name) VALUES (?);", categoryName);

        // Act
        var category = categoryDao.findByName(categoryName);

        // Assert
        assertThat(category).isPresent();
        assertThat(category.get().getName()).isEqualTo(categoryName);
    }

    @Test
    @DisplayName("存在しない名前でカテゴリーを取得すると空が返ること")
    void findByName_shouldReturnEmpty_whenNameDoesNotExist() {
        // Act
        var category = categoryDao.findByName("Nonexistent Category");
        // Assert
        assertThat(category).isEmpty();
    }
}