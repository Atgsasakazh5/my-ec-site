package com.github.Atgsasakazh5.my_ec_site.service;

import com.github.Atgsasakazh5.my_ec_site.dto.CreateOrderRequestDto;
import com.github.Atgsasakazh5.my_ec_site.dto.OrderDetailDto;
import com.github.Atgsasakazh5.my_ec_site.dto.OrderDetailResponseDto;
import com.github.Atgsasakazh5.my_ec_site.dto.OrderSummaryDto;

import java.util.List;

public interface OrderService {
    List<OrderDetailDto> getOrderDetails(String email, Long orderId);
    List<OrderDetailDto> placeOrder(String email, CreateOrderRequestDto requestDto);
    List<OrderSummaryDto> getOrderSummaries(String email);
    OrderDetailResponseDto getOrderDetail(String email, Long orderId);
}