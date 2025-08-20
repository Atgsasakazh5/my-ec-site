package com.github.Atgsasakazh5.my_ec_site.controller;

import com.github.Atgsasakazh5.my_ec_site.dto.SkuDto;
import com.github.Atgsasakazh5.my_ec_site.dto.SkuUpdateRequestDto;
import com.github.Atgsasakazh5.my_ec_site.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/skus")
public class AdminSkuController {

    private final ProductService productService;

    public AdminSkuController(ProductService productService) {
        this.productService = productService;
    }

    @PutMapping("/{skuId}")
    public ResponseEntity<SkuDto> updateSku(@PathVariable Long skuId,
                                            @Valid @RequestBody SkuUpdateRequestDto request) {

        var updatedSku = productService.updateSku(skuId, request);
        return ResponseEntity.ok(updatedSku);
    }

    @DeleteMapping("/{skuId}")
    public ResponseEntity<Void> deleteSku(@PathVariable Long skuId) {
        productService.deleteSku(skuId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
