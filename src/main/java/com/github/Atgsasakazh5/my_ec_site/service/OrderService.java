package com.github.Atgsasakazh5.my_ec_site.service;

import com.github.Atgsasakazh5.my_ec_site.dto.CreateOrderRequestDto;
import com.github.Atgsasakazh5.my_ec_site.repository.OrderDao;
import com.github.Atgsasakazh5.my_ec_site.repository.OrderDetailDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final CartService cartService;

    private final OrderDao orderDao;

    private final OrderDetailDao orderDetailDao;

    public OrderService(CartService cartService, OrderDao orderDao, OrderDetailDao orderDetailDao) {
        this.cartService = cartService;
        this.orderDao = orderDao;
        this.orderDetailDao = orderDetailDao;
    }

    @Transactional
    public void placeOrder(String email, CreateOrderRequestDto requestDto) {
        // カートの詳細を取得
        var cartDetail = cartService.getCartDetail(email);

        if (cartDetail.cartItems().isEmpty()) {
            throw new IllegalStateException("カートが空です");
        }

        // 再度在庫確認


        // 注文を作成
        var order = orderDao.saveOrder(
                email,
                cartDetail.totalPrice(),
                shippingAddress,
                postalCode,
                shippingName
        );

        // 注文詳細を作成
        cartDetail.cartItems().forEach(cartItem -> {
            orderDetailDao.createOrderDetail(
                    order.getId(),
                    cartItem.skuId(),
                    cartItem.quantity(),
                    cartItem.price()
            );
        });

        // カートをクリア
        cartService.clearCart(email);
    }
}
