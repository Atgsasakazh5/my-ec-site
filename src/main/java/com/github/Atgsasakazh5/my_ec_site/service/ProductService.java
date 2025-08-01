package com.github.Atgsasakazh5.my_ec_site.service;

import com.github.Atgsasakazh5.my_ec_site.dto.*;
import com.github.Atgsasakazh5.my_ec_site.entity.Inventory;
import com.github.Atgsasakazh5.my_ec_site.entity.Product;
import com.github.Atgsasakazh5.my_ec_site.entity.Sku;
import com.github.Atgsasakazh5.my_ec_site.repository.CategoryDao;
import com.github.Atgsasakazh5.my_ec_site.repository.InventoryDao;
import com.github.Atgsasakazh5.my_ec_site.repository.ProductDao;
import com.github.Atgsasakazh5.my_ec_site.repository.SkuDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductDao productDao;
    private final SkuDao skuDao;
    private final InventoryDao inventoryDao;
    private final CategoryDao categoryDao;

    public ProductService(ProductDao productDao, SkuDao skuDao, InventoryDao inventoryDao, CategoryDao categoryDao) {
        this.productDao = productDao;
        this.skuDao = skuDao;
        this.inventoryDao = inventoryDao;
        this.categoryDao = categoryDao;
    }

    @Transactional
    public ProductDetailDto createProduct(ProductCreateRequestDto requestDto) {
        // 1. 商品を保存
        Product product = new Product();
        product.setName(requestDto.name());
        product.setPrice(requestDto.price());
        product.setDescription(requestDto.description());
        product.setImageUrl(requestDto.imageUrl());
        product.setCategoryId(requestDto.categoryId());
        Product savedProduct = productDao.save(product);

        // 2. SKUと在庫をループ内で同時に保存し、レスポンス用DTOのリストを作成
        List<SkuDto> skuDtos = requestDto.skus().stream().map(skuRequest -> {
            // SKUを保存
            Sku sku = new Sku();
            sku.setProductId(savedProduct.getId());
            sku.setSize(skuRequest.size());
            sku.setColor(skuRequest.color());
            sku.setExtraPrice(skuRequest.extraPrice());
            Sku savedSku = skuDao.save(sku);

            // 在庫を保存
            Inventory inventory = new Inventory();
            inventory.setSkuId(savedSku.getId());
            inventory.setQuantity(skuRequest.quantity()); // DTO名はSkuCreateRequestDtoのquantity()
            Inventory savedInventory = inventoryDao.save(inventory);

            // レスポンス用のSkuDtoを組み立て
            InventoryDto inventoryDto = new InventoryDto(savedInventory.getQuantity());
            return new SkuDto(savedSku.getId(), savedSku.getSize(), savedSku.getColor(), savedSku.getExtraPrice(), inventoryDto);
        }).collect(Collectors.toList());

        // 3. 最終的なProductDetailDtoを組み立てて返す
        CategoryDto categoryDto = categoryDao.findById(savedProduct.getCategoryId())
                .map(c -> new CategoryDto(c.getId(), c.getName()))
                .orElse(null);

        return new ProductDetailDto(
                savedProduct.getId(),
                savedProduct.getName(),
                savedProduct.getDescription(),
                savedProduct.getImageUrl(),
                categoryDto,
                skuDtos
        );
    }
}
