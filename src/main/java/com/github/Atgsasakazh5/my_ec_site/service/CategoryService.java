package com.github.Atgsasakazh5.my_ec_site.service;

import com.github.Atgsasakazh5.my_ec_site.dto.CategoryDto;
import com.github.Atgsasakazh5.my_ec_site.repository.CategoryDaoImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final CategoryDaoImpl categoryDao;

    public CategoryService(CategoryDaoImpl categoryDao) {
        this.categoryDao = categoryDao;
    }

    @Transactional
    public CategoryDto createCategory(String name) {
        var category = categoryDao.save(name);
        return new CategoryDto(category.getId(), category.getName());
    }


}
