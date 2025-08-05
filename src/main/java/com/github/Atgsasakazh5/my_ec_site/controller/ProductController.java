package com.github.Atgsasakazh5.my_ec_site.controller;

import com.github.Atgsasakazh5.my_ec_site.dto.PageResponseDto;
import com.github.Atgsasakazh5.my_ec_site.dto.ProductDetailDto;
import com.github.Atgsasakazh5.my_ec_site.dto.ProductSummaryDto;
import com.github.Atgsasakazh5.my_ec_site.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<PageResponseDto<ProductSummaryDto>> getProductSummaries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponseDto<ProductSummaryDto> productSummaries = productService.findAllPaginated(page, size);
        return ResponseEntity.ok(productSummaries);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailDto> getProductDetail(
            @PathVariable Long productId) {

        ProductDetailDto productDetail = productService.getProductDetail(productId);
        return ResponseEntity.ok(productDetail);
    }
}
