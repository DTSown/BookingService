package com.courtflow.booking_service.dto;

import com.courtflow.booking_service.entity.Payment.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequestDto {

    @NotNull(message = "Mã đơn bookingId không được để trống")
    private Long bookingId;

    @NotNull(message = "Vui lòng chọn phương thức thanh toán")
    private PaymentMethod paymentMethod;

    // Tùy chọn: mã giao dịch từ bên ngoài (nếu không truyền, backend tự sinh mã test)
    private String transactionId;
}