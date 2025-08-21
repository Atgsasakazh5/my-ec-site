package com.github.Atgsasakazh5.my_ec_site.controller;

import com.github.Atgsasakazh5.my_ec_site.dto.*;
import com.github.Atgsasakazh5.my_ec_site.service.OrderService;
import com.github.Atgsasakazh5.my_ec_site.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public ResponseEntity<List<OrderSummaryDto>> getOrderSummaries(
            Authentication authentication) {
        String email = authentication.getName();
        List<OrderSummaryDto> orders = orderService.getOrderSummaries(email);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponseDto> getOrderDetails(
            Authentication authentication,
            @PathVariable Long orderId) {
        String email = authentication.getName();
        var orderDetail = orderService.getOrderDetail(email, orderId);
        return ResponseEntity.ok(orderDetail);
    }
}
