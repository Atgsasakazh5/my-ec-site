package com.github.Atgsasakazh5.my_ec_site.repository;

import com.github.Atgsasakazh5.my_ec_site.entity.Sku;

import java.util.List;
import java.util.Optional;

public interface SkuDao {

    Sku save(Sku sku);

    Optional<Sku> findById(Long id);

    Optional<List<Sku>> findByProductId(Long productId);

    Sku update(Sku sku);

    void delete(Long id);
}
