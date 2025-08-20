package com.github.Atgsasakazh5.my_ec_site.controller;

import com.github.Atgsasakazh5.my_ec_site.dto.CategoryDto;
import com.github.Atgsasakazh5.my_ec_site.dto.PageResponseDto;
import com.github.Atgsasakazh5.my_ec_site.dto.ProductSummaryDto;
import com.github.Atgsasakazh5.my_ec_site.service.CategoryService;
import com.github.Atgsasakazh5.my_ec_site.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private CategoryService categoryService;

    private ProductService productService;

    public CategoryController(CategoryService categoryService, ProductService productService) {
        this.categoryService = categoryService;
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        List<CategoryDto> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{categoryId}/products")
    public ResponseEntity<PageResponseDto<ProductSummaryDto>> getProductsByCategoryId(
            @PathVariable int categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponseDto<ProductSummaryDto> products = productService.searchProductsByCategory(categoryId, page, size);
        return ResponseEntity.ok(products);
    }

}
