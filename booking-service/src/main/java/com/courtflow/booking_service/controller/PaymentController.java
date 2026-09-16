package com.courtflow.booking_service.controller;

import com.courtflow.booking_service.dto.PaymentRequestDto;
import com.courtflow.booking_service.dto.PaymentResponseDto;
import com.courtflow.booking_service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/checkout")
    public ResponseEntity<PaymentResponseDto> checkout(@Valid @RequestBody PaymentRequestDto request) {
        PaymentResponseDto response = paymentService.processPayment(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}