package com.github.Atgsasakazh5.my_ec_site.repository;

import com.github.Atgsasakazh5.my_ec_site.entity.Inventory;

import java.util.Optional;

public interface InventoryDao {

    Inventory save(Inventory inventory);
    Inventory update(Inventory inventory);
    Optional<Inventory> findBySkuId(Long skuId);
    Optional<Inventory> findById(Long id);
    void deleteBySkuId(Long skuId);

}
