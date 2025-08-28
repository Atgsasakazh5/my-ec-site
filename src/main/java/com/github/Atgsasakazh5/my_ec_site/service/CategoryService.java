package com.github.Atgsasakazh5.my_ec_site.service;

import com.github.Atgsasakazh5.my_ec_site.dto.CategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto createCategory(String name);
    List<CategoryDto> getAllCategories();
    CategoryDto updateCategory(Integer id, String name);
    void deleteCategory(Integer id);
    CategoryDto searchCategoryById(Integer id);
}