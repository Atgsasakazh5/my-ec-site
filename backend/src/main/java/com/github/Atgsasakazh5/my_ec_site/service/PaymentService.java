package com.github.Atgsasakazh5.my_ec_site.service;

import com.github.Atgsasakazh5.my_ec_site.dto.PaymentRequestDto;
import com.github.Atgsasakazh5.my_ec_site.entity.Order;
import com.github.Atgsasakazh5.my_ec_site.entity.OrderStatus;
import com.github.Atgsasakazh5.my_ec_site.exception.ResourceNotFoundException;
import com.github.Atgsasakazh5.my_ec_site.repository.OrderDao;
import com.stripe.Stripe;
import com.stripe.exception.CardException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

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

    @Transactional
    public void processPayment(PaymentRequestDto request) {
        Order order = orderDao.findOrderById(request.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("指定された注文は存在しません: " + request.orderId()));

        if (!order.getStatus().equals(OrderStatus.PENDING)){
            throw new IllegalStateException("既に支払い済みです。現在のステータス: " + order.getStatus());
        }

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount((long) order.getTotalPrice())
                    .setCurrency("jpy")
                    .setPaymentMethod(request.paymentMethodId())
                    .setConfirmationMethod(PaymentIntentCreateParams.ConfirmationMethod.MANUAL)
                    .setConfirm(true)
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            if ("succeeded".equals(paymentIntent.getStatus())) {
                order.setStatus(OrderStatus.PAID);
                orderDao.updateOrder(order);
            } else {
                throw new IllegalStateException("支払いが完了しませんでした。ステータス: " + paymentIntent.getStatus());
            }

        } catch (CardException e) {
            throw new IllegalStateException("決済エラー: " + e.getMessage(), e);
        } catch (StripeException e) {
            throw new RuntimeException("支払い処理中にStripeでエラーが発生しました。", e);
        }
    }

}
