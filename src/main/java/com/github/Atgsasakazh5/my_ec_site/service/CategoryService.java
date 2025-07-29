package com.github.Atgsasakazh5.my_ec_site.service;

import com.github.Atgsasakazh5.my_ec_site.dto.CategoryDto;
import com.github.Atgsasakazh5.my_ec_site.exception.ResourceNotFoundException;
import com.github.Atgsasakazh5.my_ec_site.repository.CategoryDao;
import com.github.Atgsasakazh5.my_ec_site.repository.CategoryDaoImpl;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryDao categoryDao;

    public CategoryService(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }

    @Transactional
    public CategoryDto createCategory(String name) {
        var category = categoryDao.save(name);
        return new CategoryDto(category.getId(), category.getName());
    }

    public List<CategoryDto> getAllCategories() {
        return categoryDao.findAll().stream()
                .map(category -> new CategoryDto(category.getId(), category.getName()))
                .toList();
    }

    @Transactional
    public CategoryDto updateCategory(Integer id, String name) {
        categoryDao.findById(id).orElseThrow(() -> new ResourceNotFoundException("カテゴリーが見つかりません。 ID: " + id));

        var updatedCategory = categoryDao.update(id, name);
        return new CategoryDto(updatedCategory.getId(), updatedCategory.getName());
    }

    @Transactional
    public void deleteCategory(Integer id) {
        categoryDao.findById(id).orElseThrow(() -> new ResourceNotFoundException("カテゴリーが見つかりません。 ID: " + id));
        categoryDao.delete(id);
    }

}
