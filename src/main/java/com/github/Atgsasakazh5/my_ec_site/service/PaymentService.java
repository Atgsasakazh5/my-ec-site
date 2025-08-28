package com.github.Atgsasakazh5.my_ec_site.service;

import com.github.Atgsasakazh5.my_ec_site.dto.PaymentRequestDto;

public interface PaymentService {
    void processPayment(PaymentRequestDto request);
}