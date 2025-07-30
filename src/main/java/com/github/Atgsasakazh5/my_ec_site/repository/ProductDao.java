package com.github.Atgsasakazh5.my_ec_site.repository;

import com.github.Atgsasakazh5.my_ec_site.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductDao {

    Product save(Product product);
    Optional<Product> findById(Long id);
    Product update(Product product);
    void delete(Long id);

}
