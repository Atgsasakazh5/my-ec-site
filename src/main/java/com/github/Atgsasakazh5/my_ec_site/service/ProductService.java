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

import java.util.ArrayList;
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
                savedProduct.getPrice(),
                savedProduct.getDescription(),
                savedProduct.getImageUrl(),
                categoryDto,
                skuDtos,
                savedProduct.getCreatedAt(),
                savedProduct.getUpdatedAt()
        );
    }

    @Transactional
    public ProductDetailDto updateProduct(Long productId, ProductUpdateRequestDto requestDto) {
        // 1. 商品を更新
        Product product = productDao.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("商品が存在しません: " + productId));
        product.setName(requestDto.name());
        product.setPrice(requestDto.price());
        product.setDescription(requestDto.description());
        product.setImageUrl(requestDto.imageUrl());
        product.setCategoryId(requestDto.categoryId());
        Product updatedProduct = productDao.update(product);

        // 2. 更新された商品に紐づくSKUと在庫情報を取得
        List<SkuDto> skuDtos = skuDao.findByProductId(updatedProduct.getId()).stream()
                .map(sku -> {
                    // 各SKUに対応する在庫情報を取得
                    Inventory inventory = inventoryDao.findBySkuId(sku.getId())
                            .orElse(new Inventory(null, sku.getId(), 0, null)); // 在庫がない場合は数量0とする
                    InventoryDto inventoryDto = new InventoryDto(inventory.getQuantity());
                    return new SkuDto(sku.getId(), sku.getSize(), sku.getColor(), sku.getExtraPrice(), inventoryDto);
                })
                .collect(Collectors.toList());

        // 3. カテゴリ情報を取得
        CategoryDto categoryDto = categoryDao.findById(updatedProduct.getCategoryId())
                .map(c -> new CategoryDto(c.getId(), c.getName()))
                .orElse(null); // 存在しないカテゴリIDの場合の考慮

        // 4. すべての情報を結合してProductDetailDtoを返す
        return new ProductDetailDto(
                updatedProduct.getId(),
                updatedProduct.getName(),
                updatedProduct.getPrice(),
                updatedProduct.getDescription(),
                updatedProduct.getImageUrl(),
                categoryDto,
                skuDtos,
                updatedProduct.getCreatedAt(),
                updatedProduct.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public ProductDetailDto getProductDetail(Long productId) {
        // 1. 商品を取得
        Product product = productDao.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("商品が存在しません: " + productId));

        // 2. 関連するSKUを一度だけ取得
        List<Sku> skus = skuDao.findByProductId(productId);

        // 3. 全SKUの在庫情報を一度のクエリで取得
        List<Long> skuIds = skus.stream().map(Sku::getId).toList();
        // 取得した在庫情報を、SKU IDをキーにしたMapに変換して高速にアクセスできるようにする
        Map<Long, Inventory> inventoryMap = inventoryDao.findBySkuIdIn(skuIds).stream()
                .collect(Collectors.toMap(Inventory::getSkuId, inventory -> inventory));

        // 4. SKUリストをDTOに変換
        List<SkuDto> skuDtos = skus.stream()
                .map(sku -> {
                    Inventory inventory = inventoryMap.getOrDefault(sku.getId(), new Inventory(null, sku.getId(), 0, null));
                    InventoryDto inventoryDto = new InventoryDto(inventory.getQuantity());
                    return new SkuDto(sku.getId(), sku.getSize(), sku.getColor(), sku.getExtraPrice(), inventoryDto);
                })
                .toList();

        // 5. カテゴリ情報を取得
        CategoryDto categoryDto = categoryDao.findById(product.getCategoryId())
                .map(c -> new CategoryDto(c.getId(), c.getName()))
                .orElse(null);

        // 6. ProductDetailDtoを組み立てて返す
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

    @Transactional(readOnly = true)
    public List<ProductDetailDto> getAllProducts() {
        List<Product> products = productDao.findAll();
        return products.stream()
                .map(product -> {
                    // 各商品に紐づくSKUを取得
                    List<Sku> skus = skuDao.findByProductId(product.getId());
                    // SKUの在庫情報を一度のクエリで取得
                    List<Long> skuIds = skus.stream().map(Sku::getId).toList();
                    Map<Long, Inventory> inventoryMap = inventoryDao.findBySkuIdIn(skuIds).stream()
                            .collect(Collectors.toMap(Inventory::getSkuId, inventory -> inventory));
                    // SKUをDTOに変換
                    List<SkuDto> skuDtos = skus.stream()
                            .map(sku -> {
                                Inventory inventory = inventoryMap.getOrDefault(sku.getId(), new Inventory(null, sku.getId(), 0, null));
                                InventoryDto inventoryDto = new InventoryDto(inventory.getQuantity());
                                return new SkuDto(sku.getId(), sku.getSize(), sku.getColor(), sku.getExtraPrice(), inventoryDto);
                            })
                            .toList();
                    // カテゴリ情報を取得
                    CategoryDto categoryDto = categoryDao.findById(product.getCategoryId())
                            .map(c -> new CategoryDto(c.getId(), c.getName()))
                            .orElse(null);
                    // ProductDtoを組み立てて返す
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
                .collect(Collectors.toList());
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
}
