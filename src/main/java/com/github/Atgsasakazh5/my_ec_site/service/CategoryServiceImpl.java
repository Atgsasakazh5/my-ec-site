package com.github.Atgsasakazh5.my_ec_site.service;

import com.github.Atgsasakazh5.my_ec_site.dto.CategoryDto;
import com.github.Atgsasakazh5.my_ec_site.exception.ResourceNotFoundException;
import com.github.Atgsasakazh5.my_ec_site.repository.CategoryDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryDao categoryDao;

    public CategoryServiceImpl(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }

    @Override
    @Transactional
    public CategoryDto createCategory(String name) {
        //既に同じ名前のカテゴリーが存在する場合は、例外をスロー
        if(categoryDao.findByName(name).isPresent()){
            throw new IllegalStateException("カテゴリー名はすでに存在します: " + name);
        }

        var category = categoryDao.save(name);
        return new CategoryDto(category.getId(), category.getName());
    }

    @Override
    public List<CategoryDto> getAllCategories() {
        return categoryDao.findAll().stream()
                .map(category -> new CategoryDto(category.getId(), category.getName()))
                .toList();
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Integer id, String name) {
        categoryDao.findById(id).orElseThrow(() -> new ResourceNotFoundException("カテゴリーが見つかりません。 ID: " + id));

        var updatedCategory = categoryDao.update(id, name);
        return new CategoryDto(updatedCategory.getId(), updatedCategory.getName());
    }

    @Override
    @Transactional
    public void deleteCategory(Integer id) {
        categoryDao.findById(id).orElseThrow(() -> new ResourceNotFoundException("カテゴリーが見つかりません。 ID: " + id));
        categoryDao.delete(id);
    }

    @Override
    public CategoryDto searchCategoryById(Integer id) {
        var category = categoryDao.findById(id).orElseThrow(() -> new ResourceNotFoundException("カテゴリーが見つかりません。 ID: " + id));
        return new CategoryDto(category.getId(), category.getName());
    }
}
