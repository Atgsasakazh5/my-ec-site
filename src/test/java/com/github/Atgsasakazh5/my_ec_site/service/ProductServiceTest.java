package com.github.Atgsasakazh5.my_ec_site.service;

import com.github.Atgsasakazh5.my_ec_site.dto.InventoryDto;
import com.github.Atgsasakazh5.my_ec_site.dto.ProductCreateRequestDto;
import com.github.Atgsasakazh5.my_ec_site.dto.SkuCreateRequestDto;
import com.github.Atgsasakazh5.my_ec_site.entity.Category;
import com.github.Atgsasakazh5.my_ec_site.entity.Inventory;
import com.github.Atgsasakazh5.my_ec_site.entity.Product;
import com.github.Atgsasakazh5.my_ec_site.entity.Sku;
import com.github.Atgsasakazh5.my_ec_site.repository.CategoryDao;
import com.github.Atgsasakazh5.my_ec_site.repository.InventoryDao;
import com.github.Atgsasakazh5.my_ec_site.repository.ProductDao;
import com.github.Atgsasakazh5.my_ec_site.repository.SkuDao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductDao productDao;

    @Mock
    private CategoryDao categoryDao;

    @Mock
    private SkuDao skuDao;

    @Mock
    private InventoryDao inventoryDao;

    @Test
    @DisplayName("商品が登録され、dtoで返されること-正常系")
    void createProduct_shouldReturnProductDto_whenProductIsCreated() {
        // 1. Arrange (準備)

        // == 入力データと期待される戻り値の定義 ==
        var categoryId = 1;
        var request = new ProductCreateRequestDto(
                "Tシャツ", categoryId, "説明", "/img.jpg", 1000,
                List.of(new SkuCreateRequestDto("S", "Red", 0, 10))
        );

        // == モックの振る舞いを定義 ==

        // 1a. カテゴリ存在チェック -> 存在する
        var mockCategory = new Category(categoryId, "トップス");
        when(categoryDao.findById(categoryId)).thenReturn(Optional.of(mockCategory));

        // 1b. Productの保存 -> IDが1LのProductを返す
        var savedProduct = new Product(1L, "Tシャツ", 1000, "説明", "/img.jpg", categoryId, LocalDateTime.now(), LocalDateTime.now());
        when(productDao.save(any(Product.class))).thenReturn(savedProduct);

        // 1c. SKUの保存 -> IDが101LのSKUを返す
        var savedSku = new Sku(101L, 1L, "S", "Red", 0, LocalDateTime.now(), LocalDateTime.now());
        when(skuDao.save(any(Sku.class))).thenReturn(savedSku);

        // 1d. 在庫の保存 -> IDが1001LのInventoryを返す
        var savedInventory = new Inventory(1001L, 101L, 10, LocalDateTime.now());
        when(inventoryDao.save(any(Inventory.class))).thenReturn(savedInventory);

        // 1e. 最終的なレスポンスDTOを組み立てるためのDAO呼び出しもモック化
        when(skuDao.findByProductId(savedProduct.getId())).thenReturn(List.of(savedSku));
        when(inventoryDao.findBySkuIdIn(List.of(savedSku.getId()))).thenReturn(List.of(savedInventory));

        // 2. Act (実行)
        var resultDto = productService.createProduct(request);

        // 3. Assert (検証)
        assertThat(resultDto).isNotNull();
        assertThat(resultDto.name()).isEqualTo(request.name());
        assertThat(resultDto.category().name()).isEqualTo("トップス");
        assertThat(resultDto.skus()).hasSize(1);
        assertThat(resultDto.skus().get(0).size()).isEqualTo("S");
        assertThat(resultDto.skus().get(0).inventory().quantity()).isEqualTo(10);
    }

}