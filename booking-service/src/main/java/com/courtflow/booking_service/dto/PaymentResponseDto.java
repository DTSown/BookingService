package com.courtflow.booking_service.dto;

import com.courtflow.booking_service.entity.Payment.PaymentMethod;
import com.courtflow.booking_service.entity.Payment.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentResponseDto {
    private Long paymentId;
    private Long bookingId;
    private String transactionId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String bookingStatus;
    private LocalDateTime paymentTime;
}