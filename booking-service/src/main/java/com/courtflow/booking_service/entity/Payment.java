package com.courtflow.booking_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @Column(name = "transaction_id", nullable = false, unique = true, length = 100)
    private String transactionId; // Mã giao dịch ngân hàng / cổng thanh toán

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod; // VNPAY, MOMO, CASH, BANK_TRANSFER

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status; // SUCCESS, FAILED

    @CreationTimestamp
    @Column(name = "payment_time", updatable = false)
    private LocalDateTime paymentTime;

    public enum PaymentMethod {
        VNPAY,
        MOMO,
        BANK_TRANSFER,
        CASH
    }

    public enum PaymentStatus {
        SUCCESS,
        FAILED
    }
}