package com.github.Atgsasakazh5.my_ec_site.service;

import com.github.Atgsasakazh5.my_ec_site.dto.PaymentRequestDto;
import com.github.Atgsasakazh5.my_ec_site.repository.OrderDao;
import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final String stripeSecretKey;
    private final OrderDao orderDao;

    public PaymentService(
            OrderDao orderDao,
            @Value("${stripe.api.secret-key}") String stripeSecretKey
    ) {
        this.orderDao = orderDao;
        this.stripeSecretKey = stripeSecretKey;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = this.stripeSecretKey;
    }

    public void processPayment(PaymentRequestDto request) {
        if (orderDao.findOrderById(request.orderId()).isEmpty()) {
            throw new IllegalArgumentException("指定された注文は存在しません: " + request.orderId());
        }

        // Stripe APIを使用して支払いを処理するコードをここに追加
    }

}
