package com.github.Atgsasakazh5.my_ec_site.service;

import com.github.Atgsasakazh5.my_ec_site.dto.AddCartItemRequestDto;
import com.github.Atgsasakazh5.my_ec_site.dto.CartDetailDto;
import com.github.Atgsasakazh5.my_ec_site.dto.CartItemDto;
import com.github.Atgsasakazh5.my_ec_site.entity.Cart;
import com.github.Atgsasakazh5.my_ec_site.entity.CartItem;
import com.github.Atgsasakazh5.my_ec_site.entity.Inventory;
import com.github.Atgsasakazh5.my_ec_site.entity.Sku;
import com.github.Atgsasakazh5.my_ec_site.exception.ResourceNotFoundException;
import com.github.Atgsasakazh5.my_ec_site.repository.CartDao;
import com.github.Atgsasakazh5.my_ec_site.repository.CartItemDao;
import com.github.Atgsasakazh5.my_ec_site.repository.InventoryDao;
import com.github.Atgsasakazh5.my_ec_site.repository.SkuDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.module.ResolutionException;
import java.util.List;

@Service
public class CartService {

    private final CartDao cartDao;

    private final CartItemDao cartItemDao;

    private final SkuDao skuDao;

    private final InventoryDao inventoryDao;

    public CartService(CartDao cartDao, CartItemDao cartItemDao, SkuDao skuDao, InventoryDao inventoryDao) {
        this.cartDao = cartDao;
        this.cartItemDao = cartItemDao;
        this.skuDao = skuDao;
        this.inventoryDao = inventoryDao;
    }

    @Transactional(readOnly = true)
    public CartDetailDto getCartDetail(Long cartId) {
        // カートアイテムを取得
        var cartItems = cartItemDao.findDetailedItemsByCartId(cartId);
        if (cartItems.isEmpty()) {
            return new CartDetailDto(cartId, List.of(), 0);
        }

        // 合計金額を計算
        int totalPrice = cartItems.stream()
                .mapToInt(item -> item.price() * item.quantity())
                .sum();

        return new CartDetailDto(cartId, cartItems, totalPrice);
    }

    @Transactional(readOnly = true)
    public CartDetailDto getCartDetail(String email) {
        // 1. メールアドレスからカートを取得
        Cart cart = cartDao.findCartByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("カートが見つかりません: " + email));

        // 2. 既存のメソッドに処理を委譲
        return getCartDetail(cart.getId());
    }

    @Transactional
    public CartDetailDto addItemToCart(String email, AddCartItemRequestDto requestDto) {
        // SKUの存在チェック
        Sku sku = skuDao.findById(requestDto.skuId())
                .orElseThrow(() -> new ResourceNotFoundException("存在しないSKUです: " + requestDto.skuId()));

        // 在庫情報を取得
        Inventory inventory = inventoryDao.findBySkuId(sku.getId())
                .orElseThrow(() -> new IllegalStateException("在庫情報が見つかりません: SKU ID " + sku.getId()));

        // カートを取得（.get()を回避）
        Cart cart = cartDao.findCartByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("カートが見つかりません: " + email));

        // カート内の既存アイテムを探す
        CartItem cartItem = cartItemDao.findByCartIdAndSkuId(cart.getId(), sku.getId())
                .orElseGet(() -> {
                    // 存在しない場合は、新しいCartItemを作成
                    CartItem newItem = new CartItem();
                    newItem.setCartId(cart.getId());
                    newItem.setSkuId(sku.getId());
                    newItem.setQuantity(0);
                    return newItem;
                });

        int newQuantity = cartItem.getQuantity() + requestDto.quantity();

        // 在庫チェック
        if (inventory.getQuantity() < newQuantity) {
            throw new IllegalStateException("在庫が不足しています。");
        }

        // 数量を更新
        cartItem.setQuantity(newQuantity);

        // 新規か既存かでsave/updateを呼び分ける
        if (cartItem.getId() == null) {
            cartItemDao.save(cartItem);
        } else {
            cartItemDao.update(cartItem);
        }

        // カート詳細を返す
        return getCartDetail(cart.getId());
    }
}
