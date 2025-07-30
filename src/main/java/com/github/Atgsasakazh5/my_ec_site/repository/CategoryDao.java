package com.github.Atgsasakazh5.my_ec_site.repository;

import com.github.Atgsasakazh5.my_ec_site.entity.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryDao {

    List<Category> findAll();
    Category update(Integer id, String name);
    void delete(Integer id);
    Category save(String name);
    Optional<Category> findById(Integer id);
    Optional<Category> findByName(String name);
}
