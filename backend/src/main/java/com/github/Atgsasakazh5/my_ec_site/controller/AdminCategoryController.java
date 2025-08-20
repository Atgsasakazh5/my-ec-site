package com.github.Atgsasakazh5.my_ec_site.controller;

import com.github.Atgsasakazh5.my_ec_site.dto.CategoryDto;
import com.github.Atgsasakazh5.my_ec_site.dto.CategoryRequestDto;
import com.github.Atgsasakazh5.my_ec_site.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    //新規カテゴリーの作成
    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CategoryRequestDto request) {
        CategoryDto createdCategory = categoryService.createCategory(request.name());
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }

    //カテゴリ一覧の取得
    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        List<CategoryDto> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    //カテゴリーの更新
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> updateCategory(@PathVariable Integer id,
                                                      @Valid @RequestBody CategoryRequestDto request) {
        CategoryDto updatedCategory = categoryService.updateCategory(id, request.name());
        return ResponseEntity.ok(updatedCategory);
    }

    //カテゴリーの削除
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Integer id) {
        categoryService.deleteCategory(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }
}
