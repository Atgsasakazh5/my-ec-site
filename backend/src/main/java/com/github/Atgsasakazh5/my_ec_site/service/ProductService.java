package com.github.Atgsasakazh5.my_ec_site.service;

import com.github.Atgsasakazh5.my_ec_site.dto.*;
import com.github.Atgsasakazh5.my_ec_site.entity.Inventory;
import com.github.Atgsasakazh5.my_ec_site.entity.Product;
import com.github.Atgsasakazh5.my_ec_site.entity.Sku;
import com.github.Atgsasakazh5.my_ec_site.exception.ResourceNotFoundException;
import com.github.Atgsasakazh5.my_ec_site.repository.CategoryDao;
import com.github.Atgsasakazh5.my_ec_site.repository.InventoryDao;
import com.github.Atgsasakazh5.my_ec_site.repository.ProductDao;
import com.github.Atgsasakazh5.my_ec_site.repository.SkuDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
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

    private ProductDetailDto buildProductDetailDto(Product product) {
        List<Sku> skus = skuDao.findByProductId(product.getId());

        List<SkuDto> skuDtos = buildSkuDtos(skus);

        CategoryDto categoryDto = categoryDao.findById(product.getCategoryId())
                .map(c -> new CategoryDto(c.getId(), c.getName()))
                .orElse(null);

        return new ProductDetailDto(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDescription(),
                product.getImageUrl(),
                categoryDto,
                skuDtos,
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    private List<SkuDto> buildSkuDtos(List<Sku> skus) {
        if (skus == null || skus.isEmpty()) {
            return List.of(); // 空のリストを返す
        }

        // 1. 全SKUの在庫情報を一度のクエリで取得
        List<Long> skuIds = skus.stream().map(Sku::getId).toList();
        Map<Long, Inventory> inventoryMap = inventoryDao.findBySkuIdIn(skuIds).stream()
                .collect(Collectors.toMap(Inventory::getSkuId, inventory -> inventory));

        // 2. SKUリストをDTOに変換
        return skus.stream()
                .map(sku -> {
                    Inventory inventory = inventoryMap.getOrDefault(sku.getId(), new Inventory(null, sku.getId(), 0, null));
                    InventoryDto inventoryDto = new InventoryDto(inventory.getQuantity());
                    return new SkuDto(sku.getId(), sku.getSize(), sku.getColor(), sku.getExtraPrice(), inventoryDto);
                })
                .toList();
    }

    private List<SkuDto> buildSkuDtos(List<Sku> skus, Map<Long, Inventory> inventoryMap) {
        // 変換元のSKUリストが空の場合は、空のDTOリストを返す
        if (skus == null || skus.isEmpty()) {
            return List.of();
        }

        // SKUエンティティのリストを、SkuDtoのリストに変換する
        return skus.stream()
                .map(sku -> {
                    // 事前に取得したMapから、このSKUに対応する在庫情報を取得
                    // もしMapに存在しない場合は、数量0のデフォルト在庫を生成
                    Inventory inventory = inventoryMap.getOrDefault(
                            sku.getId(),
                            new Inventory(null, sku.getId(), 0, null)
                    );

                    // DTOを組み立てる
                    InventoryDto inventoryDto = new InventoryDto(inventory.getQuantity());
                    return new SkuDto(
                            sku.getId(),
                            sku.getSize(),
                            sku.getColor(),
                            sku.getExtraPrice(),
                            inventoryDto
                    );
                })
                .toList();
    }

    @Transactional
    public ProductDetailDto createProduct(ProductCreateRequestDto requestDto) {
        if (categoryDao.findById(requestDto.categoryId()).isEmpty()) {
            throw new ResourceNotFoundException("指定されたカテゴリが存在しません: " + requestDto.categoryId());
        }

        long uniqueSkuCount = requestDto.skus().stream()
                .map(sku -> sku.size() + "::" + sku.color()) // sizeとcolorを組み合わせたキーを作成
                .distinct()
                .count();

        if (uniqueSkuCount != requestDto.skus().size()) {
            throw new IllegalStateException("リクエスト内に重複したSKU（サイズと色の組み合わせ）が含まれています。");
        }

        Product product = new Product();
        product.setName(requestDto.name());
        product.setPrice(requestDto.price());
        product.setDescription(requestDto.description());
        product.setImageUrl(requestDto.imageUrl());
        product.setCategoryId(requestDto.categoryId());
        Product savedProduct = productDao.save(product);

        requestDto.skus().forEach(skuRequest -> {
            Sku sku = new Sku();
            sku.setProductId(savedProduct.getId());
            sku.setSize(skuRequest.size());
            sku.setColor(skuRequest.color());
            sku.setExtraPrice(skuRequest.extraPrice());
            Sku savedSku = skuDao.save(sku);

            Inventory inventory = new Inventory();
            inventory.setSkuId(savedSku.getId());
            inventory.setQuantity(skuRequest.quantity());
            inventoryDao.save(inventory);
        });

        return buildProductDetailDto(savedProduct);
    }

    @Transactional
    public ProductDetailDto updateProduct(Long productId, ProductUpdateRequestDto requestDto) {
        // 1. 商品を更新
        Product product = productDao.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("商品が存在しません: " + productId));

        if (categoryDao.findById(requestDto.categoryId()).isEmpty()) {
            throw new ResourceNotFoundException("指定されたカテゴリが存在しません: " + requestDto.categoryId());
        }

        product.setName(requestDto.name());
        product.setPrice(requestDto.price());
        product.setDescription(requestDto.description());
        product.setImageUrl(requestDto.imageUrl());
        product.setCategoryId(requestDto.categoryId());
        Product updatedProduct = productDao.update(product);

        return buildProductDetailDto(updatedProduct);
    }

    @Transactional(readOnly = true)
    public ProductDetailDto getProductDetail(Long productId) {
        // 1. 商品を取得
        Product product = productDao.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("商品が存在しません: " + productId));

        return buildProductDetailDto(product);
    }

    @Transactional(readOnly = true)
    public List<ProductDetailDto> getAllProducts() {
        // 1. 全商品リストを取得
        List<Product> products = productDao.findAll();
        if (products.isEmpty()) {
            return List.of();
        }

        // 2. 関連するデータを一度のクエリでまとめて取得
        List<Long> productIds = products.stream().map(Product::getId).toList();
        List<Integer> categoryIds = products.stream().map(Product::getCategoryId).distinct().toList();

        Map<Long, List<Sku>> skusByProductId = skuDao.findAllByProductIds(productIds).stream()
                .collect(Collectors.groupingBy(Sku::getProductId));

        List<Long> allSkuIds = skusByProductId.values().stream().flatMap(List::stream).map(Sku::getId).toList();
        Map<Long, Inventory> inventoryMap = inventoryDao.findBySkuIdIn(allSkuIds).stream()
                .collect(Collectors.toMap(Inventory::getSkuId, i -> i));

        Map<Integer, CategoryDto> categoryMap = categoryDao.findByIds(categoryIds).stream()
                .map(c -> new CategoryDto(c.getId(), c.getName()))
                .collect(Collectors.toMap(CategoryDto::id, c -> c));

        // 3. メモリ上でデータを結合してDTOを組み立てる
        return products.stream()
                .map(product -> {
                    List<Sku> skus = skusByProductId.getOrDefault(product.getId(), List.of());
                    List<SkuDto> skuDtos = buildSkuDtos(skus, inventoryMap); // Mapを渡すヘルパーに改良
                    CategoryDto categoryDto = categoryMap.get(product.getCategoryId());

                    return new ProductDetailDto(
                            product.getId(),
                            product.getName(),
                            product.getPrice(),
                            product.getDescription(),
                            product.getImageUrl(),
                            categoryDto,
                            skuDtos,
                            product.getCreatedAt(),
                            product.getUpdatedAt()
                    );
                })
                .toList();
    }


    @Transactional(readOnly = true)
    public PageResponseDto<ProductSummaryDto> findAllPaginated(int page, int size) {
        // 1. 許可するページサイズのリストを定義
        final List<Integer> ALLOWED_PAGE_SIZES = List.of(20, 50, 100);
        int validatedSize = ALLOWED_PAGE_SIZES.contains(size) ? size : 20;

        // 2. ページ番号がマイナスにならないようにする
        int validatedPage = Math.max(page, 0);

        // 3. DAOを呼び出して、商品リストと総商品数を取得
        List<Product> products = productDao.findAll(validatedPage, validatedSize);
        int totalProducts = productDao.countAll();

        // 4a. 取得した商品リストからカテゴリIDのリストを作成
        List<Integer> categoryIds = products.stream()
                .map(Product::getCategoryId)
                .distinct()
                .toList();

        // 4b. カテゴリ情報を1回のクエリで取得し、IDをキーにしたMapに変換
        Map<Integer, CategoryDto> categoryMap = categoryDao.findByIds(categoryIds).stream()
                .map(c -> new CategoryDto(c.getId(), c.getName()))
                .collect(Collectors.toMap(CategoryDto::id, categoryDto -> categoryDto));

        // 4c. DTOに変換
        List<ProductSummaryDto> dtos = products.stream()
                .map(p -> new ProductSummaryDto(
                        p.getId(),
                        p.getName(),
                        p.getPrice(),
                        p.getDescription(),
                        p.getImageUrl(),
                        categoryMap.get(p.getCategoryId()), // Mapからカテゴリ情報を取得
                        p.getCreatedAt(),
                        p.getUpdatedAt()
                ))
                .toList();

        // 5. ページネーション情報を含んだレスポンスを組み立てる
        return new PageResponseDto<>(
                dtos,
                validatedPage,
                validatedSize,
                totalProducts
        );
    }

    @Transactional
    public void deleteProduct(Long productId) {
        // 1. 商品を取得
        Product product = productDao.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("商品が存在しません: " + productId));

        // 2. 商品に紐づくSKUを取得
        List<Sku> skus = skuDao.findByProductId(productId);
        List<Long> skuIds = skus.stream().map(Sku::getId).toList();

        // 3. SKUに紐づく在庫情報を削除
        inventoryDao.deleteBySkuIds(skuIds);

        // 4. SKUを削除
        skuDao.deleteByProductId(productId);

        // 5. 商品を削除
        productDao.delete(productId);
    }

    @Transactional
    public SkuDto createSku(Long productId, SkuCreateRequestDto requestDto) {
        // 商品が存在するか確認
        Product product = productDao.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("商品が存在しません: " + productId));

        // SKUの重複チェック
        skuDao.findByProductIdAndSizeAndColor(productId, requestDto.size(), requestDto.color())
                .ifPresent(existingSku -> {
                    throw new IllegalStateException("この商品には既に同じサイズと色のSKUが存在します: "
                            + productId + ", サイズ: " + requestDto.size() + ", 色: " + requestDto.color());
                });

        // SKUを作成
        Sku sku = new Sku();
        sku.setProductId(productId);
        sku.setSize(requestDto.size());
        sku.setColor(requestDto.color());
        sku.setExtraPrice(requestDto.extraPrice());
        Sku savedSku = skuDao.save(sku);

        // 在庫を作成
        Inventory inventory = new Inventory();
        inventory.setSkuId(savedSku.getId());
        inventory.setQuantity(requestDto.quantity());
        Inventory savedInventory = inventoryDao.save(inventory);

        // レスポンス用のSkuDtoを組み立て
        InventoryDto inventoryDto = new InventoryDto(savedInventory.getQuantity());
        return new SkuDto(savedSku.getId(), savedSku.getSize(), savedSku.getColor(), savedSku.getExtraPrice(), inventoryDto);
    }

    @Transactional
    public SkuDto updateSku(Long skuId, SkuUpdateRequestDto requestDto) {
        // SKUを取得
        Sku sku = skuDao.findById(skuId)
                .orElseThrow(() -> new ResourceNotFoundException("SKUが存在しません: " + skuId));

        // SKUの重複チェック
        skuDao.findByProductIdAndSizeAndColor(sku.getProductId(), requestDto.size(), requestDto.color())
                .ifPresent(existingSku -> {
                    if (!existingSku.getId().equals(skuId)) {
                        throw new IllegalStateException("この商品には既に同じサイズと色のSKUが存在します: "
                                + sku.getProductId() + ", サイズ: " + requestDto.size() + ", 色: " + requestDto.color());
                    }
                });

        // SKUを更新
        sku.setId(skuId);
        sku.setSize(requestDto.size());
        sku.setColor(requestDto.color());
        sku.setExtraPrice(requestDto.extraPrice());
        Sku updatedSku = skuDao.update(sku);

        // 在庫情報を更新
        Inventory inventory = inventoryDao.findBySkuId(skuId)
                .orElseThrow(() -> new ResourceNotFoundException("在庫情報が存在しません: " + skuId));
        inventory.setQuantity(requestDto.quantity());
        Inventory updatedInventory = inventoryDao.update(inventory);

        // レスポンス用のSkuDtoを組み立て
        InventoryDto inventoryDto = new InventoryDto(updatedInventory.getQuantity());

        return new SkuDto(updatedSku.getId(), updatedSku.getSize(), updatedSku.getColor(), updatedSku.getExtraPrice(), inventoryDto);
    }

    @Transactional(readOnly = true)
    public SkuDto getSkuDetail(Long skuId) {
        // 1. SKUを取得
        Sku sku = skuDao.findById(skuId)
                .orElseThrow(() -> new ResourceNotFoundException("SKUが存在しません: " + skuId));

        // 2. 在庫情報を取得
        Inventory inventory = inventoryDao.findBySkuId(skuId)
                .orElse(new Inventory(null, sku.getId(), 0, null)); // 在庫がない場合は数量0とする

        // 3. レスポンス用のSkuDtoを組み立て
        InventoryDto inventoryDto = new InventoryDto(inventory.getQuantity());
        return new SkuDto(sku.getId(), sku.getSize(), sku.getColor(), sku.getExtraPrice(), inventoryDto);
    }

    @Transactional(readOnly = true)
    public List<SkuDto> getSkusByProductId(Long productId) {
        // 1. 商品が存在するか確認
        Product product = productDao.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("商品が存在しません: " + productId));

        // 2. 商品に紐づくSKUを取得
        List<Sku> skus = skuDao.findByProductId(productId);

        return buildSkuDtos(skus);
    }

    @Transactional
    public void deleteSku(Long skuId) {
        // 1. SKUを取得
        Sku sku = skuDao.findById(skuId)
                .orElseThrow(() -> new ResourceNotFoundException("SKUが存在しません: " + skuId));

        // 2. SKUに紐づく在庫情報を削除
        inventoryDao.deleteBySkuId(skuId);

        // 3. SKUを削除
        skuDao.delete(skuId);
    }

    @Transactional(readOnly = true)
    public PageResponseDto<ProductSummaryDto> searchProductsByCategory(
            int categoryId, int page, int size) {

        // 許可するページサイズのリストを定義
        final List<Integer> ALLOWED_PAGE_SIZES = List.of(20, 50, 100);
        int validatedSize = ALLOWED_PAGE_SIZES.contains(size) ? size : 20;

        // ページ番号がマイナスにならないようにする
        int validatedPage = Math.max(page, 0);

        // カテゴリIDが存在するか確認
        var category = categoryDao.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("指定されたカテゴリが存在しません: " + categoryId));

        // DAOを呼び出して、商品リストと総商品数を取得
        List<Product> products = productDao.findByCategoryId(categoryId, validatedPage, validatedSize);
        int totalProducts = productDao.countByCategoryId(categoryId);

        // summaryDtoのリストを作成
        List<ProductSummaryDto> dtos = products.stream()
                .map(p -> new ProductSummaryDto(
                        p.getId(),
                        p.getName(),
                        p.getPrice(),
                        p.getDescription(),
                        p.getImageUrl(),
                        new CategoryDto(category.getId(), category.getName()),
                        p.getCreatedAt(),
                        p.getUpdatedAt()
                ))
                .toList();

        //ページネーション情報を含んだレスポンスを組み立てる
        return new PageResponseDto<>(
                dtos,
                validatedPage,
                validatedSize,
                totalProducts
        );
    }

}
