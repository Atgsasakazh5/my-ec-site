package com.github.Atgsasakazh5.my_ec_site.service;

import com.github.Atgsasakazh5.my_ec_site.dto.*;
import com.github.Atgsasakazh5.my_ec_site.entity.*;
import com.github.Atgsasakazh5.my_ec_site.exception.ResourceNotFoundException;
import com.github.Atgsasakazh5.my_ec_site.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderDao orderDao;

    private final OrderDetailDao orderDetailDao;

    private final InventoryDao inventoryDao;

    private final CartDao cartDao;

    private final CartItemDao cartItemDao;

    private final UserService userService;

    public OrderService(OrderDao orderDao, OrderDetailDao orderDetailDao, InventoryDao inventoryDao, CartDao cartDao, CartItemDao cartItemDao, UserService userService) {
        this.orderDao = orderDao;
        this.orderDetailDao = orderDetailDao;
        this.inventoryDao = inventoryDao;
        this.cartDao = cartDao;
        this.cartItemDao = cartItemDao;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<OrderDetailDto> getOrderDetails(String email, Long orderId) {
        // ユーザーIDを取得
        UserDto user = userService.findByEmail(email);

        // 注文を取得
        Order order = orderDao.findOrderById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("注文が見つかりません: " + orderId));

        // orderの属するuserと、リクエストしているユーザーが一致するかを確認
        if (!order.getUserId().equals(user.id())) {
            throw new SecurityException("この注文にアクセスする権限がありません。");
        }

        // 注文詳細を取得して返す
        return orderDetailDao.findByOrderId(orderId);
    }

    @Transactional
    public List<OrderDetailDto> placeOrder(String email, CreateOrderRequestDto requestDto) {
        // カートとユーザーIDを取得
        Cart cart = cartDao.findCartByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("カートが見つかりません: " + email));

        // カートアイテムを取得
        List<CartItemDto> cartItems = cartItemDao.findDetailedItemsByCartId(cart.getId());

        if (cartItems.isEmpty()) {
            throw new IllegalStateException("カートが空です");
        }

        // 在庫をロックして検証し、更新用リストを作成
        List<Long> skuIds = cartItems.stream().map(CartItemDto::skuId).toList();
        Map<Long, Inventory> lockedInventories = inventoryDao.findBySkuIdsWithLock(skuIds).stream()
                .collect(Collectors.toMap(Inventory::getSkuId, i -> i));

        List<Inventory> inventoryUpdates = new ArrayList<>();
        for (CartItemDto item : cartItems) {
            Inventory inventory = lockedInventories.get(item.skuId());
            if (inventory == null || inventory.getQuantity() < item.quantity()) {
                throw new IllegalStateException("在庫が不足しています: SKU ID " + item.skuId());
            }
            inventory.setQuantity(inventory.getQuantity() - item.quantity());
            inventoryUpdates.add(inventory);
        }

        // 在庫を一括更新
        inventoryDao.updateAll(inventoryUpdates);

        // 注文を作成
        Order order = new Order();
        order.setUserId(cart.getUserId());
        order.setStatus(OrderStatus.PENDING);

        // 合計金額を再計算
        int totalPrice = cartItems.stream().mapToInt(item -> item.price() * item.quantity()).sum();
        order.setTotalPrice(totalPrice);
        order.setShippingAddress(requestDto.shippingAddress());
        Order savedOrder = orderDao.saveOrder(order);

        // 注文明細を作成
        List<OrderDetail> orderDetails = cartItems.stream()
                .map(item -> new OrderDetail(
                        null,
                        savedOrder.getId(),
                        item.skuId(),
                        item.quantity(),
                        item.price()))
                .toList();
        orderDetailDao.save(orderDetails);

        // カートを空にする
        cartItemDao.deleteByCartId(cart.getId()); // <- DAOに新メソッドが必要

        // 注文詳細を返却
        return getOrderDetails(email, savedOrder.getId());
    }

    @Transactional(readOnly = true)
    public List<OrderSummaryDto> getOrderSummaries(String email) {
        UserDto user = userService.findByEmail(email);

        return orderDao.findOrderSummariesByUserId(user.id());
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(String email, Long orderId) {
        UserDto user = userService.findByEmail(email);

        Order order = orderDao.findOrderById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("注文が見つかりません: " + orderId));

        // オーダーの属するユーザーとリクエストしているユーザーが一致するかを確認
        if (!order.getUserId().equals(user.id())) {
            throw new SecurityException("この注文にアクセスする権限がありません。");
        }

        List<OrderDetailDto> orderDetails = orderDetailDao.findByOrderId(orderId);

        return new OrderDetailResponse(
                order.getId(),
                order.getShippingAddress(),
                order.getPostalCode(),
                order.getShippingName(),
                order.getTotalPrice(),
                order.getStatus(),
                order.getOrderedAt(),
                orderDetails
        );
    }
}
