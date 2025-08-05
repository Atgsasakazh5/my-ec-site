package com.github.Atgsasakazh5.my_ec_site.service;

import com.github.Atgsasakazh5.my_ec_site.dto.*;
import com.github.Atgsasakazh5.my_ec_site.entity.Category;
import com.github.Atgsasakazh5.my_ec_site.entity.Inventory;
import com.github.Atgsasakazh5.my_ec_site.entity.Product;
import com.github.Atgsasakazh5.my_ec_site.entity.Sku;
import com.github.Atgsasakazh5.my_ec_site.exception.ResourceNotFoundException;
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
import static org.mockito.Mockito.*;

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
        // 1. Arrange

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

        // 2. Act
        var resultDto = productService.createProduct(request);

        // 3. Assert
        assertThat(resultDto).isNotNull();
        assertThat(resultDto.name()).isEqualTo(request.name());
        assertThat(resultDto.category().name()).isEqualTo("トップス");
        assertThat(resultDto.skus()).hasSize(1);
        assertThat(resultDto.skus().get(0).size()).isEqualTo("S");
        assertThat(resultDto.skus().get(0).inventory().quantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("存在しないカテゴリIDで商品を登録しようとするとResourceNotFoundExceptionが発生すること-異常系")
    void createProduct_shouldThrowResourceNotFoundException_whenCategoryDoesNotExist() {
        // 1. Arrange
        var request = new ProductCreateRequestDto(
                "Tシャツ", 999, "説明", "/img.jpg", 1000,
                List.of(new SkuCreateRequestDto("S", "Red", 0, 10))
        );

        when(categoryDao.findById(999)).thenReturn(Optional.empty());

        // 2. Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.createProduct(request));
    }

    @Test
    @DisplayName("SKUに重複があった場合にIllegalStateExceptionが発生すること-異常系")
    void createProduct_shouldThrowIllegalStateException_whenDuplicateSkuExists() {
        // 1. Arrange
        var categoryId = 1;
        var request = new ProductCreateRequestDto(
                "Tシャツ", categoryId, "説明", "/img.jpg", 1000,
                List.of(
                        new SkuCreateRequestDto("S", "Red", 0, 10),
                        new SkuCreateRequestDto("S", "Red", 0, 10) // 重複SKU
                )
        );

        var mockCategory = new Category(categoryId, "トップス");
        when(categoryDao.findById(categoryId)).thenReturn(Optional.of(mockCategory));

        // 2. Act & Assert
        assertThrows(IllegalStateException.class, () -> productService.createProduct(request));
    }

    @Test
    @DisplayName("商品情報の更新が成功すること-正常系")
    void updateProduct_shouldReturnUpdatedProductDto_whenProductExists() {
        // 1. Arrange
        var productId = 1L;
        var request = new ProductUpdateRequestDto(
                "更新されたTシャツ", 1500, "更新された説明", "/updated_img.jpg", 1);

        var existingProduct = new Product(productId, "Tシャツ", 1000, "説明", "/img.jpg", 1, LocalDateTime.now(), LocalDateTime.now());
        var updatedProduct = new Product(productId, request.name(), request.price(), request.description(), request.imageUrl(), request.categoryId(), LocalDateTime.now(), LocalDateTime.now());
        var mockCategory = new Category(request.categoryId(), "トップス");

        // 1. updateProductメソッド内のモック
        when(productDao.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productDao.update(any(Product.class))).thenReturn(updatedProduct);

        // 2. buildProductDetailDtoヘルパーメソッド内で呼ばれるDAOのモック
        when(categoryDao.findById(request.categoryId())).thenReturn(Optional.of(mockCategory));
        when(skuDao.findByProductId(productId)).thenReturn(List.of());

        // 2. Act
        var resultDto = productService.updateProduct(productId, request);

        // 3. Assert
        assertNotNull(resultDto);
        assertEquals(request.name(), resultDto.name());
        assertEquals(request.price(), resultDto.price());
        assertEquals(request.description(), resultDto.description());
        assertEquals(request.imageUrl(), resultDto.imageUrl());
        assertEquals(request.categoryId(), resultDto.category().id());
    }

    @Test
    @DisplayName("存在しない商品IDで更新しようとするとResourceNotFoundExceptionが発生すること-異常系")
    void updateProduct_shouldThrowResourceNotFoundException_whenProductDoesNotExist() {
        // 1. Arrange
        var productId = 999L;
        var request = new ProductUpdateRequestDto(
                "更新されたTシャツ", 1500, "更新された説明", "/updated_img.jpg", 1);
        when(productDao.findById(productId)).thenReturn(Optional.empty());

        // 2. Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.updateProduct(productId, request));
    }

    @Test
    @DisplayName("存在しないカテゴリIDで商品を更新しようとするとResourceNotFoundExceptionが発生すること-異常系")
    void updateProduct_shouldThrowResourceNotFoundException_whenCategoryDoesNotExist() {
        // 1. Arrange
        var productId = 1L;
        var request = new ProductUpdateRequestDto(
                "更新されたTシャツ", 1500, "更新された説明", "/updated_img.jpg", 999);
        var existingProduct = new Product(productId, "Tシャツ", 1000, "説明", "/img.jpg", 1, LocalDateTime.now(), LocalDateTime.now());

        when(productDao.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(categoryDao.findById(999)).thenReturn(Optional.empty());

        // 2. Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.updateProduct(productId, request));
    }

    @Test
    @DisplayName("商品を検索できること-正常系")
    void getProductDetail_shouldReturnProductDto_whenProductExists() {
        // 1. Arrange
        var productId = 1L;
        var mockCategory = new Category(1, "トップス");
        var existingProduct = new Product(productId, "Tシャツ", 1000, "説明", "/img.jpg", 1, LocalDateTime.now(), LocalDateTime.now());
        var mockSku = new Sku(101L, productId, "S", "Red", 0, LocalDateTime.now(), LocalDateTime.now());
        var mockInventory = new Inventory(1001L, mockSku.getId(), 10, LocalDateTime.now());

        when(productDao.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(categoryDao.findById(existingProduct.getCategoryId())).thenReturn(Optional.of(mockCategory));
        when(skuDao.findByProductId(productId)).thenReturn(List.of(mockSku));
        when(inventoryDao.findBySkuIdIn(List.of(mockSku.getId()))).thenReturn(List.of(mockInventory));

        // 2. Act
        var resultDto = productService.getProductDetail(productId);

        // 3. Assert
        assertNotNull(resultDto);
        assertEquals(existingProduct.getName(), resultDto.name());
        assertEquals(mockCategory.getName(), resultDto.category().name());
        assertEquals(1, resultDto.skus().size());
        assertEquals(mockSku.getSize(), resultDto.skus().get(0).size());
        assertEquals(mockInventory.getQuantity(), resultDto.skus().get(0).inventory().quantity());
    }

    @Test
    @DisplayName("存在しない商品IDで検索しようとするとResourceNotFoundExceptionが発生すること-異常系")
    void getProductDetail_shouldThrowResourceNotFoundException_whenProductDoesNotExist() {
        // 1. Arrange
        var productId = 999L;
        when(productDao.findById(productId)).thenReturn(Optional.empty());

        // 2. Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.getProductDetail(productId));
    }

    @Test
    @DisplayName("ページ数とページサイズを指定して商品一覧を取得できること-正常系")
    void findAllPaginated_shouldReturnPaginatedProductList_whenPageAndSizeAreValid() {
        // 1. Arrange
        int page = 0;
        int size = 20;
        var mockCategory = new Category(1, "トップス");
        var mockProduct1 = new Product(1L, "Tシャツ", 1000, "説明", "/img.jpg", 1, LocalDateTime.now(), LocalDateTime.now());
        var mockProduct2 = new Product(2L, "ジーンズ", 2000, "説明", "/img.jpg", 2, LocalDateTime.now(), LocalDateTime.now());
        List<Product> productList = List.of(mockProduct1, mockProduct2);

        when(productDao.findAll(page, size)).thenReturn(productList);
        when(productDao.countAll()).thenReturn(2);
        when(categoryDao.findByIds(List.of(1, 2))).thenReturn(List.of(mockCategory));

        // 2. Act
        var resultPage = productService.findAllPaginated(page, size);

        // 3. Assert
        assertThat(resultPage).isNotNull();
        assertThat(resultPage.content()).hasSize(2);
        assertThat(resultPage.totalPages()).isEqualTo(1);
        assertThat(resultPage.totalElements()).isEqualTo(2);
        assertThat(resultPage.content().get(0).name()).isEqualTo("Tシャツ");
        assertThat(resultPage.content().get(0).category().name()).isEqualTo("トップス");
    }

    @Test
    @DisplayName("商品数が0の場合、空のページを返すこと-正常系")
    void findAllPaginated_shouldReturnEmptyPage_whenNoProductsExist() {
        // 1. Arrange
        int page = 0;
        int size = 20;
        when(productDao.findAll(page, size)).thenReturn(List.of());
        when(productDao.countAll()).thenReturn(0);
        when(categoryDao.findByIds(List.of())).thenReturn(List.of());

        // 2. Act
        var resultPage = productService.findAllPaginated(page, size);

        // 3. Assert
        assertThat(resultPage).isNotNull();
        assertThat(resultPage.content()).isEmpty();
        assertThat(resultPage.totalPages()).isEqualTo(0);
        assertThat(resultPage.totalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("商品数が21の場合、2ページ目の商品数が1であること-正常系")
    void findAllPaginated_shouldReturnSecondPageWithOneProduct_whenTotalProductsIs21() {
        // 1. Arrange
        int page = 1; // 2ページ目
        int size = 20;
        var mockCategory = new Category(3, "トップス");
        var mockProduct3 = new Product(3L, "ジャケット", 3000, "説明", "/img.jpg", 3, LocalDateTime.now(), LocalDateTime.now());

        when(productDao.findAll(page, size)).thenReturn(List.of(mockProduct3)); // 2ページ目は1商品
        when(productDao.countAll()).thenReturn(21);
        when(categoryDao.findByIds(List.of(3))).thenReturn(List.of(mockCategory));

        // 2. Act
        var resultPage = productService.findAllPaginated(page, size);

        // 3. Assert
        assertThat(resultPage).isNotNull();
        assertThat(resultPage.content()).hasSize(1); // 2ページ目は1商品
        assertThat(resultPage.totalPages()).isEqualTo(2); // 総ページ数は2
        assertThat(resultPage.totalElements()).isEqualTo(21); // 総商品数は21
        assertThat(resultPage.content().get(0).name()). isEqualTo("ジャケット");
        assertThat(resultPage.content().get(0).category().name()).isEqualTo("トップス");
    }

    @Test
    @DisplayName("商品数が20のとき、2ページ目は空であること-正常系")
    void findAllPaginated_shouldReturnEmptySecondPage_whenTotalProductsIs20() {
        // 1. Arrange
        int page = 1; // 2ページ目
        int size = 20;
        when(productDao.findAll(page, size)).thenReturn(List.of()); // 2ページ目は空
        when(productDao.countAll()).thenReturn(20); // 総商品数は20
        when(categoryDao.findByIds(List.of())).thenReturn(List.of());

        // 2. Act
        var resultPage = productService.findAllPaginated(page, size);

        // 3. Assert
        assertThat(resultPage).isNotNull();
        assertThat(resultPage.content()).isEmpty(); // 2ページ目は空
        assertThat(resultPage.totalPages()).isEqualTo(1); // 総ページ数
        assertThat(resultPage.totalElements()).isEqualTo(20); // 総商品数
    }

    @Test
    @DisplayName("負のページ数を指定した場合にページ数が0になること-異常系")
    void findAllPaginated_shouldReturnFirstPage_whenNegativePageNumberIsGiven() {
        // 1. Arrange
        int page = -1; // 負のページ数
        int size = 20;
        when(productDao.findAll(0, size)).thenReturn(List.of()); // page数は0に変換される
        when(productDao.countAll()).thenReturn(0); // 総商品数は0
        when(categoryDao.findByIds(List.of())).thenReturn(List.of());

        // 2. Act
        var resultPage = productService.findAllPaginated(page, size);

        // 3. Assert
        assertThat(resultPage).isNotNull();
        assertThat(resultPage.content()).isEmpty(); // 0ページ目は空
        assertThat(resultPage.totalPages()).isEqualTo(0); // 総ページ数
        assertThat(resultPage.totalElements()).isEqualTo(0); // 総商品数

    }

    @Test
    @DisplayName("許可されていないページサイズを指定された場合にデフォルトのページサイズが使用されること-異常系")
    void findAllPaginated_shouldUseDefaultSize_whenInvalidSizeIsGiven() {
        // 1. Arrange
        int page = 0;
        int size = -10; // 無効なページサイズ
        int defaultSize = 20; // デフォルトのページサイズ
        var mockCategory = new Category(1, "トップス");
        var mockProduct1 = new Product(1L, "Tシャツ", 1000, "説明", "/img.jpg", 1, LocalDateTime.now(), LocalDateTime.now());
        var mockProduct2 = new Product(2L, "ジーンズ", 2000, "説明", "/img.jpg", 2, LocalDateTime.now(), LocalDateTime.now());

        List<Product> productList = List.of(mockProduct1, mockProduct2);

        when(productDao.findAll(page, defaultSize)).thenReturn(productList);
        when(productDao.countAll()).thenReturn(2);
        when(categoryDao.findByIds(List.of(1, 2))).thenReturn(List.of(mockCategory));

        // 2. Act
        var resultPage = productService.findAllPaginated(page, size);

        // 3. Assert
        assertThat(resultPage).isNotNull();
        assertThat(resultPage.content()).hasSize(2);
        assertThat(resultPage.totalPages()).isEqualTo(1);
        assertThat(resultPage.totalElements()).isEqualTo(2);
        assertThat(resultPage.content().get(0).name()).isEqualTo("Tシャツ");
        assertThat(resultPage.content().get(0).category().name()).isEqualTo("トップス");
    }

    @Test
    @DisplayName("商品削除が成功すること-正常系")
    void deleteProduct_shouldCallDaoDelete_whenProductExists() {
        // 1. Arrange
        long productId = 1L;
        var mockProduct = new Product(productId, "Tシャツ", 1000, "説明", "/img.jpg", 1, LocalDateTime.now(), LocalDateTime.now());
        var mockSku = new Sku(101L, productId, "S", "Red", 0, LocalDateTime.now(), LocalDateTime.now());
        var skuList = List.of(mockSku);
        var skuIds = List.of(mockSku.getId());

        when(productDao.findById(productId)).thenReturn(Optional.of(mockProduct));
        when(skuDao.findByProductId(productId)).thenReturn(skuList);

        // 2. Act
        productService.deleteProduct(productId);

        // 3. Assert
        verify(inventoryDao, times(1)).deleteBySkuIds(skuIds);
        verify(skuDao, times(1)).deleteByProductId(productId);
        verify(productDao, times(1)).delete(productId);
    }

    @Test
    @DisplayName("存在しない商品IDで削除しようとするとResourceNotFoundExceptionが発生すること-異常系")
    void deleteProduct_throwsResourceNotFoundException_whenProductDoesNotExist() {
        // 1. Arrange
        long productId = 999L;
        when(productDao.findById(productId)).thenReturn(Optional.empty());

        // 2. Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.deleteProduct(productId));
    }

    @Test
    @DisplayName("SKUを新たに追加できること-正常系")
    void createSku_shouldReturnSkuDto_whenSkuIsCreated() {
        // 1. Arrange
        long productId = 1L;
        var request = new SkuCreateRequestDto("M", "Blue", 0, 20);
        var mockProduct = new Product(productId, "Tシャツ", 1000, "説明", "/img.jpg", 1, LocalDateTime.now(), LocalDateTime.now());
        var mockSku = new Sku(102L, productId, request.size(), request.color(), request.extraPrice(), LocalDateTime.now(), LocalDateTime.now());
        var mockInventory = new Inventory(1002L, mockSku.getId(), request.quantity(), LocalDateTime.now());

        when(productDao.findById(productId)).thenReturn(Optional.of(mockProduct));
        when(skuDao.findByProductIdAndSizeAndColor(productId, request.size(), request.color()))
                .thenReturn(Optional.empty());

        when(skuDao.save(any(Sku.class))).thenReturn(mockSku);
        when(inventoryDao.save(any(Inventory.class))).thenReturn(mockInventory);

        // 2. Act
        var resultDto = productService.createSku(productId, request);

        // 3. Assert
        assertNotNull(resultDto);
        assertEquals(mockSku.getSize(), resultDto.size());
        assertEquals(mockSku.getColor(), resultDto.color());
        assertEquals(mockInventory.getQuantity(), resultDto.inventory().quantity());
    }

    @Test
    @DisplayName("存在しない商品IDでSKUを追加しようとするとResourceNotFoundExceptionが発生すること-異常系")
    void createSku_shouldThrowResourceNotFoundException_whenProductDoesNotExist() {
        // 1. Arrange
        long productId = 999L;
        var request = new SkuCreateRequestDto("M", "Blue", 0, 20);
        when(productDao.findById(productId)).thenReturn(Optional.empty());

        // 2. Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.createSku(productId, request));
    }

    @Test
    @DisplayName("SKUの重複がある場合にIllegalStateExceptionが発生すること-異常系")
    void createSku_shouldThrowIllegalStateException_whenDuplicateSkuExists() {
        // 1. Arrange
        long productId = 1L;
        var request = new SkuCreateRequestDto("M", "Blue", 0, 20);
        var mockProduct = new Product(productId, "Tシャツ", 1000, "説明", "/img.jpg", 1, LocalDateTime.now(), LocalDateTime.now());

        when(productDao.findById(productId)).thenReturn(Optional.of(mockProduct));
        when(skuDao.findByProductIdAndSizeAndColor(productId, request.size(), request.color()))
                .thenReturn(Optional.of(new Sku(103L, productId, request.size(), request.color(), request.extraPrice(), LocalDateTime.now(), LocalDateTime.now())));

        // 2. Act & Assert
        assertThrows(IllegalStateException.class, () -> productService.createSku(productId, request));
    }

    @Test
    @DisplayName("SKUの更新が成功すること-正常系")
    void updateSku_shouldReturnUpdatedSkuDto_whenSkuExists() {
        // 1. Arrange
        long skuId = 101L;
        var request = new SkuUpdateRequestDto(skuId, "M", "Red", 0, 30);
        var mockSku = new Sku(skuId, 1L, "M", "Red", 0, LocalDateTime.now(), LocalDateTime.now());
        var mockInventory = new Inventory(1001L, skuId, 30, LocalDateTime.now());

        when(skuDao.findById(skuId)).thenReturn(Optional.of(mockSku));
        when(skuDao.findByProductIdAndSizeAndColor(mockSku.getProductId(), request.size(), request.color()))
                .thenReturn(Optional.empty());

        when(skuDao.update(any(Sku.class))).thenReturn(new Sku(skuId, mockSku.getProductId(), request.size(), request.color(), request.extraPrice(), LocalDateTime.now(), LocalDateTime.now()));
        when(inventoryDao.findBySkuId(skuId)).thenReturn(Optional.of(mockInventory));
        when(inventoryDao.update(any(Inventory.class))).thenReturn(mockInventory);

        // 2. Act
        var resultDto = productService.updateSku(skuId, request);

        // 3. Assert
        assertNotNull(resultDto);
        assertEquals(request.size(), resultDto.size());
        assertEquals(request.color(), resultDto.color());
        assertEquals(request.extraPrice(), resultDto.extraPrice());
        assertEquals(mockInventory.getQuantity(), resultDto.inventory().quantity());
    }

    @Test
    @DisplayName("存在しないSKU IDで更新しようとするとResourceNotFoundExceptionが発生すること-異常系")
    void updateSku_shouldThrowResourceNotFoundException_whenSkuDoesNotExist() {
        // 1. Arrange
        long skuId = 999L;
        var request = new SkuUpdateRequestDto(skuId, "M", "Red", 0, 30);
        when(skuDao.findById(skuId)).thenReturn(Optional.empty());

        // 2. Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.updateSku(skuId, request));
    }

    @Test
    @DisplayName("SKUを更新すると重複が発生する場合にIllegalStateExceptionが発生すること-異常系")
    void updateSku_shouldThrowIllegalStateException_whenDuplicateSkuExists() {
        // 1. Arrange
        long skuId = 101L;
        var request = new SkuUpdateRequestDto(skuId, "M", "Red", 0, 30);
        var mockSku = new Sku(skuId, 1L, "S", "Blue", 0, LocalDateTime.now(), LocalDateTime.now());

        when(skuDao.findById(skuId)).thenReturn(Optional.of(mockSku));
        when(skuDao.findByProductIdAndSizeAndColor(mockSku.getProductId(), request.size(), request.color()))
                .thenReturn(Optional.of(new Sku(102L, mockSku.getProductId(), request.size(), request.color(), request.extraPrice(), LocalDateTime.now(), LocalDateTime.now())));

        // 2. Act & Assert
        assertThrows(IllegalStateException.class, () -> productService.updateSku(skuId, request));
    }

    @Test
    @DisplayName("SKU情報を取得できること-正常系")
    void getSkuDetail_shouldReturnSkuDto_whenSkuExists() {
        // 1. Arrange
        long skuId = 101L;
        var mockSku = new Sku(skuId, 1L, "M", "Red", 0, LocalDateTime.now(), LocalDateTime.now());
        var mockInventory = new Inventory(1001L, skuId, 20, LocalDateTime.now());
        var mockProduct = new Product(1L, "Tシャツ", 1000, "説明", "/img.jpg", 1, LocalDateTime.now(), LocalDateTime.now());

        when(skuDao.findById(skuId)).thenReturn(Optional.of(mockSku));
        when(inventoryDao.findBySkuId(skuId)).thenReturn(Optional.of(mockInventory));

        // 2. Act
        var resultDto = productService.getSkuDetail(skuId);

        // 3. Assert
        assertNotNull(resultDto);
        assertEquals(mockSku.getSize(), resultDto.size());
        assertEquals(mockSku.getColor(), resultDto.color());
        assertEquals(mockInventory.getQuantity(), resultDto.inventory().quantity());
    }

    @Test
    @DisplayName("存在しないSKU IDで取得しようとするとResourceNotFoundExceptionが発生すること-異常系")
    void getSkuDetail_shouldThrowResourceNotFoundException_whenSkuDoesNotExist() {
        // 1. Arrange
        long skuId = 999L;
        when(skuDao.findById(skuId)).thenReturn(Optional.empty());

        // 2. Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.getSkuDetail(skuId));
    }

    @Test
    @DisplayName("商品IDからSKUのリストを取得できること-正常系")
    void getSkusByProductId_shouldReturnSkuList_whenProductExists() {
        // 1. Arrange
        long productId = 1L;
        var mockProduct = new Product(productId, "Tシャツ", 1000, "説明", "/img.jpg", 1, LocalDateTime.now(), LocalDateTime.now());
        var mockSku1 = new Sku(101L, productId, "S", "Red", 0, LocalDateTime.now(), LocalDateTime.now());
        var mockSku2 = new Sku(102L, productId, "M", "Blue", 0, LocalDateTime.now(), LocalDateTime.now());
        var mockInventory1 = new Inventory(1001L, mockSku1.getId(), 10, LocalDateTime.now());
        var mockInventory2 = new Inventory(1002L, mockSku2.getId(), 20, LocalDateTime.now());

        when(productDao.findById(productId)).thenReturn(Optional.of(mockProduct));
        when(skuDao.findByProductId(productId)).thenReturn(List.of(mockSku1, mockSku2));
        when(inventoryDao.findBySkuIdIn(List.of(mockSku1.getId(), mockSku2.getId())))
                .thenReturn(List.of(mockInventory1, mockInventory2));

        // 2. Act
        var resultList = productService.getSkusByProductId(productId);

        // 3. Assert
        assertNotNull(resultList);
        assertEquals(2, resultList.size());
        assertEquals(mockSku1.getSize(), resultList.get(0).size());
        assertEquals(mockInventory1.getQuantity(), resultList.get(0).inventory().quantity());
        assertEquals(mockSku2.getSize(), resultList.get(1).size());
        assertEquals(mockInventory2.getQuantity(), resultList.get(1).inventory().quantity());

    }

    @Test
    @DisplayName("存在しない商品IDでSKUリストを取得しようとするとResourceNotFoundExceptionが発生すること-異常系")
    void getSkusByProductId_shouldThrowResourceNotFoundException_whenProductDoesNotExist() {
        // 1. Arrange
        long productId = 999L;
        when(productDao.findById(productId)).thenReturn(Optional.empty());

        // 2. Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.getSkusByProductId(productId));
    }

    @Test
    @DisplayName("SKUが存在しない場合、空のリストを返すこと-正常系")
    void getSkusByProductId_shouldThrowResourceNotFoundException_whenNoSkusExist() {
        // 1.  Arrange
        long productId = 1L;
        var mockProduct = new Product(productId, "Tシャツ", 1000, "説明", "/img.jpg", 1, LocalDateTime.now(), LocalDateTime.now());

        when(productDao.findById(productId)).thenReturn(Optional.of(mockProduct));
        when(skuDao.findByProductId(productId)).thenReturn(List.of()); // SKUが存在しない

        // 2. Act
        var resultList = productService.getSkusByProductId(productId);

        // 3. Assert
        assertNotNull(resultList);
        assertTrue(resultList.isEmpty());
        verify(inventoryDao, never()).findBySkuIdIn(anyList());

    }

    @Test
    @DisplayName("SKUを削除できること-正常系")
    void deleteSku_shouldCallDaoDelete_whenSkuExists() {
        // 1. Arrange
        long skuId = 101L;
        var mockSku = new Sku(skuId, 1L, "S", "Red", 0, LocalDateTime.now(), LocalDateTime.now());
        var mockInventory = new Inventory(1001L, skuId, 10, LocalDateTime.now());

        when(skuDao.findById(skuId)).thenReturn(Optional.of(mockSku));

        // 2. Act
        productService.deleteSku(skuId);

        // 3. Assert
        verify(inventoryDao, times(1)).deleteBySkuId(skuId);
        verify(skuDao, times(1)).delete(skuId);
    }

    @Test
    @DisplayName("存在しないSKU IDで削除しようとするとResourceNotFoundExceptionが発生すること-異常系")
    void deleteSku_shouldThrowResourceNotFoundException_whenSkuDoesNotExist() {
        // 1. Arrange
        long skuId = 999L;
        when(skuDao.findById(skuId)).thenReturn(Optional.empty());

        // 2. Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.deleteSku(skuId));
    }

    @Test
    @DisplayName("特定のカテゴリの商品をページネーション対応して取得できること-正常系")
    void findProductsByCategoryIdPaginated_shouldReturnPaginatedProductList_whenCategoryExists(){
        // 1. Arrange
        int page = 0;
        int size = 20;
        int categoryId = 1;
        var mockCategory = new Category(categoryId, "トップス");
        var mockProduct1 = new Product(1L, "Tシャツ", 1000, "説明", "/img.jpg", categoryId, LocalDateTime.now(), LocalDateTime.now());
        var mockProduct2 = new Product(2L, "ジーンズ", 2000, "説明", "/img.jpg", categoryId, LocalDateTime.now(), LocalDateTime.now());
        List<Product> productList = List.of(mockProduct1, mockProduct2);

        when(categoryDao.findById(categoryId)).thenReturn(Optional.of(mockCategory));
        when(productDao.findByCategoryId(categoryId, page, size)).thenReturn(productList);
        when(productDao.countByCategoryId(categoryId)).thenReturn(2);

        // 2. Act
        var resultPage = productService.searchProductsByCategory(categoryId, page, size);

        // 3. Assert
        assertThat(resultPage).isNotNull();
        assertThat(resultPage.content()).hasSize(2);
        assertThat(resultPage.totalPages()).isEqualTo(1);
        assertThat(resultPage.totalElements()).isEqualTo(2);
        assertThat(resultPage.content().get(0).name()).isEqualTo("Tシャツ");
        assertThat(resultPage.content().get(0).category().name()).isEqualTo("トップス");
        assertThat(resultPage.content().get(1).name()).isEqualTo("ジーンズ");
        assertThat(resultPage.content().get(1).category().name()).isEqualTo("トップス");

    }

    @Test
    @DisplayName("存在しないカテゴリIDで検索すると404を返すこと-正常系")
    void findProductsByCategoryIdPaginated_shouldReturn404_whenCategoryDoesNotExist() {
        // 1. Arrange
        int page = 0;
        int size = 20;
        int categoryId = 999; // 存在しないカテゴリID

        when(categoryDao.findById(categoryId)).thenReturn(Optional.empty());

        // 3. Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            productService.searchProductsByCategory(categoryId, page, size);
        });
    }
}