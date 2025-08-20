package com.github.Atgsasakazh5.my_ec_site.controller;

import com.github.Atgsasakazh5.my_ec_site.dto.*;
import com.github.Atgsasakazh5.my_ec_site.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductDetailDto> createProduct(@Valid @RequestBody ProductCreateRequestDto request) {
        var createdProduct = productService.createProduct(request);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailDto> getProductDetail(@PathVariable Long id) {
        var productDetail = productService.getProductDetail(id);
        return ResponseEntity.ok(productDetail);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDetailDto> updateProduct(@PathVariable Long id,
                                                          @Valid @RequestBody ProductUpdateRequestDto request) {
        var updatedProduct = productService.updateProduct(id, request);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{id}/skus")
    public ResponseEntity<SkuDto> createSku(@PathVariable Long id,
                                            @Valid @RequestBody SkuCreateRequestDto request) {

        var createdSku = productService.createSku(id, request);
        return new ResponseEntity<>(createdSku, HttpStatus.CREATED);
    }
}
