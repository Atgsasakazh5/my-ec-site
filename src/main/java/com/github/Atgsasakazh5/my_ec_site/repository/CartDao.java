package com.github.Atgsasakazh5.my_ec_site.repository;

import com.github.Atgsasakazh5.my_ec_site.entity.Cart;

import java.util.Optional;

public interface CartDao {

    Cart saveCart(Long userId);

    Optional<Cart> findCartByUserId(Long userId);

    Optional<Cart> findCartByEmail(String email);
}
