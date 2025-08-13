package com.github.Atgsasakazh5.my_ec_site.controller;

import com.github.Atgsasakazh5.my_ec_site.dto.ApiResponse;
import com.github.Atgsasakazh5.my_ec_site.dto.CreateOrderRequestDto;
import com.github.Atgsasakazh5.my_ec_site.dto.OrderDetailDto;
import com.github.Atgsasakazh5.my_ec_site.dto.PaymentRequestDto;
import com.github.Atgsasakazh5.my_ec_site.service.OrderService;
import com.github.Atgsasakazh5.my_ec_site.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    private final PaymentService paymentService;

    public OrderController(OrderService orderService, PaymentService paymentService) {
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<List<OrderDetailDto>> createOrder(
            Authentication authentication,
            @Valid @RequestBody CreateOrderRequestDto createOrderRequestDto) {

        var email = authentication.getName();
        List<OrderDetailDto> orderDetails = orderService.placeOrder(email, createOrderRequestDto);
        return new ResponseEntity<>(orderDetails, HttpStatus.CREATED);

    }

    @PostMapping("/payment")
    public ResponseEntity<ApiResponse> handlePayment(
            @Valid @RequestBody PaymentRequestDto request
    ) {
        paymentService.processPayment(request);
        return ResponseEntity.ok(new ApiResponse(true, "支払いが完了しました。"));
    }
}
