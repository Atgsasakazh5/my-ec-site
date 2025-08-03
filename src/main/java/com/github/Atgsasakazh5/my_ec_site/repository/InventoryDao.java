package com.github.Atgsasakazh5.my_ec_site.repository;

import com.github.Atgsasakazh5.my_ec_site.entity.Inventory;

import java.util.List;
import java.util.Optional;

public interface InventoryDao {

    Inventory save(Inventory inventory);

    Inventory update(Inventory inventory);

    Optional<Inventory> findBySkuId(Long skuId);

    Optional<Inventory> findById(Long id);

    void deleteBySkuId(Long skuId);

    void deleteBySkuIds(List<Long> skuIds);

    // 複数のSKU IDに対応する在庫を一括で取得する
    List<Inventory> findBySkuIdIn(List<Long> skuIds);
}
