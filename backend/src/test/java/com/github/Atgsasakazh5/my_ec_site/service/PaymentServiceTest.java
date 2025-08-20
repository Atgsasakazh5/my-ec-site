package com.github.Atgsasakazh5.my_ec_site.service;

import com.github.Atgsasakazh5.my_ec_site.dto.PaymentRequestDto;
import com.github.Atgsasakazh5.my_ec_site.entity.Order;
import com.github.Atgsasakazh5.my_ec_site.entity.OrderStatus;
import com.github.Atgsasakazh5.my_ec_site.exception.ResourceNotFoundException;
import com.github.Atgsasakazh5.my_ec_site.repository.OrderDao;
import com.stripe.exception.CardException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private OrderDao orderDao;

    private MockedStatic<PaymentIntent> mockedPaymentIntent;

    @BeforeEach
    void setUp() {
        mockedPaymentIntent = Mockito.mockStatic(PaymentIntent.class);
    }

    @AfterEach
    void tearDown() {
        mockedPaymentIntent.close();
    }

    @Test
    @DisplayName("支払いが成功し、注文ステータスがPAIDになること")
    void processPayment_shouldSucceed_andUpdateOrderStatusToPaid() {
        // Arrange
        var request = new PaymentRequestDto(1L, "pm_test");
        var order = new Order();
        order.setStatus(OrderStatus.PENDING);
        order.setTotalPrice(1000);

        var successfulPaymentIntent = new PaymentIntent();
        successfulPaymentIntent.setStatus("succeeded");

        when(orderDao.findOrderById(request.orderId())).thenReturn(Optional.of(order));
        mockedPaymentIntent.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                .thenReturn(successfulPaymentIntent);

        // Act
        paymentService.processPayment(request);

        // Assert
        verify(orderDao, times(1)).updateOrder(argThat(
                updatedOrder -> updatedOrder.getStatus() == OrderStatus.PAID
        ));
    }

    @Test
    @DisplayName("Stripeでカードエラーが発生した場合、例外をスローすること")
    void processPayment_shouldThrowException_whenCardIsDeclined() throws CardException {
        // Arrange
        var request = new PaymentRequestDto(1L, "pm_card_declined");
        var order = new Order();
        order.setStatus(OrderStatus.PENDING);
        order.setTotalPrice(1000);

        when(orderDao.findOrderById(request.orderId())).thenReturn(Optional.of(order));
        // PaymentIntent.createが呼ばれたら、CardExceptionをスローする
        mockedPaymentIntent.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                .thenThrow(new CardException("支払いに失敗しました。", null, null, null, null, null, null, null));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            paymentService.processPayment(request);
        });
    }
}