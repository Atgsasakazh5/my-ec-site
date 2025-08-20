package com.github.Atgsasakazh5.my_ec_site.repository;

import com.github.Atgsasakazh5.my_ec_site.dto.CartItemDto;
import com.github.Atgsasakazh5.my_ec_site.entity.CartItem;

import java.util.List;
import java.util.Optional;

public interface CartItemDao {

    CartItem save(CartItem cartItem);

    Optional<CartItem> findById(Long id);

    Optional<CartItem> findByCartIdAndSkuId(Long cartId, Long skuId);

    List<CartItem> findByCartId(Long cartId);

    void deleteById(Long id);

    void deleteByCartId(Long cartId);

    CartItem update(CartItem cartItem);

    List<CartItemDto> findDetailedItemsByCartId(Long cartId);
}
