package com.courtflow.booking_service.service;

import com.courtflow.booking_service.dto.PaymentRequestDto;
import com.courtflow.booking_service.dto.PaymentResponseDto;

public interface PaymentService {
    PaymentResponseDto processPayment(PaymentRequestDto request);
}