package com.github.Atgsasakazh5.my_ec_site.service;

import com.github.Atgsasakazh5.my_ec_site.repository.InventoryDao;
import com.github.Atgsasakazh5.my_ec_site.repository.ProductDao;
import com.github.Atgsasakazh5.my_ec_site.repository.SkuDao;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductDao productDao;
    private final SkuDao skuDao;
    private final InventoryDao inventoryDao;

    public ProductService(ProductDao productDao, SkuDao skuDao, InventoryDao inventoryDao) {
        this.productDao = productDao;
        this.skuDao = skuDao;
        this.inventoryDao = inventoryDao;
    }

    // 商品の作成、商品の更新、skuの更新,商品の削除、skuの削除、商品一覧の取得、商品詳細の取得


}
