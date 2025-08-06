package com.github.Atgsasakazh5.my_ec_site.repository;

import com.github.Atgsasakazh5.my_ec_site.entity.Cart;

public interface CartDao {

    Cart saveCart(Long userId);
}
