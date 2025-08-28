package com.github.Atgsasakazh5.my_ec_site.service;

import com.github.Atgsasakazh5.my_ec_site.dto.*;

import java.util.List;

public interface ProductService {
    ProductDetailDto createProduct(ProductCreateRequestDto requestDto);
    ProductDetailDto updateProduct(Long productId, ProductUpdateRequestDto requestDto);
    ProductDetailDto getProductDetail(Long productId);
    List<ProductDetailDto> getAllProducts();
    PageResponseDto<ProductSummaryDto> findAllPaginated(int page, int size);
    void deleteProduct(Long productId);
    SkuDto createSku(Long productId, SkuCreateRequestDto requestDto);
    SkuDto updateSku(Long skuId, SkuUpdateRequestDto requestDto);
    SkuDto getSkuDetail(Long skuId);
    List<SkuDto> getSkusByProductId(Long productId);
    void deleteSku(Long skuId);
    PageResponseDto<ProductSummaryDto> searchProductsByCategory(int categoryId, int page, int size);
}